package test;

import java.util.List;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestCase {
    private String createTableStatement; // SQL statement for creating the table
    private String selectStatement; // SQL statement for selecting data
    private String indexColumn; // Column used for indexing
    private String isolationLevel; // Database transaction isolation level
    private String testCaseName; // Name of the test case
    private List<AbstractMap.SimpleEntry<String, String>> columns; // List of table columns and their data types
    private String tableName; // Name of the table
    private int max_random; // Maximum random value for test data

    // Constructor to initialize the test case name
    public TestCase(String testCaseName) {
        this.testCaseName = testCaseName;
    }

    // Getter and setter for the create table SQL statement
    public String getCreateTableStatement() {
        return createTableStatement;
    }

    public void setCreateTableStatement(String createTableStatement) {
        this.createTableStatement = createTableStatement;
        parseColumns(createTableStatement); // Parse column information
        parseTableName(createTableStatement); // Parse table name
    }

    // Getter and setter for the select SQL statement
    public String getSelectStatement() {
        return selectStatement;
    }

    public void setSelectStatement(String selectStatement) {
        this.selectStatement = selectStatement;
    }

    // Getter and setter for the index column
    public String getIndexColumn() {
        return indexColumn;
    }

    public void setIndexColumn(String indexColumn) {
        this.indexColumn = indexColumn;
    }

    // Getter and setter for the isolation level
    public String getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(String isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    // Setter and getter for the max random value
    public void setMaxRandom(int max_random) {
        this.max_random = max_random;
    }

    public int getMaxRandom() {
        return max_random;
    }

    // Getter for the test case name
    public String getTestCaseName() {
        return testCaseName;
    }

    // Getter for the table name
    public String getTableName() {
        return tableName;
    }

    // Getter for the list of columns
    public List<AbstractMap.SimpleEntry<String, String>> getColumns() {
        return columns;
    }

    /**
     * Replaces commas within parentheses in a string with spaces.
     * This helps avoid splitting column definitions incorrectly.
     *
     * @param input The input string.
     * @return The modified string with commas replaced inside parentheses.
     */
    private static String replaceCommasInParentheses(String input) {
        String regex = "\\(([^()]+)\\)"; // Regex to match content within parentheses
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String content = matcher.group(1);
            String replacedContent = content.replace(",", " "); // Replace commas with spaces
            matcher.appendReplacement(result, "(" + replacedContent + ")");
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Parses column definitions from the CREATE TABLE SQL statement.
     *
     * @param createSql The SQL statement for creating a table.
     */
    private void parseColumns(String createSql) {
        columns = new ArrayList<>();
        String columnRegex = "\\s*(\\w+)\\s+(\\S(.*\\S)?)\\s*"; // Regex to capture column name and data type
        Pattern pattern = Pattern.compile(columnRegex);
        Matcher matcher = pattern.matcher(createSql);

        String columnsPart = createSql.substring(createSql.indexOf('(') + 1, createSql.lastIndexOf(')'));
        columnsPart = replaceCommasInParentheses(columnsPart);
        columnsPart = columnsPart.replaceAll("\\s+", " "); // Normalize spaces

        String[] columnsArray = columnsPart.split(",");
        for (String column : columnsArray) {
            matcher = pattern.matcher(column.trim());
            if (matcher.find()) {
                String columnName = matcher.group(1); // Extract column name
                String columnType = matcher.group(2); // Extract data type
                columns.add(new AbstractMap.SimpleEntry<>(columnName, columnType));
            }
        }

        // Uncomment to debug and print parsed columns
        // columns.forEach(entry -> System.out.println("Column: " + entry.getKey() + ", Type: " + entry.getValue()));
    }

    /**
     * Parses the table name from the CREATE TABLE SQL statement.
     *
     * @param createSql The SQL statement for creating a table.
     */
    private void parseTableName(String createSql) {
        String[] parts = createSql.split(" "); // Split the SQL statement by spaces
        tableName = parts[2]; // Table name is the third token in the statement
    }

    /**
     * Overrides the toString method to provide a detailed representation of the test case.
     *
     * @return A string representation of the test case.
     */
    @Override
    public String toString() {
        return "TestCase{" +
                "testCaseName='" + testCaseName + '\'' +
                ", createTableStatement='" + createTableStatement + '\'' +
                ", selectStatement='" + selectStatement + '\'' +
                ", indexColumn='" + indexColumn + '\'' +
                ", isolationLevel='" + isolationLevel + '\'' +
                ", tableName='" + tableName + '\'' +
                ", columns=" + columns +
                '}';
    }
}
