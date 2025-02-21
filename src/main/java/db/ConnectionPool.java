package db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectionPool {
    private static Map<Integer, HikariDataSource> dataSourceMap = new HashMap<>();

    public static void addDataSource(int dbIndex, String url, String user, String password, int initialSize, int maxSize) throws SQLException {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);
        hikariConfig.setMaximumPoolSize(maxSize);
        hikariConfig.setMinimumIdle(initialSize);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        dataSourceMap.put(dbIndex, dataSource);
    }

    public static synchronized Connection getConnection(int dbIndex) throws SQLException {
        HikariDataSource dataSource = dataSourceMap.get(dbIndex);
        if (dataSource == null) {
            throw new SQLException("No data source found for database index: " + dbIndex);
        }
        return dataSource.getConnection();
    }

    public static synchronized void releaseConnection(int dbIndex, Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized boolean isDataSourceAdded(int dbIndex) {
        return dataSourceMap.containsKey(dbIndex);
    }
}