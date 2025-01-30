package db;

import config.ConfigParser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.sql.Statement;

public class DatabaseConnection implements AutoCloseable {
    private Connection connection;

    public DatabaseConnection(ConfigParser configParser) throws SQLException {
        
        connect(configParser.getJdbcUrl(), configParser.getUsername(), configParser.getPassword());
    }

    private void connect(String url, String user, String password) throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        connection = DriverManager.getConnection(url, properties);
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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