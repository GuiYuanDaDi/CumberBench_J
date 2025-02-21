package db;

import config.ConfigParser;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class DatabaseConnection implements AutoCloseable {
    private int dbIndex;
    private Connection connection;

    public DatabaseConnection(ConfigParser configParser, int dbIndex) throws SQLException {
        this.dbIndex = dbIndex;
        Map<Integer, ConfigParser.DatabaseConfig> databaseConfigs = configParser.getDatabaseConfigs();
        ConfigParser.DatabaseConfig dbConfig = databaseConfigs.get(dbIndex);
        if (dbConfig == null) {
            throw new SQLException("No configuration found for database index: " + dbIndex);
        }
        synchronized (ConnectionPool.class) {
            if (!ConnectionPool.isDataSourceAdded(dbIndex)) {
                ConnectionPool.addDataSource(
                    dbIndex,
                    dbConfig.getJdbcUrl(),
                    dbConfig.getUsername(),
                    dbConfig.getPassword(),
                    20, // initial pool size
                    30  // max pool size
                );
            }
        }
        this.connection = ConnectionPool.getConnection(dbIndex);
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() {
        if (connection != null) {
            ConnectionPool.releaseConnection(dbIndex, connection);
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