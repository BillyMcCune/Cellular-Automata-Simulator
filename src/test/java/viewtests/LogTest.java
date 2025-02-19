package viewtests;

import cellsociety.logging.Log;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;

public class LogTest {

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalSystemOut = System.out;
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalSystemErr = System.err;

  // Helper method to reset the output streams
  private void resetStreams() {
    System.setOut(originalSystemOut);
    System.setErr(originalSystemErr);
  }

  @Test
  public void testTraceMessage() {
    // Redirect System.out and System.err to capture output
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.trace("Test trace message");

    // Assert that the output contains the expected trace message
    assertTrue(outContent.toString().contains("Test trace message"));

    resetStreams();
  }

  @Test
  public void testTraceFormattedMessage() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.trace("Test %s message", "formatted");

    assertTrue(outContent.toString().contains("Test formatted message"));

    resetStreams();
  }

  @Test
  public void testInfoMessage() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.info("Test info message");

    assertTrue(outContent.toString().contains("Test info message"));

    resetStreams();
  }

  @Test
  public void testInfoFormattedMessage() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.info("Test %s info message", "formatted");

    assertTrue(outContent.toString().contains("Test formatted info message"));

    resetStreams();
  }

  @Test
  public void testWarnMessage() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.warn("Test warn message");

    assertTrue(outContent.toString().contains("Test warn message"));

    resetStreams();
  }

  @Test
  public void testWarnFormattedMessage() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.warn("Test %s warn message", "formatted");

    assertTrue(outContent.toString().contains("Test formatted warn message"));

    resetStreams();
  }

  @Test
  public void testErrorMessage() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.error("Test error message");

    assertTrue(outContent.toString().contains("Test error message"));

    resetStreams();
  }

  @Test
  public void testErrorFormattedMessage() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.error("Test %s error message", "formatted");

    assertTrue(outContent.toString().contains("Test formatted error message"));

    resetStreams();
  }

  @Test
  public void testTraceWithException() {
    Exception e = new Exception("Test exception");

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.trace(e, "Test trace with exception");

    assertTrue(outContent.toString().contains("Test trace with exception"));

    resetStreams();
  }

  @Test
  public void testInfoWithException() {
    Exception e = new Exception("Test exception");

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.info(e, "Test info with exception");

    assertTrue(outContent.toString().contains("Test info with exception"));

    resetStreams();
  }

  @Test
  public void testWarnWithException() {
    Exception e = new Exception("Test exception");

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.warn(e, "Test warn with exception");

    assertTrue(outContent.toString().contains("Test warn with exception"));

    resetStreams();
  }

  @Test
  public void testErrorWithException() {
    Exception e = new Exception("Test exception");

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.error(e, "Test error with exception");

    assertTrue(outContent.toString().contains("Test error with exception"));

    resetStreams();
  }

  @Test
  public void testTraceWithExceptionAndFormat() {
    Exception e = new Exception("Test exception");

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.trace(e, "Test %s trace with exception", "formatted");

    assertTrue(outContent.toString().contains("Test formatted trace with exception"));

    resetStreams();
  }

  @Test
  public void testInfoWithExceptionAndFormat() {
    Exception e = new Exception("Test exception");

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.info(e, "Test %s info with exception", "formatted");

    assertTrue(outContent.toString().contains("Test formatted info with exception"));

    resetStreams();
  }

  @Test
  public void testWarnWithExceptionAndFormat() {
    Exception e = new Exception("Test exception");

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.warn(e, "Test %s warn with exception", "formatted");

    assertTrue(outContent.toString().contains("Test formatted warn with exception"));

    resetStreams();
  }

  @Test
  public void testErrorWithExceptionAndFormat() {
    Exception e = new Exception("Test exception");

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.error(e, "Test %s error with exception", "formatted");

    assertTrue(outContent.toString().contains("Test formatted error with exception"));

    resetStreams();
  }
}
