package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String ERRLOG_FILE = "./error.log";
    private static final String LOG_FILE = "./test.log";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String message) {
        try (FileWriter fileWriter = new FileWriter(LOG_FILE, true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            printWriter.printf("[%s] %s%n", timestamp, message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logError(String message, Throwable throwable) {
        try (FileWriter fileWriter = new FileWriter(ERRLOG_FILE, true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            printWriter.printf("[%s] ERROR: %s%n", timestamp, message);
            throwable.printStackTrace(printWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



