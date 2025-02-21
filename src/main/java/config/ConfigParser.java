package config;

import test.TestCase;

import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigParser {
    private String jdbcUrl; // JDBC URL for database connection
    private String username; // Database username
    private String password; // Database password
    private int testDuration; // Test duration in seconds
    private int max_random; // Maximum random number for test cases
    private boolean enableLogging;
    private Map<String, TestCase> testCases; // Map of test cases
    private Map<Integer, DatabaseConfig> databaseConfigs; // Map of database configurations
    private int databaseIndex;

    /**
     * Constructor that parses the configuration file.
     *
     * @param filePath Path to the configuration INI file.
     * @throws IOException If the file cannot be read.
     */
    public ConfigParser(String filePath) throws IOException {
        // Get the current working directory
        String path = System.getProperty("user.dir");
        File file = new File(path + "/" + filePath);
        System.out.println("Configuration file loading: " + path + "/" + filePath);

        // Load the INI file
        Ini ini = new Ini(file);

        // Log that the configuration file is loaded
       // System.out.println("Configuration file loaded: " + path + "/" + filePath);

        // Uncomment this block to print the content of the INI file
        /*
        for (String sectionName : ini.keySet()) {
            System.out.println("Section: " + sectionName);
            for (Map.Entry<String, String> entry : ini.get(sectionName).entrySet()) {
                System.out.println(entry.getKey() + "=" + entry.getValue());
            }
        }
        */

        // Parse the main configuration section
        // jdbcUrl = ini.get("main", "jdbcurl"); // Read the JDBC URL
        // username = ini.get("main", "username"); // Read the database username
        // password = ini.get("main", "password"); // Read the database password
        enableLogging = Boolean.parseBoolean(ini.get("main", "logging_sql"));
        // Read test duration, with a default value of 60 seconds
        String testDurationStr = ini.get("main", "test_duration");
        testDuration = (testDurationStr != null) ? Integer.parseInt(testDurationStr) : 60;

        // Read max random value, with a default value of 100
        String maxRandomStr = ini.get("main", "max_random");
        max_random = (maxRandomStr != null) ? Integer.parseInt(maxRandomStr) : 100;

        // Parse the database configurations
        databaseConfigs = new HashMap<>();
        int dbIndex = 1;
        while (ini.containsKey("database" + dbIndex)) {
            String sectionName = "database" + dbIndex;
            String jdbcUrl = ini.get(sectionName, "jdbcurl");
            String username = ini.get(sectionName, "username");
            String password = ini.get(sectionName, "password");
            databaseConfigs.put(dbIndex, new DatabaseConfig(jdbcUrl, username, password));
            dbIndex++;
        }
        databaseIndex = dbIndex - 1;

        // Parse the test cases from sections starting with "test"
        testCases = new HashMap<>();
        for (String sectionName : ini.keySet()) {
            if (sectionName.startsWith("test")) {
                String testName = sectionName;

                // Create a new TestCase object if it does not exist
                testCases.putIfAbsent(testName, new TestCase(testName));

                // Set the test case properties from the INI file
                testCases.get(testName).setCreateTableStatement(ini.get(testName, "create_sql"));
                testCases.get(testName).setSelectStatement(ini.get(testName, "select_sql"));
                testCases.get(testName).setIndexColumn(ini.get(testName, "index_col"));
                testCases.get(testName).setIsolationLevel(ini.get(testName, "iso"));
                testCases.get(testName).setMaxRandom(max_random);
            }
        }


    }

    // Getters for JDBC configuration
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getTestDuration() {
        return testDuration;
    }
    public int getMaxrandom() {
        return max_random;
    }
    public boolean getEnableLogging() {
        return enableLogging;
    }

    public Map<String, TestCase> getTestCases() {
        return testCases;
    }

    public Map<Integer, DatabaseConfig> getDatabaseConfigs() {
        return databaseConfigs;
    }
    public int getDatabaseIndex() {
        return databaseIndex;
    }

    public static class DatabaseConfig {
        private String jdbcUrl;
        private String username;
        private String password;

        public DatabaseConfig(String jdbcUrl, String username, String password) {
            this.jdbcUrl = jdbcUrl;
            this.username = username;
            this.password = password;
        }

        public String getJdbcUrl() {
            return jdbcUrl;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
