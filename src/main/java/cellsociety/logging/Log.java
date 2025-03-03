package cellsociety.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This the class for console logging.
 *
 * @author Hsuan-Kai Liao
 */
public class Log {

  // ASCII color format
  public static final String RESTORE_COLOR = "\u001B[0m";
  public static final String TRACE_COLOR = "\u001B[37m";
  public static final String INFO_COLOR = "\u001B[32m";
  public static final String WARN_COLOR = "\u001B[33m";
  public static final String ERROR_COLOR = "\u001B[31m";
  // Indentation values
  private static final int TIME_STRING_LENGTH = 10;
  private static final int LEVEL_STRING_LENGTH = 8;
  private static final int MAX_LINE_WIDTH = 150;
  // Time format
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
  // List to store the log messages
  private static final int MAX_LOG_SIZE = 1000;
  private static final List<String> LOG_MESSAGES = new ArrayList<>();
  private static final List<Consumer<String>> LOG_CONSUMERS = new ArrayList<>();

  private static void log(LogLevel level, String msg) {
    String formattedMessage = formatMessage(level, msg);
    printFormattedMessage(level, formattedMessage);
    storeLogMessage(formattedMessage);
  }

  /* MAIN LOG METHOD */

  private static void log(LogLevel level, String msg, Throwable e) {
    if (e == null) {
      log(level, msg);
      return;
    }

    // Aggregate messages and exceptions
    String stackTraceString = Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString)
        .collect(Collectors.joining("\n"));
    String splitLine = "=".repeat(MAX_LINE_WIDTH - TIME_STRING_LENGTH - LEVEL_STRING_LENGTH - 3);
    String formattedMessage = formatMessage(level,
        msg + "\n" + splitLine + "\n" + stackTraceString + "\n" + splitLine);

    printFormattedMessage(level, formattedMessage);
    storeLogMessage(formattedMessage);
  }

  private static void printFormattedMessage(LogLevel level, String formattedMessage) {
    switch (level) {
      case TRACE:
        notifyLogListeners(TRACE_COLOR + formattedMessage + "\n");
        break;
      case INFO:
        notifyLogListeners(INFO_COLOR + formattedMessage + "\n");
        break;
      case WARN:
        notifyLogListeners(WARN_COLOR + formattedMessage + "\n");
        break;
      case ERROR:
        // Format the error message itself
        notifyLogListeners(ERROR_COLOR + formattedMessage + "\n");
        break;
      default:
        notifyLogListeners(formattedMessage + "\n");
    }
  }

  private static String formatMessage(LogLevel level, String msg) {
    String timestamp = LocalDateTime.now().format(TIME_FORMATTER);

    // Ensure the level is padded to the right with spaces to match levelStringLength
    String levelString = String.format("%-" + LEVEL_STRING_LENGTH + "s",
        "<" + level + ">");  // Left-pad with spaces
    String prefix = String.format("[%s] %s| ", timestamp, levelString);
    return wrapMessage(msg, prefix);
  }

  /* MESSAGE FORMATTER */

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
          wrappedMessage.append(" ".repeat(prefix.length() - 2)).append("| ")
              .append(line, 0, wrapIndex).append("\n");
        }

        // Remove the processed part of the line
        line = line.substring(wrapIndex).trim();
      }

      if (isFirstLine) {
        wrappedMessage.append(prefix).append(line).append("\n");
        isFirstLine = false;
      } else {
        wrappedMessage.append(" ".repeat(prefix.length() - 2)).append("| ").append(line)
            .append("\n");
      }
    }

    // Remove the last newline character
    if (!wrappedMessage.isEmpty()) {
      wrappedMessage.deleteCharAt(wrappedMessage.length() - 1);  // Remove the last '\n'
    }

    return wrappedMessage.toString();
  }

  private static void storeLogMessage(String message) {
    if (LOG_MESSAGES.size() >= MAX_LOG_SIZE) {
      LOG_MESSAGES.removeFirst();
    }
    LOG_MESSAGES.add(message);
  }

  /**
   * Log a TRACE level message.
   *
   * @param msg The message to log.
   */
  public static void trace(String msg) {
    log(LogLevel.TRACE, msg);
  }

  /* LOG APIS */

  /**
   * Log a TRACE level message with format
   *
   * @param format The message format
   * @param args   The arguments
   */
  public static void trace(String format, Object... args) {
    log(LogLevel.TRACE, String.format(format, args));
  }

  /**
   * Log an INFO level message.
   *
   * @param msg The message to log.
   */
  public static void info(String msg) {
    log(LogLevel.INFO, msg);
  }

  /**
   * Log an INFO level message with format
   *
   * @param format The message format
   * @param args   The arguments
   */
  public static void info(String format, Object... args) {
    log(LogLevel.INFO, String.format(format, args));
  }

  /**
   * Log a WARN level message.
   *
   * @param msg The message to log.
   */
  public static void warn(String msg) {
    log(LogLevel.WARN, msg);
  }

  /**
   * Log a WARN level message with format
   *
   * @param format The message format
   * @param args   The arguments
   */
  public static void warn(String format, Object... args) {
    log(LogLevel.WARN, String.format(format, args));
  }

  /**
   * Log an ERROR level message.
   *
   * @param msg The message to log.
   */
  public static void error(String msg) {
    log(LogLevel.ERROR, msg);
  }

  /**
   * Log an ERROR level message with format
   *
   * @param format The message format
   * @param args   The arguments
   */
  public static void error(String format, Object... args) {
    log(LogLevel.ERROR, String.format(format, args));
  }

  /**
   * Log a TRACE level message with exception details.
   *
   * @param e   The exception to log, along with its stack trace.
   * @param msg The message to log.
   */
  public static void trace(Throwable e, String msg) {
    log(LogLevel.TRACE, msg, e);
  }

  /**
   * Log an INFO level message with exception details.
   *
   * @param e   The exception to log, along with its stack trace.
   * @param msg The message to log.
   */
  public static void info(Throwable e, String msg) {
    log(LogLevel.INFO, msg, e);
  }

  /**
   * Log a WARN level message with exception details.
   *
   * @param e   The exception to log, along with its stack trace.
   * @param msg The message to log.
   */
  public static void warn(Throwable e, String msg) {
    log(LogLevel.WARN, msg, e);
  }

  /**
   * Log an ERROR level message with exception details.
   *
   * @param e   The exception to log, along with its stack trace.
   * @param msg The message to log.
   */
  public static void error(Throwable e, String msg) {
    log(LogLevel.ERROR, msg, e);
  }

  /**
   * Log a TRACE level message with exception details and format.
   *
   * @param e      The exception to log, along with its stack trace.
   * @param format The message format.
   * @param args   The arguments.
   */
  public static void trace(Throwable e, String format, Object... args) {
    log(LogLevel.TRACE, String.format(format, args), e);
  }

  /**
   * Log an INFO level message with exception details and format.
   *
   * @param e      The exception to log, along with its stack trace.
   * @param format The message format.
   * @param args   The arguments.
   */
  public static void info(Throwable e, String format, Object... args) {
    log(LogLevel.INFO, String.format(format, args), e);
  }

  /**
   * Log a WARN level message with exception details and format.
   *
   * @param e      The exception to log, along with its stack trace.
   * @param format The message format.
   * @param args   The arguments.
   */
  public static void warn(Throwable e, String format, Object... args) {
    log(LogLevel.WARN, String.format(format, args), e);
  }

  /**
   * Log an ERROR level message with exception details and format.
   *
   * @param e      The exception to log, along with its stack trace.
   * @param format The message format.
   * @param args   The arguments.
   */
  public static void error(Throwable e, String format, Object... args) {
    log(LogLevel.ERROR, String.format(format, args), e);
  }

  /**
   * Add a log listener to receive log messages.
   *
   * @param consumer The consumer to receive log messages.
   */
  public static void addLogListener(Consumer<String> consumer) {
    LOG_CONSUMERS.add(consumer);
  }

  /* LISTENERS */

  /**
   * Remove a log listener from receiving log messages.
   *
   * @param consumer The consumer to remove.
   */
  public static void removeLogListener(Consumer<String> consumer) {
    LOG_CONSUMERS.remove(consumer);
  }

  /**
   * Clear all log listeners.
   */
  public static void clearLogListeners() {
    LOG_CONSUMERS.clear();
  }

  private static void notifyLogListeners(String message) {
    if (LOG_CONSUMERS.isEmpty()) {
      return;
    }
    LOG_CONSUMERS.forEach(consumer -> consumer.accept(message));
  }

  /**
   * Return an unmodifiable view of the logMessages list
   *
   * @return The unmodifiable list of log messages
   */
  public static List<String> getLogMessages() {
    return Collections.unmodifiableList(LOG_MESSAGES);
  }

  /* LOG OUTPUT */

  // Enum for log level
  private enum LogLevel {
    TRACE, INFO, WARN, ERROR
  }
}
