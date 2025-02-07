package db;

import java.sql.Connection;

import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


public class ConnectionPool {
    private HikariDataSource dataSource;
    public ConnectionPool(String url, String user, String password, int initialSize, int maxSize) throws SQLException {

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
        this.dataSource = dataSource;
    }



    public synchronized Connection getConnection() throws SQLException {
        return  dataSource.getConnection();
    }

    public synchronized void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}