package cellsociety.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This the class for console logging.
 *
 * @author Hsuan-Kai Liao
 */
public class Log {

    // Enum for log level
    private enum LogLevel {
        TRACE, INFO, WARN, ERROR
    }

    // Indentation values
    private static final int TIME_STRING_LENGTH = 10;
    private static final int LEVEL_STRING_LENGTH = 8;
    private static final int MAX_LINE_WIDTH = 150;

    // Time format
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ASCII color format
    private static final String RESTORE_COLOR = "\u001B[0m";
    private static final String TRACE_COLOR = "\u001B[37m";
    private static final String INFO_COLOR = "\u001B[32m";
    private static final String WARN_COLOR = "\u001B[33m";
    private static final String ERROR_COLOR = "\u001B[31m";

    /* MAIN LOG METHOD */

    private static void log(LogLevel level, String msg) {
        String formattedMessage = formatMessage(level, msg);
        printFormattedMessage(level, formattedMessage);
    }

    private static void log(LogLevel level, String msg, Throwable e) {
        if (e == null) {
            log(level, msg);
            return;
        }

        // Aggregate messages and exceptions
        String stackTraceString = Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n"));
        String splitLine = "=".repeat(MAX_LINE_WIDTH - TIME_STRING_LENGTH - LEVEL_STRING_LENGTH - 3);
        String formattedMessage = formatMessage(level, msg + "\n" + splitLine + "\n" + stackTraceString + "\n" + splitLine);

        printFormattedMessage(level, formattedMessage);
    }

    private static void printFormattedMessage(LogLevel level, String formattedMessage) {
        switch (level) {
            case TRACE:
                System.out.println(TRACE_COLOR + formattedMessage + RESTORE_COLOR);  // Light Gray
                break;
            case INFO:
                System.out.println(INFO_COLOR + formattedMessage + RESTORE_COLOR);  // Green
                break;
            case WARN:
                System.out.println(WARN_COLOR + formattedMessage + RESTORE_COLOR);  // Yellow
                break;
            case ERROR:
                // Format the error message itself
                System.err.println(ERROR_COLOR + formattedMessage + RESTORE_COLOR);
                break;
            default:
                System.out.println(formattedMessage);
        }
    }

    /* MESSAGE FORMATTER */

    private static String formatMessage(LogLevel level, String msg) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);

        // Ensure the level is padded to the right with spaces to match levelStringLength
        String levelString = String.format("%-" + LEVEL_STRING_LENGTH + "s", "<" + level + ">");  // Left-pad with spaces
        String prefix = String.format("[%s] %s| ", timestamp, levelString);
        return wrapMessage(msg, prefix);
    }

    private static String wrapMessage(String message, String prefix) {
        StringBuilder wrappedMessage = new StringBuilder();
        String[] lines = message.split("\n");
        boolean isFirstLine = true;

        for (String line : lines) {
            while (line.length() > MAX_LINE_WIDTH - prefix.length()) {
                // Find the last space within the maxLineWidth limit to avoid cutting words
                int wrapIndex = line.lastIndexOf(' ', MAX_LINE_WIDTH - prefix.length());
                if (wrapIndex == -1) { // If no space is found, break the line at maxLineWidth
                    wrapIndex = MAX_LINE_WIDTH - prefix.length();
                }

                // Add the prefix for the first line, or just spaces for subsequent lines
                if (isFirstLine) {
                    wrappedMessage.append(prefix).append(line, 0, wrapIndex).append("\n");
                    isFirstLine = false;
                } else {
                    wrappedMessage.append(" ".repeat(prefix.length() - 2)).append("| ").append(line, 0, wrapIndex).append("\n");
                }

                // Remove the processed part of the line
                line = line.substring(wrapIndex).trim();
            }

            if (isFirstLine) {
                wrappedMessage.append(prefix).append(line).append("\n");
                isFirstLine = false;
            } else {
                wrappedMessage.append(" ".repeat(prefix.length() - 2)).append("| ").append(line).append("\n");
            }
        }

        // Remove the last newline character
        if (!wrappedMessage.isEmpty()) {
            wrappedMessage.deleteCharAt(wrappedMessage.length() - 1);  // Remove the last '\n'
        }

        return wrappedMessage.toString();
    }

    /* LOG APIS */

    /**
     * Log a TRACE level message.
     * @param msg The message to log.
     */
    public static void trace(String msg) {
        log(LogLevel.TRACE, msg);
    }

    /**
     * Log a TRACE level message with format
     * @param format The message format
     * @param args The arguments
     */
    public static void trace(String format, Object... args) {
        log(LogLevel.TRACE, String.format(format, args));
    }

    /**
     * Log an INFO level message.
     * @param msg The message to log.
     */
    public static void info(String msg) {
        log(LogLevel.INFO, msg);
    }

    /**
     * Log an INFO level message with format
     * @param format The message format
     * @param args The arguments
     */
    public static void info(String format, Object... args) {
        log(LogLevel.INFO, String.format(format, args));
    }

    /**
     * Log a WARN level message.
     * @param msg The message to log.
     */
    public static void warn(String msg) {
        log(LogLevel.WARN, msg);
    }

    /**
     * Log a WARN level message with format
     * @param format The message format
     * @param args The arguments
     */
    public static void warn(String format, Object... args) {
        log(LogLevel.WARN, String.format(format, args));
    }

    /**
     * Log an ERROR level message.
     * @param msg The message to log.
     */
    public static void error(String msg) {
        log(LogLevel.ERROR, msg);
    }

    /**
     * Log an ERROR level message with format
     * @param format The message format
     * @param args The arguments
     */
    public static void error(String format, Object... args) {
        log(LogLevel.ERROR, String.format(format, args));
    }

    /**
     * Log a TRACE level message with exception details.
     * @param e The exception to log, along with its stack trace.
     * @param msg The message to log.
     */
    public static void trace(Throwable e, String msg) {
        log(LogLevel.TRACE, msg, e);
    }

    /**
     * Log an INFO level message with exception details.
     * @param e The exception to log, along with its stack trace.
     * @param msg The message to log.
     */
    public static void info(Throwable e, String msg) {
        log(LogLevel.INFO, msg, e);
    }

    /**
     * Log a WARN level message with exception details.
     * @param e The exception to log, along with its stack trace.
     * @param msg The message to log.
     */
    public static void warn(Throwable e, String msg) {
        log(LogLevel.WARN, msg, e);
    }

    /**
     * Log an ERROR level message with exception details.
     * @param e The exception to log, along with its stack trace.
     * @param msg The message to log.
     */
    public static void error(Throwable e, String msg) {
        log(LogLevel.ERROR, msg, e);
    }

    /**
     * Log a TRACE level message with exception details and format.
     * @param e The exception to log, along with its stack trace.
     * @param format The message format.
     * @param args The arguments.
     */
    public static void trace(Throwable e, String format, Object... args) {
        log(LogLevel.TRACE, String.format(format, args), e);
    }

    /**
     * Log an INFO level message with exception details and format.
     * @param e The exception to log, along with its stack trace.
     * @param format The message format.
     * @param args The arguments.
     */
    public static void info(Throwable e, String format, Object... args) {
        log(LogLevel.INFO, String.format(format, args), e);
    }

    /**
     * Log a WARN level message with exception details and format.
     * @param e The exception to log, along with its stack trace.
     * @param format The message format.
     * @param args The arguments.
     */
    public static void warn(Throwable e, String format, Object... args) {
        log(LogLevel.WARN, String.format(format, args), e);
    }

    /**
     * Log an ERROR level message with exception details and format.
     * @param e The exception to log, along with its stack trace.
     * @param format The message format.
     * @param args The arguments.
     */
    public static void error(Throwable e, String format, Object... args) {
        log(LogLevel.ERROR, String.format(format, args), e);
    }
}
