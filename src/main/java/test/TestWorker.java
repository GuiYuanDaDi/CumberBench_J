package test;

import db.DatabaseConnection;
import util.Logger;
import util.ControlledFileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.IOException;

import java.util.*;

import config.ConfigParser;

public class TestWorker implements Runnable {
    private final TestCase testCase; // The test case containing table and column information
    private final ConfigParser configParser; // Configuration parser for database connection
    private final AtomicInteger transactionCount; // Counter for the number of transactions executed
    private final AtomicInteger queryCount; // Counter for the number of queries executed

    public TestWorker(TestCase testCase, ConfigParser configParser, AtomicInteger transactionCount,
            AtomicInteger queryCount) {
        this.testCase = testCase;
        this.configParser = configParser;
        this.transactionCount = transactionCount;
        this.queryCount = queryCount;
    }

    // Returns the current time as a formatted string
    public String getCurrentTimeString() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return now.format(formatter);
    }

    @Override
    public void run() {
        // Connect to the database, create the table and indexes based on the TestCase, then disconnect
        try (DatabaseConnection dbConnection = new DatabaseConnection(configParser)) {
            Connection connection = dbConnection.getConnection();
            try (Statement statement = connection.createStatement()) {
                String dropsql = "DROP TABLE IF EXISTS " + testCase.getTableName() + ";";
                statement.execute(dropsql);
                statement.execute(testCase.getCreateTableStatement());
                Logger.log(String.format("Test case: %s, Table created successfully", testCase.getTestCaseName()));

                String[] indexColumns = testCase.getIndexColumn().split(",");
                for (String indexColumn : indexColumns) {
                    String createIndexStatement = String.format("CREATE INDEX idx_%s%s ON %s (%s);",
                            testCase.getTableName(), indexColumn.trim(), testCase.getTableName(), indexColumn.trim());
                    statement.execute(createIndexStatement);
                    Logger.log(String.format("Test case: %s, Index %s created successfully", testCase.getTestCaseName(), indexColumn.trim()));
                }
                dbConnection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Create four threads, each connecting to the database and performing random CRUD operations
        for (int i = 0; i < 4; i++) {
            final int threadId = i;
            new Thread(() -> performDatabaseOperations(threadId)).start();
        }
    }

    private void performDatabaseOperations(int id) {
        String fileName = "testsql/" + testCase.getTestCaseName() + "_thread_" + id + ".sql";
        ControlledFileWriter fileWriter;
        try {
            fileWriter = new ControlledFileWriter(fileName, configParser.getEnableLogging());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (testCase.getIsolationLevel().equals("READ_COMMITTED")) {
            performReadCommittedOperations(id, fileWriter);
        } else if (testCase.getIsolationLevel().equals("REPEATABLE_READ")) {
            performRepeatbleOperations(id, fileWriter);
        }

        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void performRepeatbleOperations(int id, ControlledFileWriter fileWriter) {
        try (DatabaseConnection dbConnection = new DatabaseConnection(configParser)) {
            Connection connection = dbConnection.getConnection();
            connection.setTransactionIsolation(getIsolationLevel(testCase.getIsolationLevel()));
            connection.setAutoCommit(false);
            int threadId = id;

            // Prepopulate the table with 500 random insert statements
            try (Statement statement = connection.createStatement()) {
                for (int i = 0; i < 500; i++) {
                    String sql = new SQLGenerator(testCase).generateInsertStatement();
                    fileWriter.write(sql + "--" + this.getCurrentTimeString() + "\n");
                    statement.executeUpdate(sql);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            }

            while (true) {
                // Infinite loop to perform random CRUD operations within a transaction
                try (Statement statement = connection.createStatement()) {
                    if (id % 2 == 0) {
                        // Read-only thread, only performs read transactions
                        validateDataConsistency(statement);
                        connection.commit();
                        transactionCount.incrementAndGet(); // Increment transaction count
                    } else {
                        // Mixed read-write thread
                        fileWriter.write("begin;" + "\n");
                        performRandomOperations(statement, fileWriter, threadId);
                        validateDataConsistency(statement);
                        if (Math.random() < 0.7) {
                            connection.commit();
                            fileWriter.write("commit;" + "\n");
                            transactionCount.incrementAndGet(); // Increment transaction count
                        } else {
                            connection.rollback();
                            transactionCount.incrementAndGet(); // Increment transaction count
                            fileWriter.write("rollback;" + "\n");
                        }
                    }
                } catch (SQLException e) {
                    if (e.getSQLState().equals("25P02") || e.getSQLState().equals("40P01")) {
                        connection.rollback();
                        transactionCount.incrementAndGet();
                        fileWriter.write("rollback;" + "\n");
                    } else {
                        throw e;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getIsolationLevel(String isolationLevel) {
        switch (isolationLevel) {
            case "READ_COMMITTED":
                return Connection.TRANSACTION_READ_COMMITTED;
            case "REPEATABLE_READ":
                return Connection.TRANSACTION_REPEATABLE_READ;
            case "SERIALIZABLE":
                return Connection.TRANSACTION_SERIALIZABLE;
            default:
                return Connection.TRANSACTION_READ_COMMITTED;
        }
    }

    private void performRandomOperations(Statement statement, ControlledFileWriter fileWriter, int id) throws SQLException {
        // Generates a random number of random CRUD operations
        for (int i = 0; i < 10; i++) {
            int operation = (int) (Math.random() * 3);
            String sql;
            switch (operation) {
                case 0:
                    sql = new SQLGenerator(testCase).generateInsertStatement();
                    break;
                case 1:
                    sql = new SQLGenerator(testCase).generateDeleteStatement();
                    break;
                default:
                    sql = new SQLGenerator(testCase).generateUpdateStatement();
                    break;
            }

            try {
                fileWriter.write(sql + "--" + this.getCurrentTimeString() + "\n");
                statement.executeUpdate(sql);
                queryCount.incrementAndGet();
            } catch (SQLException e) {
                if (e.getSQLState().equals("40001") || e.getSQLState().equals("40P01")) {
                    Logger.log(String.format("Test case: %s, %s lock conflict, continuing execution", testCase.getTestCaseName(), e));

                } else {
                    throw e;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void validateDataConsistency(Statement statement) throws SQLException {
        // Executes the SELECT statement from the test case and limits the result set to 1000 rows
        int ccount = 0;
        int ccount1 = 0;
        statement.setMaxRows(10000);
        statement.execute(testCase.getSelectStatement());
        String c_sql = "select count(*) from " + testCase.getTableName() + ";";
        ResultSet resultSet1 = statement.getResultSet();
        queryCount.incrementAndGet();
        List<String> resInt1 = new ArrayList<>();
        List<String> resStr1 = new ArrayList<>();
        int count1 = 0;
        int count2 = 0;

        while (resultSet1.next()) {
            resInt1.add(resultSet1.getString(1));
            resStr1.add(resultSet1.getString(2));
            count1++;
        }
        statement.execute(c_sql);
        ResultSet resultSet = statement.getResultSet();
        while (resultSet.next()) {
            ccount = resultSet.getInt(1);
        }

        for (int i = 0; i < 3; i++) {
            statement.setMaxRows(10000);
            statement.execute(testCase.getSelectStatement());
            queryCount.incrementAndGet();
            ResultSet resultSet2 = statement.getResultSet();
            List<String> resInt2 = new ArrayList<>();
            List<String> resStr2 = new ArrayList<>();
            count2 = 0;
            while (resultSet2.next()) {
                resInt2.add(resultSet2.getString(1));
                resStr2.add(resultSet2.getString(2));
                count2++;
            }
            for (int j = 0; j < resStr1.size(); j++) {
                if (!resStr1.get(j).equals(resStr2.get(j))) {
                    System.out.println("Test discovered: " + testCase.getTestCaseName() + " data comparison mismatch");
                    Logger.logError(String.format(
                            "Test case: %s, Data inconsistency found. first_select: %s, next_select: %s, next_select: %d, Count2: %d",
                            testCase.getTestCaseName(), resStr1.get(j), resStr2.get(j), count1, count2),
                            new Throwable("Data inconsistency error"));
                    System.exit(0);
                }
            }
            statement.execute(c_sql);
            ResultSet resultSet3 = statement.getResultSet();
            while (resultSet3.next()) {
                ccount1 = resultSet3.getInt(1);
            }
            if (ccount != ccount1) {
                System.out.println("Test discovered: " + testCase.getTestCaseName() + " count (*) comparison mismatch");
                Logger.logError(String.format(
                        "Test case: %s, conut (*). first_count: %d, next_count: %d", testCase.getTestCaseName(),
                        ccount, ccount1), new Throwable("Data inconsistency error"));
                System.exit(0);
            }
            if (Math.random() < 0.01) {
                Logger.log(String.format("Test case: %s, RR passed in one check", testCase.getTestCaseName()));

            }
        }
    }

    private void performReadCommittedOperations(int thid, ControlledFileWriter fileWriter) {
        try (DatabaseConnection dbConnection = new DatabaseConnection(configParser)) {
            Connection connection = dbConnection.getConnection();
            connection.setTransactionIsolation(getIsolationLevel(testCase.getIsolationLevel()));
            connection.setAutoCommit(false);
            int threadId = thid + 1;
            Queue<Item> out_data = new LinkedList<>();
            Queue<Item> in_data = new LinkedList<>();
            Queue<Item> un_commit = new LinkedList<>();
            boolean rollback = false;

            for (int i = (100 * (threadId - 1)); i <= (100 * threadId - 1); i++) {
                Item item = new Item(i, 4);
                out_data.add(item);
            }
            Collections.shuffle((List<?>) out_data);

            while (true) {
                un_commit.clear();
                fileWriter.write("begin;" + "--" + this.getCurrentTimeString() + "\n");

                for (int j = 0; j < (int) (Math.random() * 10); j++) {
                    if (rollback) {
                        break;
                    }
                    int operation = (int) (Math.random() * 3);

                    if (operation == 0) {
                        if (out_data.isEmpty()) {
                            continue;
                        }
                        int id = out_data.poll().id;
                        String sql = new SQLGenerator(testCase).generateInsertStatementWithId(id);
                        fileWriter.write(sql + "--" + this.getCurrentTimeString() + "\n");
                        try (Statement statement = connection.createStatement()) {
                            statement.executeUpdate(sql);
                            queryCount.incrementAndGet();
                            Item item = new Item(id, 0);
                            un_commit.add(item);
                        } catch (SQLException e) {
                            if (e.getSQLState().equals("40001") || e.getSQLState().equals("40P01")) {
                                rollback = true;
                                out_data.add(new Item(id, 4));
                                break;
                            } else {
                                throw e;
                            }
                        }
                    }
                    if (operation == 1) {
                        if (in_data.isEmpty() || out_data.isEmpty()) {
                            continue;
                        }
                        int id = in_data.poll().id;
                        String sql = new SQLGenerator(testCase).generateDeleteStatementWithId(id);
                        fileWriter.write(sql + "--" + this.getCurrentTimeString() + "\n");
                        try (Statement statement = connection.createStatement()) {
                            statement.executeUpdate(sql);
                            queryCount.incrementAndGet();
                            Item item = new Item(id, 1);
                            un_commit.add(item);
                        } catch (SQLException e) {
                            if (e.getSQLState().equals("40001") || e.getSQLState().equals("40P01")) {
                                rollback = true;
                                in_data.add(new Item(id, 4));
                                break;
                            } else {
                                throw e;
                            }
                        }
                    }
                    if (operation == 2) {
                        if (in_data.isEmpty()) {
                            continue;
                        }
                        int oldId = in_data.poll().id;
                        int newId = out_data.poll().id;

                        String sql = new SQLGenerator(testCase).generateUpdateStatementWithId(oldId, newId);
                        fileWriter.write(sql + "--" + this.getCurrentTimeString() + "\n");
                        try (Statement statement = connection.createStatement()) {
                            statement.executeUpdate(sql);
                            queryCount.incrementAndGet();
                            Item newItem = new Item(newId, 0);
                            Item oldItem = new Item(oldId, 1);
                            un_commit.add(newItem);
                            un_commit.add(oldItem);
                        } catch (SQLException e) {
                            if (e.getSQLState().equals("40001") || e.getSQLState().equals("40P01")) {
                                rollback = true;
                                in_data.add(new Item(oldId, 4));
                                out_data.add(new Item(newId, 1));
                                break;
                            } else {
                                throw e;
                            }
                        }
                    }
                }

                List<Integer> visibleIds = new ArrayList<>();
                List<Integer> invisibleIds = new ArrayList<>();
                for (Item item : un_commit) {
                    if (item.opt == 1) {
                        visibleIds.add(item.id);
                    } else if (item.opt == 0) {
                        invisibleIds.add(item.id);
                    }
                }
                executeAndCheckDataConsistency(connection, threadId, visibleIds, invisibleIds, in_data, false);

                List<Integer> finalVisibleIds = new ArrayList<>();
                List<Integer> finalInvisibleIds = new ArrayList<>();

                if (rollback || Math.random() < 0.3) {
                    connection.rollback();
                    transactionCount.incrementAndGet();
                    fileWriter.write("rollback;" + "--" + this.getCurrentTimeString() + "\n");
                    for (Integer id : visibleIds) {
                        finalVisibleIds.add(id);
                    }
                    for (Integer id : invisibleIds) {
                        finalInvisibleIds.add(id);
                    }
                } else {
                    connection.commit();
                    transactionCount.incrementAndGet();
                    fileWriter.write("commit;" + "--" + this.getCurrentTimeString() + "\n");
                    for (Integer id : invisibleIds) {
                        finalVisibleIds.add(id);
                    }
                    for (Integer id : visibleIds) {
                        finalInvisibleIds.add(id);
                    }
                }

                executeAndCheckDataConsistency(connection, threadId, finalVisibleIds, finalInvisibleIds, in_data, true);
                for (Integer id : finalVisibleIds) {
                    in_data.add(new Item(id, 0));
                }
                for (Integer id : finalInvisibleIds) {
                    out_data.add(new Item(id, 4));
                }

                if (Math.random() < 0.01) {
                    Logger.log(String.format("Test case: %s, RC passed in one check", testCase.getTestCaseName()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkDataConsistency(Connection connection, int BaseId, List<Integer> visibleIds,
            List<Integer> invisibleIds, Queue<Item> in_data, boolean aftercommit) {
        boolean myself = true;
        DatabaseConnection dbConnection = null;
        if (connection == null) {
            try {
                dbConnection = new DatabaseConnection(configParser);
                connection = dbConnection.getConnection();
                connection.setTransactionIsolation(getIsolationLevel(testCase.getIsolationLevel()));
                connection.setAutoCommit(false);
                myself = false;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        List<Integer> tmpVisibleIds = new ArrayList<>();
        List<Integer> tmpInvisibleIds = new ArrayList<>();
        for (Item item : in_data) {
            tmpVisibleIds.add(item.id);
        }
        for (Integer id : visibleIds) {
            tmpVisibleIds.add(id);
        }
        for (Integer id : invisibleIds) {
            tmpInvisibleIds.add(id);
        }

        Collections.sort(tmpVisibleIds);
        Collections.sort(tmpInvisibleIds);
        int min = 100 * (BaseId - 1);
        int max = 100 * BaseId - 1;

        String sql = "select * from " + testCase.getTableName() + " where id >= " + min + " and id <= " + max
                + " order by id asc";

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            ResultSet resultSet = statement.getResultSet();
            queryCount.incrementAndGet();
            List<Integer> result = new ArrayList<>();

            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                result.add(id);
            }
            for (int i = 0; i < result.size(); i++) {
                if (i < tmpVisibleIds.size()) {
                    if ((int) result.get(i) != (int) tmpVisibleIds.get(i)) {
                        System.out.println("Test discovered: " + testCase.getTestCaseName() + " RC data comparison mismatch");
                        System.out.println(result.get(i) + " " + tmpVisibleIds.get(i));
                        String errString1;
                        if (myself) {
                            errString1 = "Write transaction RC read data does not meet expectations";
                        } else {
                            errString1 = "Read transaction RC read data does not meet expectations";
                        }

                        String errString2;
                        if (aftercommit) {
                            errString2 = "After commit";
                        } else {
                            errString2 = "Before commit";
                        }

                        Logger.logError(String.format(
                                "Test case: %s, Data inconsistency found.%s, %s, result: %s, visibleIds: %s, invisibleIds: %s",
                                testCase.getTestCaseName(), errString2, errString1, result, tmpVisibleIds, tmpInvisibleIds),
                                new Throwable("Data inconsistency error"));
                        System.exit(0);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!myself) { 
            dbConnection.close();
        }
    }

    private void waitForThreadsToFinish(Thread... threads) {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void executeAndCheckDataConsistency(Connection connection, int BaseId, List<Integer> visibleIds,
            List<Integer> invisibleIds, Queue<Item> in_data, boolean aftercommit) {
        if (aftercommit) {
            checkDataConsistency(connection, BaseId, visibleIds, invisibleIds, in_data, true);
        } else {
            checkDataConsistency(connection, BaseId, invisibleIds, visibleIds, in_data, false);
        }
        Thread th1 = new Thread(() -> checkDataConsistency(null, BaseId, visibleIds, invisibleIds, in_data, aftercommit));
        Thread th2 = new Thread(() -> checkDataConsistency(null, BaseId, visibleIds, invisibleIds, in_data, aftercommit));

        th1.start();
        th2.start();

        waitForThreadsToFinish(th1, th2);
    }
}