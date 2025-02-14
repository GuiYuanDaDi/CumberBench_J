import config.ConfigParser;
import test.TestCase;
import test.TestWorker;
import db.DatabaseConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class App {

    private static ConfigParser configParser; // Configuration parser
    private static AtomicInteger transactionCount = new AtomicInteger(0); // Transaction counter
    private static AtomicInteger queryCount = new AtomicInteger(0); // Query counter

    public static void main(String[] args) {
        try {

            CrocodileArt();

            // Initialize configuration parser
            configParser = new ConfigParser("config.ini");

            // Test database connection
            if (testDatabaseConnection()) {
                System.out.println("Database connection is successful");

                // Get test cases from the configuration
                Map<String, TestCase> testCases = configParser.getTestCases();
                
                // Create a fixed-size thread pool
                ExecutorService executorService = Executors.newFixedThreadPool(4);

                // Submit test tasks to the thread pool
                for (TestCase testCase : testCases.values()) {
                    TestWorker worker = new TestWorker(testCase, configParser, transactionCount, queryCount);
                    executorService.submit(worker);
                }

                // Start a separate thread to periodically print TPS and QPS
                startMonitoringThread(executorService);

                // Shut down the thread pool and wait for tasks to complete
                executorService.shutdown();
                // Sleep for the specified test duration
                try {
                    TimeUnit.SECONDS.sleep(configParser.getTestDuration());
                    // Print completion message
                    System.out.println("Testing completed");
                    System.exit(0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Preserve the interrupt status
                    System.out.println("Main thread was interrupted");
                }
            } else {
                // Exit if database connection fails
                System.out.println("Database connection failed");
                System.exit(1); // Exit program with a non-zero status to indicate an error
            }
        } catch (IOException e) {
            // Handle exceptions during configuration file reading
            System.out.println("Failed to read configuration file");
            System.exit(1);
        }
    }

    /**
     * Tests whether the database connection is successful.
     * @return true if the database connection is successful.
     */
    private static boolean testDatabaseConnection() {
        try (DatabaseConnection dbConnection = new DatabaseConnection(configParser)) {
            Connection connection = dbConnection.getConnection();
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace(); // Print error stack trace
            return false;
        }
    }

    /**
     * Starts a monitoring thread that periodically prints TPS (Transactions Per Second) and QPS (Queries Per Second).
     * @param executorService The thread pool
     */
    private static void startMonitoringThread(ExecutorService executorService) {
        new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(5); // Print every 5 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Preserve the interrupt status
                    break; // Exit loop if monitoring thread is interrupted
                }

                // Print TPS and QPS
                int tps = transactionCount.getAndSet(0) / 5;
                int qps = queryCount.getAndSet(0) / 5;
                System.out.println("Monitoring in progress *************** TPS: " + tps + " *************** QPS: " + qps);
            }
        }).start();
    }


    private static void CrocodileArt() {
        System.out.println("************************************");
        System.out.println("*                                  *");
        System.out.println("*      Welcome to CumberBench!     *");
        System.out.println("*                                  *");
        System.out.println("************************************");
        System.out.println("               .-._   _ _ _ _ _ _ _ _");
        System.out.println(" .-''-.__.-'00  '-' ' ' ' ' ' ' ' '-.'");
        System.out.println("'.___ '    .   .--_'-' '-' '-' _'-' '._");
        System.out.println(" V: V 'vv-'   '_   '.       .'  _..' '.'.");
        System.out.println("   '=.____.=_.--'   :_.__.__:_   '.   : :");
        System.out.println("           (((____.-'        '-.  /   : :");
        System.out.println("                            (((-\\ .' /");
        System.out.println("                          _____..'  .'");
        System.out.println("                         '-._____.-'");

 
    }

}
