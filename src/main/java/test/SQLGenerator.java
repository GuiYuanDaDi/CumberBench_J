package test;

import java.util.Random;
import java.util.AbstractMap;

public class SQLGenerator {
    private final TestCase testCase; // The test case containing table and column information
    private final Random random; // Random number generator for generating random values

    public SQLGenerator(TestCase testCase) {
        this.testCase = testCase;
        this.random = new Random();
    }

    // Generates an SQL INSERT statement with random values
    public String generateInsertStatement() {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();

        for (AbstractMap.SimpleEntry<String, String> entry : testCase.getColumns()) {
            columns.append(entry.getKey()).append(", ");
            values.append(generateRandomValue(entry.getValue())).append(", ");
        }

        // Remove the trailing comma and space
        columns.setLength(columns.length() - 2);
        values.setLength(values.length() - 2);

        return String.format("INSERT INTO %s (%s) VALUES (%s);", testCase.getTableName(), columns.toString(), values.toString());
    }

    // Generates an SQL DELETE statement using the primary key
    public String generateDeleteStatement() {
        String primaryKey = testCase.getColumns().get(0).getKey(); // Assuming the first column is the primary key
        return String.format("DELETE FROM %s WHERE %s = %s;", testCase.getTableName(), primaryKey, generateRandomValue(testCase.getColumns().get(0).getValue()));
    }

    // Generates an SQL UPDATE statement with random values
    public String generateUpdateStatement() {
        StringBuilder setClause = new StringBuilder();
        String primaryKey = testCase.getColumns().get(0).getKey();  // Assuming the first column is the primary key

        for (AbstractMap.SimpleEntry<String, String> entry : testCase.getColumns()) {
            if (!entry.getKey().equals(primaryKey)) {
                setClause.append(entry.getKey()).append(" = ").append(generateRandomValue(entry.getValue())).append(", ");
            }
        }

        // Remove the trailing comma and space
        setClause.setLength(setClause.length() - 2);

        return String.format("UPDATE %s SET %s WHERE %s = %s;", testCase.getTableName(), setClause.toString(), primaryKey, generateRandomValue(testCase.getColumns().get(0).getValue()));
    }

    // Generates an SQL INSERT statement with a specific ID
    public String generateInsertStatementWithId(int id) {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();

        for (AbstractMap.SimpleEntry<String, String> entry : testCase.getColumns()) {
            columns.append(entry.getKey()).append(", ");
            if (entry.getKey().equalsIgnoreCase("id")) {
                values.append(id).append(", ");
            } else {
                values.append(generateRandomValue(entry.getValue())).append(", ");
            }
        }

        // Remove the trailing comma and space
        columns.setLength(columns.length() - 2);
        values.setLength(values.length() - 2);

        return String.format("INSERT INTO %s (%s) VALUES (%s);", testCase.getTableName(), columns.toString(), values.toString());
    }

    // Generates an SQL DELETE statement with a specific ID
    public String generateDeleteStatementWithId(int id) {
        return String.format("DELETE FROM %s WHERE id = %d;", testCase.getTableName(), id);
    }

    // Generates an SQL UPDATE statement with a specific old ID and new ID
    public String generateUpdateStatementWithId(int oldId, int newId) {
        StringBuilder setClause = new StringBuilder();

        for (AbstractMap.SimpleEntry<String, String> entry : testCase.getColumns()) {
            if (!entry.getKey().equalsIgnoreCase("id")) {
                setClause.append(entry.getKey()).append(" = ").append(generateRandomValue(entry.getValue())).append(", ");
            } else {
                setClause.append(entry.getKey()).append(" = ").append(newId).append(", ");
            }
        }

        // Remove the trailing comma and space
        setClause.setLength(setClause.length() - 2);

        return String.format("UPDATE %s SET %s WHERE id = %d;", testCase.getTableName(), setClause.toString(), oldId);
    }

    // Generates a random value based on the column type
    private String generateRandomValue(String columnType) {
        columnType = columnType.toUpperCase();

        if (columnType.startsWith("VARCHAR") || columnType.startsWith("CHAR") || columnType.startsWith("LVCHAR")) {
            String[] parts = columnType.replaceAll(".*\\((.*)\\).*", "$1").split(" ");
            int length = parts.length;
            if (length == 1) {
                return "'" + generateRandomString(Integer.parseInt(parts[0]), -1) + "'";
            }
            int min = Integer.parseInt(parts[0]);
            int max = Integer.parseInt(parts[1]);
            return "'" + generateRandomString(min, max) + "'";
        }

        if (columnType.startsWith("DECIMAL") || columnType.startsWith("NUMERIC")) {
            String[] parts = columnType.replaceAll(".*\\((.*)\\).*", "$1").split(" ");
            int precision = Integer.parseInt(parts[0]);
            int scale = Integer.parseInt(parts[1]);
            return generateRandomDecimal(precision, scale);
        }
        if (columnType.startsWith("TIMESTAMP")) {
            return "'" + generateRandomTimestamp() + "'";
        }
        switch (columnType) {
            case "INT":
                return String.valueOf(random.nextInt(testCase.getMaxRandom()));
            case "BIGINT":
                return String.valueOf(random.nextLong());
            case "FLOAT":
                return String.valueOf(random.nextFloat());
            case "DOUBLE":
                return String.valueOf(random.nextDouble());
            case "BOOLEAN":
                return String.valueOf(random.nextBoolean());
            case "TEXT":
                return "'" + generateRandomString(500, -1) + "'";
            case "DATE":
                return "'" + generateRandomDate() + "'";
            default:
                return "NULL";
        }
    }

    // Generates a random string of a given length or within a range
    private String generateRandomString(int min, int max) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        int length;
        if (max == -1) {
            length = min;
        } else {
            length = random.nextInt(max - min + 1) + min;
        }
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }
        return result.toString();
    }

    // Generates a random date between 2000 and 2023
    private String generateRandomDate() {
        int year = random.nextInt(2023 - 2000) + 2000;
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(28) + 1;
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    // Generates a random timestamp between 2000 and 2024
    private String generateRandomTimestamp() {
        int year = random.nextInt(2024 - 2000) + 2000;
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(28) + 1;
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);
        int millisecond = random.nextInt(1000);
        return String.format("%04d-%02d-%02d %02d:%02d:%02d.%03d", year, month, day, hour, minute, second, millisecond);
    }

    // Generates a random decimal value with the specified precision and scale
    private String generateRandomDecimal(int precision, int scale) {
        double value = random.nextDouble() * Math.pow(10, precision - scale);
        return String.format("%." + scale + "f", value);
    }
}