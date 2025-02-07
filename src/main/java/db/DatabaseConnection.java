package db;

import config.ConfigParser;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection implements AutoCloseable {
    private static ConnectionPool connectionPool;
    private Connection connection;

    public DatabaseConnection(ConfigParser configParser) throws SQLException {
        if (connectionPool == null) {
            synchronized (DatabaseConnection.class) {
                if (connectionPool == null) {
                    connectionPool = new ConnectionPool(
                        configParser.getJdbcUrl(),
                        configParser.getUsername(),
                        configParser.getPassword(),
                        20, // initial pool size
                        30 // max pool size
                    );
                }
            }
        }
        this.connection = connectionPool.getConnection();
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() {
        if (connection != null) {
            connectionPool.releaseConnection(connection);
        }
    }

    public void executeUpdate(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    public void executeQuery(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeQuery(sql);
        }
    }
}