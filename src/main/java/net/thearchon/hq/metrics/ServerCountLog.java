package net.thearchon.hq.metrics;

import net.thearchon.hq.Archon;
import net.thearchon.hq.handler.Handler;
import net.thearchon.hq.handler.GameLobbyHandler;
import net.thearchon.hq.handler.NamedBukkitHandler;
import net.thearchon.hq.util.io.AsyncDatabase;
import net.thearchon.hq.util.io.Database;
import net.thearchon.hq.util.io.Database.ResultSetIterator;
import net.thearchon.hq.ServerType;
import net.thearchon.hq.client.BukkitClient;
import net.thearchon.hq.Settings;
import net.thearchon.hq.handler.LobbyHandler;

import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ServerCountLog implements Runnable {

    private final Archon server;
    private final AsyncDatabase db;
    private final Map<String, ServerMetric> metrics = new HashMap<>();

    public ServerCountLog(Archon server) {
        this.server = server;

        Settings c = server.getSettings();
        db = new AsyncDatabase(new Database(c.getMysqlHost(), c.getMysqlPort(), "archonlog",
                c.getMysqlUsername(), c.getMysqlPassword()));
//        db.execute("CREATE TABLE IF NOT EXISTS server_counts_hourly (" +
//                "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
//                " archon VARCHAR(32) NOT NULL," +
//                " time DATETIME NOT NULL," +
//                " high INT UNSIGNED NOT NULL," +
//                " low INT UNSIGNED NOT NULL," +
//                " avg INT UNSIGNED NOT NULL)");
        db.execute("CREATE TABLE IF NOT EXISTS server_counts (" +
                "server VARCHAR(30) NOT NULL," +
                " time DATETIME NOT NULL," +
                " online_count SMALLINT UNSIGNED NOT NULL)");
//        db.execute("CREATE TABLE IF NOT EXISTS server_index (" +
//                "id TINYINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
//                " archon VARCHAR(30) NOT NULL UNIQUE KEY)");

        // Load available servers to track
        for (Handler handler : server.getHandlers().values()) {
            if (handler instanceof LobbyHandler) {
                register("lobby");
            } else if (handler instanceof GameLobbyHandler<?, ?>) {
                GameLobbyHandler<?, ?> h = (GameLobbyHandler<?, ?>) handler;
                ServerType type = h.getGameHandler().getLobbyType();
                register(type.name().replace("_LOBBY", ""));
            } else if (handler instanceof NamedBukkitHandler<?>) {
                NamedBukkitHandler<?> h = (NamedBukkitHandler<?>) handler;
                for (BukkitClient client : h.getClients()) {
                    register(client.getServerName());
                }
            }
        }

        // Interval to check time change
        server.runTaskLater(() -> server.runTaskTimer(this, 1, TimeUnit.SECONDS),
                30, TimeUnit.SECONDS); // 30 second start delay
    }

    private int lastMinute = -1, lastHour = -1;

    @Override
    public void run() {
        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);

        int minute = utc.getMinute();
        if (lastMinute == -1) {
            lastMinute = minute;
        }
        if (minute != lastMinute) {
            lastMinute = minute;

            String time = utc.getYear() + "-" + utc.getMonthValue() + "-" + utc.getDayOfMonth()
                    + " " + utc.getHour() + ":" + (utc.getMinute() - 1);

            // Minute change event
            onMinuteChange(time);
        }

        int hour = utc.getHour();
        if (hour != lastHour) {
            lastHour = hour;
            // Hour change event
//            onHourChange();
        }
    }

    private void onMinuteChange(String time) {
        Statement stmt = db.createStatement();

        for (Handler handler : server.getHandlers().values()) {
            if (handler instanceof LobbyHandler) {
                logMinute(stmt, "lobby", time, ((LobbyHandler) handler).getOnlineCount());
            } else if (handler instanceof GameLobbyHandler<?, ?>) {
                GameLobbyHandler<?, ?> h = (GameLobbyHandler<?, ?>) handler;
                ServerType type = h.getGameHandler().getLobbyType();
                logMinute(stmt, type.name().replace("_LOBBY", ""), time, h.getOnlineCount());
            } else if (handler instanceof NamedBukkitHandler<?>) {
                NamedBukkitHandler<?> h = (NamedBukkitHandler<?>) handler;
                for (BukkitClient client : h.getClients()) {
                    logMinute(stmt, client.getServerName(), time, client.getOnlineCount());
                }
            }
        }

        db.executeBatch(stmt);
    }

    private void logMinute(Statement stmt, String server, String time, int onlineCount) {
        server = server.toLowerCase();
        try {
            stmt.addBatch(db.inject(true, "INSERT INTO server_counts VALUES (?, ?, ?)", server, time, onlineCount));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void onHourChange() {
        for (Handler handler : server.getHandlers().values()) {
            if (handler instanceof LobbyHandler) {
                ServerMetric m = get("lobby");
                m.logMinute(((LobbyHandler) handler).getOnlineCount());
                log(m);
            } else if (handler instanceof GameLobbyHandler<?, ?>) {
                GameLobbyHandler<?, ?> h = (GameLobbyHandler<?, ?>) handler;
                ServerType type = h.getGameHandler().getLobbyType();
                ServerMetric m = get(type.name().replace("_LOBBY", ""));
                m.logMinute(h.getOnlineCount());
                log(m);
            } else if (handler instanceof NamedBukkitHandler<?>) {
                NamedBukkitHandler<?> h = (NamedBukkitHandler<?>) handler;
                for (BukkitClient client : h.getClients()) {
                    ServerMetric m = get(client.getServerName());
                    m.logMinute(client.getOnlineCount());
                    log(m);
                }
            }
        }
//        db.execute("DELETE FROM server_counts_state");
    }

    private void register(String name) {
        name = name.toLowerCase();
        metrics.put(name, new ServerMetric(name));

//        db.execute("INSERT INTO server_index (archon) VALUES (?)", archon);
    }

    private ServerMetric get(String name) {
        return metrics.get(name.toLowerCase());
    }

    public void log(ServerMetric metric) {
        db.executeQuery(new ResultSetIterator() {
            @Override
            public void handle() {
                db.execute("INSERT INTO server_counts (server, time, high, low, avg) VALUES (?, CONVERT_TZ(NOW(), @@session.time_zone, 'UTC'), ?, ?, ?)",
                        metric.getServerName(),
                        metric.hourHigh(),
                        metric.hourLow(),
                        metric.hourAverage());
            }
        }, "SELECT server, MAX(online_count) AS max, MIN(online_count) AS min, TRUNCATE(AVG(online_count), 1) AS avg FROM server_counts_state GROUP BY server");
    }
}
