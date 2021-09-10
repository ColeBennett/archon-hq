package net.thearchon.hq.util.io;

import java.io.File;
import java.sql.*;
import java.util.*;

public class Database {
	
	private final DatabaseType type;
	
	private Connection connection;
	private long lastActivity;
	
	public Database(String path) {
		this(new File(path));
	}
	
	public Database(File file) {
		type = DatabaseType.SQLITE;
		if (file.getParentFile() != null) {
			file.getParentFile().mkdir();
		}
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Database(String host, int port, String database, String username, String password) {
		type = DatabaseType.MYSQL;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			connection = DriverManager.getConnection(
					"jdbc:mysql://" + host + ":" + port + "/" + database +
							"?autoReconnect=true&dontTrackOpenResources=true", username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		lastActivity = System.currentTimeMillis();
	}
	
	public enum DatabaseType {
		SQLITE, MYSQL
	}

	public int execute(String sql) {
		lastActivity = System.currentTimeMillis();
		try {
			Statement statement = connection.createStatement();	
			int rowsUpdated = statement.executeUpdate(sql);
			statement.close();
			return rowsUpdated;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public int execute(String sql, Object... values) {
		return execute(inject(true, sql, values));
	}

	public ResultSet executeQuery(String sql) {
		lastActivity = System.currentTimeMillis();
		try {
            return connection.createStatement().executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ResultSet executeQuery(String sql, Object... values) {
		return executeQuery(inject(true, sql, values));
	}
	
	public void executeQuery(ResultSetIterator iterator, String sql) {
		iterator.start(executeQuery(sql));
	}
	
	public void executeQuery(ResultSetIterator iterator, String sql, Object... values) {
		iterator.start(executeQuery(sql, values));
	}
	
	public void createTable(String table, String... columns) {
		execute("CREATE TABLE IF NOT EXISTS " + table + " (" + implode(false, columns) + ")");
	}
	
	public void deleteTable(String table) {
		execute("DROP TABLE IF EXISTS " + table);
	}
	
	public void renameTable(String table, String name) {
		execute("RENAME TABLE " + table + " TO " + name);
	}
	
	public void clearTable(String table) {
		execute("DELETE FROM " + table);
	}
	
	public void addColumnBefore(String table, String column, String identifier) {
		execute("ALTER TABLE " + table + " ADD " + column + " BEFORE " + identifier);
	}
	
	public void addColumnAfter(String table, String column, String identifier) {
		execute("ALTER TABLE " + table + " ADD " + column + " AFTER " + identifier);
	}
	
	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int getRowCount(String table) {
		int size = 0;
		try {
			ResultSet rs = executeQuery("SELECT COUNT(*) AS count FROM " + table);
			if (rs.next()) {
				size = rs.getInt("count");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return size;
	}
	
	public Statement createStatement() {
		lastActivity = System.currentTimeMillis();
		try {
			return connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public PreparedStatement prepareStatement(String sql) {
		lastActivity = System.currentTimeMillis();
		try {
			return connection.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<String> getColumns(String table) {
		List<String> columns = new ArrayList<>();
		try {
			ResultSet rs = executeQuery("SELECT * FROM " + table);
			ResultSetMetaData md = rs.getMetaData();
			for (int i = 1; i <= md.getColumnCount(); i++) {
				columns.add(md.getColumnName(i));
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return columns;
	}
	
	public Set<String> getTables() {	
		lastActivity = System.currentTimeMillis();
		Set<String> tables = new LinkedHashSet<>();
		try {
			DatabaseMetaData md = connection.getMetaData();
			ResultSet rs = md.getTables(null, null, "%", null);
			while (rs.next()) {
				tables.add(rs.getString(3));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tables;
	}
	
	public boolean checkConnection() {
		try {
			return connection.isClosed();
		} catch (SQLException e) {
			return false;
		}
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public DatabaseType getDatabaseType() {
		return type;
	}
	
	public long getLastActivity() {
		return lastActivity;
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
	
	public static abstract class ResultSetIterator {
		protected final Map<String, Object> current = new LinkedHashMap<>();
		protected int currentRow;
		
		public ResultSetIterator() {
			
		}
		
		public ResultSetIterator(ResultSet rs) {
			start(rs);
		}
		
		public void start(ResultSet rs) {
			try {
				Set<String> columns = new LinkedHashSet<>();
				ResultSetMetaData meta = rs.getMetaData();
				int columnCount = meta.getColumnCount();
				for (int i = 1; i <= columnCount; i++) {
					columns.add(meta.getColumnName(i));
				}
				if (!rs.isBeforeFirst()) {
					noResults();
				}
				currentRow = 0;
				current.clear();
				while (rs.next()) {
					for (String column : columns) {
						current.put(column, rs.getObject(column));
					}
					handle();
					currentRow++;
					current.clear();
				}
				rs.close();
				onFinish();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		public abstract void handle();
		
		public void noResults() {}
		
		public void onFinish() {}

		public Object getObject(String column) {
			return current.get(column);
		}
		
		public byte[] getBytes(String column) {
			return (byte[]) getObject(column);
		}
		
		public byte getByte(String column) {
			return (Byte) getObject(column);
		}
		
		public float getFloat(String column) {
			return (Float) getObject(column);
		}
		
		public double getDouble(String column) {
			return (Double) getObject(column);
		}
		
		public short getShort(String column) {
			return (Short) getObject(column);
		}
		
		public int getInt(String column) {
			return (Integer) getObject(column);
		}
		
		public long getLong(String column) {
			return (Long) getObject(column);
		}
		
		public char getChar(String column) {
			return (Character) getObject(column);
		}
		
		public String getString(String column) {
			return getObject(column).toString().replace("''", "'");
		}
		
		public boolean getBoolean(String column) {
			return (Boolean) getObject(column);
		}
	}
}