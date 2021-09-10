package net.thearchon.hq;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.thearchon.hq.util.io.AsyncDatabase;
import net.thearchon.hq.util.io.Database;
import org.bson.Document;

import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

public class DataSource {

    public static final String PRIMARY_MONGO_DATABASE = "archon";

    private static final int MYSQL_POOL_SIZE = 24;
    private static final int MONGODB_POOL_SIZE = 16;

    private final Archon archon;

    private AsyncDatabase logdb;
    private HikariDataSource hikari;
    private MongoClient mongodb;

    private final ScheduledExecutorService pool = Executors.newScheduledThreadPool(MYSQL_POOL_SIZE);

    public DataSource(Archon archon) {
        this.archon = archon;
    }

    public void initMysql() {
        if (hikari != null) return;

        HikariConfig conf = new HikariConfig();
        conf.setPoolName("MySQL DataSource");
        conf.setConnectionTestQuery("SELECT 1;");
        conf.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        conf.setMaximumPoolSize(MYSQL_POOL_SIZE);
        conf.setMinimumIdle(MYSQL_POOL_SIZE);
        conf.setConnectionTimeout(10000);
        conf.setLeakDetectionThreshold(15000);
        conf.setMaxLifetime(1800000); // 30 minutes

        Settings settings = archon.getSettings();
        conf.addDataSourceProperty("serverName", settings.getMysqlHost());
        conf.addDataSourceProperty("port", settings.getMysqlPort());
        conf.addDataSourceProperty("databaseName", settings.getMysqlDatabase());
        conf.addDataSourceProperty("user", settings.getMysqlUsername());
        conf.addDataSourceProperty("password", settings.getMysqlPassword());
        conf.addDataSourceProperty("autoReconnect", "true");
        conf.addDataSourceProperty("dontTrackOpenResources", "true");

        hikari = new HikariDataSource(conf);

        // To fix the group by error
        try (Connection conn = hikari.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.addBatch("SET global sql_mode='STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION';");
            stmt.addBatch("SET session sql_mode='STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION';");
            stmt.executeBatch();
        } catch (SQLException e) {
            archon.getLogger().log(Level.SEVERE, "Failed to initialize MySQL", e);
        }

        archon.getLogger().info("Created MySQL connection");
        createTables();

        Settings c = archon.getSettings();
        logdb = new AsyncDatabase(new Database(
                c.getMysqlHost(), c.getMysqlPort(), "archonlog",
                c.getMysqlUsername(), c.getMysqlPassword()));
    }

    public void initMongoDb() {
        if (mongodb == null) {
            Settings settings = archon.getSettings();
            mongodb = MongoClients.create(String.format("mongodb://%s:%s@%s:27017/admin?maxPoolSize=" + MONGODB_POOL_SIZE,
                    settings.getMongoDbUsername(), settings.getMongoDbPassword(), settings.getMongoDbHost()));
            archon.getLogger().info("Created MongoDB connection");
        }
    }

    public void closeMysql() {
        if (hikari != null) {
            hikari.close();
            hikari = null;
            archon.getLogger().info("Closed MySQL connection");
        }
    }

    public void closeMongoDb() {
        if (mongodb != null) {
            mongodb.close();
            mongodb = null;
            archon.getLogger().info("Closed MongoDB connection");
        }
    }

    public AsyncDatabase getLogDb() {
        return logdb;
    }

    public MongoDatabase getDatabase() {
        return mongodb.getDatabase(PRIMARY_MONGO_DATABASE);
    }

    public MongoCollection<Document> getCollection(String name) {
        return getDatabase().getCollection(name);
    }

    public MongoDatabase getDatabase(String name) {
        return mongodb.getDatabase(name);
    }

    public MongoCollection<Document> getCollection(String database, String name) {
        return getDatabase(database).getCollection(name);
    }

    public void getConnection(AsyncSession session) {
        schedule(() -> {
            Connection conn = null;
            try {
                conn = hikari.getConnection();
                session.handle(conn);
            } catch (SQLException e) {
                archon.getLogger().log(Level.WARNING, "Failed to use connection.", e);
            } finally {
                try {
                    conn.close();
                } catch (SQLException e) {
                    archon.getLogger().log(Level.WARNING, "Failed to close connection.", e);
                }
            }
        });
    }

    /**
     * Retrieve a new connection from the pool.
     * @return connection
     */
    public Connection getConnection() {
        try {
            return hikari.getConnection();
        } catch (SQLException e) {
            archon.getLogger().log(Level.SEVERE, "Failed to obtain connection.", e);
        }
        return null;
    }

    /**
     * Query the given SQL prepared statement.
     * @param ps prepared statement
     * @param done task which provides the returned ResultSet.
     */
    public void query(PreparedStatement ps, AsyncQueryResult done) {
        schedule(() -> {
            Connection conn = null;
            try {
                conn = hikari.getConnection();

                // Execute result task on main thread.
                ResultSet rs = ps.executeQuery();
                archon.runTask(() -> {
                    try {
                        done.done(rs);
                    } catch (Exception e) {
                        archon.getLogger().log(Level.WARNING, "Failed to run result AsyncQueryResult task.", e);
                    }
                });
            } catch (SQLException e) {
                archon.getLogger().log(Level.WARNING, "Failed to execute statement.", e);
            } finally {
                try {
                    ps.close();
                } catch (SQLException e) {
                    archon.getLogger().log(Level.WARNING, "Failed to close statement.", e);
                }
                try {
                    conn.close();
                } catch (SQLException e) {
                    archon.getLogger().log(Level.WARNING, "Failed to close connection.", e);
                }
            }
        });
    }

    /**
     * Query the given SQL statement. ResultSet must be closed by the user
     * of this method to prevent memory leaks.
     * @param sql query statemement
     * @param done task which provides the returned ResultSet.
     */
    public void query(String sql, AsyncQueryResult done) {
        schedule(() -> {
            Connection conn = null;
            Statement stmt = null;
            try {
                conn = hikari.getConnection();
                stmt = conn.createStatement();

                // Execute result task on main thread.
                ResultSet rs = stmt.executeQuery(sql);
                archon.runTask(() -> {
                    try {
                        done.done(rs);
                    } catch (Exception e) {
                        archon.getLogger().log(Level.WARNING, "Failed to run result AsyncQueryResult task.", e);
                    } finally {
//                        try {
//                            stmt.close();
//                        } catch (SQLException e) {
//                            archon.getLogger().log(Level.WARNING, "Failed to close statement.", e);
//                        }
//                        try {
//                            conn.close();
//                        } catch (SQLException e) {
//                            archon.getLogger().log(Level.WARNING, "Failed to close connection.", e);
//                        }
                    }
                });
            } catch (SQLException e) {
                archon.getLogger().log(Level.WARNING, "Failed to execute statement.", e);
            }
        });
    }

    public void execute(String sql, Object... values) {
        execute(inject(true, sql, values));
    }

    /**
     * Executes the given sql statement.
     * @param sql update execution statement
     */
    public void execute(String sql) {
        schedule(() -> {
            Connection conn = null;
            Statement stmt = null;
            try {
                conn = hikari.getConnection();
                stmt = conn.createStatement();
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                archon.getLogger().log(Level.WARNING, "Failed to execute statement.", e);
            } finally {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    archon.getLogger().log(Level.WARNING, "Failed to close statement.", e);
                }
                try {
                    conn.close();
                } catch (SQLException e) {
                    archon.getLogger().log(Level.WARNING, "Failed to close connection.", e);
                }
            }
        });
    }

    /**
     * Executes the given sql statement.
     * @param sql update execution statement
     * @param done task which provides the returned value.
     */
    public void execute(String sql, AsyncUpdateResult done) {
        schedule(() -> {
            Connection conn = null;
            Statement stmt = null;
            try {
                conn = hikari.getConnection();
                stmt = conn.createStatement();

                // Execute result task on main thread.
                int result = stmt.executeUpdate(sql);
                archon.runTask(() -> {
                    try {
                        done.done(result);
                    } catch (Exception e) {
                        archon.getLogger().log(Level.WARNING, "Failed to run result AsyncUpdateResult task.", e);
                    }
                });
            } catch (SQLException e) {
                archon.getLogger().log(Level.WARNING, "Failed to execute statement.", e);
            } finally {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    archon.getLogger().log(Level.WARNING, "Failed to close statement.", e);
                }
                try {
                    conn.close();
                } catch (SQLException e) {
                    archon.getLogger().log(Level.WARNING, "Failed to close connection.", e);
                }
            }
        });
    }

    private void schedule(Runnable task) {
        pool.execute(task);
    }

    public interface AsyncSession {
        void handle(Connection conn);
    }

    public interface AsyncQueryResult extends AsyncResult<ResultSet> {
        void done(ResultSet rs);
    }

    public interface AsyncUpdateResult extends AsyncResult<Integer> {
        void done(int value);
    }

    public interface AsyncResult<T> {
        void done(T task);
    }

    public String implode(boolean quote, String... values) {
        StringBuilder result = new StringBuilder();
        for (String s : values) {
            if (quote) result.append("'");
            result.append(s);
            result.append(quote ? "', " : ", ");
        }
        result.setLength(result.length() - 2);
        return result.toString();
    }

    public String inject(boolean quote, String query, Object... values) {
        StringBuilder result = new StringBuilder();
        int cur = 0;
        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            if (query.charAt(i) == '?') {
                Object val = values[cur++];
                String str = val != null ? val.toString() : null;
                if (str == null || str.equalsIgnoreCase("NULL")) {
                    result.append("NULL");
                } else {
                    if (quote) result.append("'");
                    result.append(str);
                    if (quote) result.append("'");
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private void createTables() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Players
            stmt.addBatch("CREATE TABLE IF NOT EXISTS players (" +
                    "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    "uuid VARCHAR(36) NOT NULL, name VARCHAR(16) NOT NULL, " +
                    "rank VARCHAR(32) NOT NULL DEFAULT 'DEFAULT', " +
                    "coins INT NOT NULL DEFAULT 0, " +
                    "ip_address VARCHAR(39) NOT NULL, " +
                    "seen DATETIME NOT NULL, " +
                    "banned BOOLEAN NOT NULL DEFAULT false, " +
                    "ban_time BIGINT DEFAULT NULL, " +
                    "ban_reason VARCHAR(128) DEFAULT NULL, " +
                    "votes INT UNSIGNED NOT NULL DEFAULT 0, " +
                    "unclaimed_votes INT UNSIGNED NOT NULL DEFAULT 0)");
            // Transfer Log
            stmt.addBatch("CREATE TABLE IF NOT EXISTS transfer_log (" +
                    "id INT UNSIGNED NOT NULL, " +
                    "date DATETIME NOT NULL, " +
                    "from_server VARCHAR(30) NOT NULL, " +
                    "to_server VARCHAR(30) NOT NULL, " +
                    "from_ranks VARCHAR(150) NOT NULL, " +
                    "to_ranks VARCHAR(150) NOT NULL);");
            // Ip Log
            stmt.addBatch("CREATE TABLE IF NOT EXISTS ip_log (" +
                    "id INT UNSIGNED NOT NULL, " +
                    "ip_address VARCHAR(39) NOT NULL, " +
                    "date DATETIME NOT NULL, " +
                    "UNIQUE INDEX ix_ip_address (id, ip_address))");
            // Mutes
            stmt.addBatch("CREATE TABLE IF NOT EXISTS mutes (" +
                    "id INT UNSIGNED NOT NULL PRIMARY KEY, " +
                    "time BIGINT DEFAULT NULL, " +
                    "reason VARCHAR(128) DEFAULT NULL)");
            // Subscriptions
//            stmt.addBatch("CREATE TABLE IF NOT EXISTS subscriptions (" +
//                    "uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
//                    "subscription VARCHAR(12) DEFAULT NULL, " +
//                    "expiry DATETIME DEFAULT NULL)");
            // Lobby
            stmt.addBatch("CREATE TABLE IF NOT EXISTS lobby (" +
                    "id INT UNSIGNED NOT NULL PRIMARY KEY, " +
                    "hats TEXT, " +
                    "trails TEXT, " +
                    "gadgets TEXT, " +
                    "selected_hat VARCHAR(32) DEFAULT NULL, " +
                    "selected_trail VARCHAR(32) DEFAULT NULL, " +
                    "selected_gadget VARCHAR(32) DEFAULT NULL, " +
                    "wardrobe TEXT)");
            // App
            stmt.addBatch("CREATE TABLE IF NOT EXISTS archonhq (" +
                    "id INT UNSIGNED NOT NULL PRIMARY KEY, " +
                    "password VARCHAR(24) NOT NULL, " +
                    "last_login DATETIME DEFAULT NULL, " +
                    "last_addr VARCHAR(39) DEFAULT NULL, " +
                    "connects INT UNSIGNED NOT NULL DEFAULT 0)");
            // Purchases
            stmt.addBatch("CREATE TABLE IF NOT EXISTS purchases (" +
                    "transaction VARCHAR(64) NOT NULL PRIMARY KEY, " +
                    "status VARCHAR(10) NOT NULL, " +
                    "name VARCHAR(16) NOT NULL, " +
                    "server VARCHAR(32) NOT NULL, " +
                    "date DATETIME NOT NULL, " +
                    "price DOUBLE NOT NULL, " +
                    "currency VARCHAR(3) NOT NULL, " +
                    "package VARCHAR(64) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL, " +
                    "ip VARCHAR(39) NOT NULL)");
            // Votes
            stmt.addBatch("CREATE TABLE IF NOT EXISTS votes (" +
                    "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    "time DATETIME NOT NULL, " +
                    "service VARCHAR(32) NOT NULL, " +
                    "player INT UNSIGNED NOT NULL, " +
                    "ip_address VARCHAR(39) NOT NULL)");
            // Punishment Log
            stmt.addBatch("CREATE TABLE IF NOT EXISTS punishment_log (" +
                    "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    "punisher INT UNSIGNED NOT NULL, " +
                    "punished INT UNSIGNED NOT NULL, " +
                    "type VARCHAR(7) NOT NULL, " +
                    "time DATETIME NOT NULL, " +
                    "duration BIGINT UNSIGNED DEFAULT NULL, " +
                    "reason VARCHAR(128) DEFAULT NULL)");
            // IP Bans
            stmt.addBatch("CREATE TABLE IF NOT EXISTS ip_bans (" +
                    "ip_address VARCHAR(39) NOT NULL PRIMARY KEY, " +
                    "reason VARCHAR(128) DEFAULT NULL)");
            // Dailytop
            stmt.addBatch("CREATE TABLE IF NOT EXISTS dailytop (" +
                    "date DATE NOT NULL PRIMARY KEY, " +
                    "new_players INT UNSIGNED NOT NULL, " +
                    "most_online INT UNSIGNED NOT NULL, " +
                    "unique_logins INT UNSIGNED NOT NULL)");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS daily_stats (" +
                    "time DATETIME NOT NULL PRIMARY KEY, " +
                    "online INT UNSIGNED NOT NULL, " +
                    "new_players INT UNSIGNED NOT NULL, " +
                    "most_online INT UNSIGNED NOT NULL, " +
                    "unique_logins INT UNSIGNED NOT NULL)");

            /**
             * Minigames
             */
            // Rankup
            stmt.addBatch("CREATE TABLE IF NOT EXISTS rankup (" +
                    "id INT UNSIGNED NOT NULL PRIMARY KEY, " +
                    "kills INT UNSIGNED NOT NULL DEFAULT 0, " +
                    "deaths INT UNSIGNED NOT NULL DEFAULT 0, " +
                    "level INT UNSIGNED NOT NULL DEFAULT 0, " +
                    "friends TEXT)");
            // SkyWars
            stmt.addBatch("CREATE TABLE IF NOT EXISTS skywars (" +
                    "id INT UNSIGNED NOT NULL PRIMARY KEY, " +
                    "kills INT UNSIGNED NOT NULL DEFAULT 0, " +
                    "deaths INT UNSIGNED NOT NULL DEFAULT 0, " +
                    "wins INT UNSIGNED NOT NULL DEFAULT 0, " +
                    "score INT NOT NULL DEFAULT 0, " +
                    "kits TEXT, " +
                    "perks TEXT, " +
                    "selected_perk VARCHAR(32) DEFAULT NULL)");
//        execute("CREATE TABLE IF NOT EXISTS sg (id INT UNSIGNED NOT NULL PRIMARY KEY, kills INT UNSIGNED NOT NULL DEFAULT 0, deaths INT UNSIGNED NOT NULL DEFAULT 0, wins INT UNSIGNED NOT NULL DEFAULT 0, score INT NOT NULL DEFAULT 0)");
//        execute("CREATE TABLE IF NOT EXISTS sgb (id INT UNSIGNED NOT NULL PRIMARY KEY, kills INT UNSIGNED NOT NULL DEFAULT 0, deaths INT UNSIGNED NOT NULL DEFAULT 0, wins INT UNSIGNED NOT NULL DEFAULT 0, score INT NOT NULL DEFAULT 0)");
//        execute("CREATE TABLE IF NOT EXISTS uhc (id INT UNSIGNED NOT NULL PRIMARY KEY, kills INT UNSIGNED NOT NULL DEFAULT 0, deaths INT UNSIGNED NOT NULL DEFAULT 0, solo_wins INT UNSIGNED NOT NULL DEFAULT 0, team_wins INT UNSIGNED NOT NULL DEFAULT 0)");

            /**
             * Arcade
             */
//        execute("CREATE TABLE IF NOT EXISTS arcade (id INT UNSIGNED NOT NULL PRIMARY KEY, tokens INT UNSIGNED NOT NULL DEFAULT 0, daily_tokens INT UNSIGNED NOT NULL DEFAULT 0, tickets INT UNSIGNED NOT NULL DEFAULT 0, wheel_spins INT UNSIGNED NOT NULL DEFAULT 0, last_tokens_given DATETIME DEFAULT CURRENT_TIMESTAMP)");
//        execute("CREATE TABLE IF NOT EXISTS disintegrate (id INT UNSIGNED NOT NULL PRIMARY KEY, wins INT UNSIGNED NOT NULL DEFAULT 0)");
//        execute("CREATE TABLE IF NOT EXISTS snowfight (id INT UNSIGNED NOT NULL PRIMARY KEY, wins INT UNSIGNED NOT NULL DEFAULT 0, kills INT UNSIGNED NOT NULL DEFAULT 0, shots_fired INT UNSIGNED NOT NULL DEFAULT 0)");
//        execute("CREATE TABLE IF NOT EXISTS hot_tnt (id INT UNSIGNED NOT NULL PRIMARY KEY, wins INT UNSIGNED NOT NULL DEFAULT 0)");
//        execute("CREATE TABLE IF NOT EXISTS horse_race (id INT UNSIGNED NOT NULL PRIMARY KEY, wins INT UNSIGNED NOT NULL DEFAULT 0, laps_completed INT UNSIGNED NOT NULL DEFAULT 0)");
//        execute("CREATE TABLE IF NOT EXISTS abba_caving (id INT UNSIGNED NOT NULL PRIMARY KEY, wins INT UNSIGNED NOT NULL DEFAULT 0, highest_score INT UNSIGNED NOT NULL DEFAULT 0, ores_mined INT UNSIGNED NOT NULL DEFAULT 0, perks TEXT NOT NULL, items TEXT NOT NULL, selected_perk VARCHAR(24) DEFAULT NULL, selected_item VARCHAR(24) DEFAULT NULL)");

            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
