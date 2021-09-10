package net.thearchon.hq.util.io;

import net.thearchon.hq.util.io.Database.DatabaseType;
import net.thearchon.hq.util.io.Database.ResultSetIterator;

import java.sql.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncDatabase {

    private final Database db;
    private final ExecutorService executor;

    private ResultProcessor processor;

    public AsyncDatabase(Database db) {
        this(db, null);
    }

    public AsyncDatabase(Database db, ResultProcessor processor) {
        this(db, Executors.newSingleThreadExecutor(), processor);
    }

    public AsyncDatabase(Database db, ExecutorService executor,
            ResultProcessor processor) {
        this.db = db;
        this.executor = executor;
        this.processor = processor;
    }

    public void executeBatch(Statement stmt) {
        execute(() -> {
            try {
                stmt.executeBatch();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void execute(PreparedStatement ps) {
        execute(() -> {
            try {
                ps.execute();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void execute(String sql) {
        execute(() -> db.execute(sql));
    }

    public void execute(String sql, Object... values) {
        execute(() -> db.execute(sql, values));
    }

    public void executeQuery(ResultSetHandler handler, String sql) {
        execute(() -> {
            ResultSet rs = db.executeQuery(sql);
            if (processor != null) {
                processor.process(() -> handler.handle(rs));
            } else {
                handler.handle(rs);
            }
        });
    }

    public void executeQuery(ResultSetHandler handler, String sql, Object... values) {
        execute(() -> {
            ResultSet rs = db.executeQuery(sql, values);
            if (processor != null) {
                processor.process(() -> handler.handle(rs));
            } else {
                handler.handle(rs);
            }
        });
    }

    public void executeQuery(ResultSetIterator iterator, String sql) {
        execute(() -> {
            ResultSet rs = db.executeQuery(sql);
            if (processor != null) {
                processor.process(() -> iterator.start(rs));
            } else {
                iterator.start(rs);
            }
        });
    }

    public void executeQuery(ResultSetIterator iterator, String sql, Object... values) {
        execute(() -> {
            ResultSet rs = db.executeQuery(sql, values);
            if (processor != null) {
                processor.process(() -> iterator.start(rs));
            } else {
                iterator.start(rs);
            }
        });
    }

    public void createTable(String table, String... columns) {
        execute(() -> db.createTable(table, columns));
    }

    public void deleteTable(String table) {
        execute(() -> db.deleteTable(table));
    }

    public void renameTable(String table, String name) {
        execute(() -> db.renameTable(table, name));
    }

    public void clearTable(String table) {
        execute(() -> db.clearTable(table));
    }

    public void addColumnBefore(String table, String column, String identifier) {
        execute(() -> db.addColumnBefore(table, column, identifier));
    }

    public void addColumnAfter(String table, String column, String identifier) {
        execute(() -> db.addColumnAfter(table, column, identifier));
    }

    public void close() {
        execute(db::close);
    }

    public int getRowCount(String table) {
        return db.getRowCount(table);
    }

    public Statement createStatement() {
        return db.createStatement();
    }

    public PreparedStatement prepareStatement(String sql) {
        return db.prepareStatement(sql);
    }

    public List<String> getColumns(String table) {
        return db.getColumns(table);
    }

    public Set<String> getTables() {
        return db.getTables();
    }

    public boolean checkConnection() {
        return db.checkConnection();
    }

    public Connection getConnection() {
        return db.getConnection();
    }

    public DatabaseType getDatabaseType() {
        return db.getDatabaseType();
    }

    public long getLastActivity() {
        return db.getLastActivity();
    }

    public String implode(boolean quote, String... values) {
        return db.implode(quote, values);
    }

    public String inject(boolean quote, String query, Object... values) {
        return db.inject(quote, query, values);
    }

    public Database sync() {
        return db;
    }

    public void shutdown() {
        executor.shutdown();
    }

    public void setProcessor(ResultProcessor processor) {
        this.processor = processor;
    }

    private void execute(Runnable task) {
        executor.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public interface ResultSetHandler {
        void handle(ResultSet rs);
    }
}
