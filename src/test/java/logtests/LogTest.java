package logtests;

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
  private void ResetStreams() {
    System.setOut(originalSystemOut);
    System.setErr(originalSystemErr);
  }

  @Test
  public void TraceMessage_StateIsGiven_ExpectedOutputIsPrinted() {
    // Redirect System.out and System.err to capture output
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.trace("Test trace message");

    // Assert that the output contains the expected trace message
    assertTrue(outContent.toString().contains("Test trace message"));

    ResetStreams();
  }

  @Test
  public void TraceFormattedMessage_StateIsGiven_ExpectedOutputIsPrinted() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.trace("Test %s message", "formatted");

    assertTrue(outContent.toString().contains("Test formatted message"));

    ResetStreams();
  }

  @Test
  public void InfoMessage_StateIsGiven_ExpectedOutputIsPrinted() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.info("Test info message");

    assertTrue(outContent.toString().contains("Test info message"));

    ResetStreams();
  }

  @Test
  public void InfoFormattedMessage_StateIsGiven_ExpectedOutputIsPrinted() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.info("Test %s info message", "formatted");

    assertTrue(outContent.toString().contains("Test formatted info message"));

    ResetStreams();
  }

  @Test
  public void WarnMessage_StateIsGiven_ExpectedOutputIsPrinted() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.warn("Test warn message");

    assertTrue(outContent.toString().contains("Test warn message"));

    ResetStreams();
  }

  @Test
  public void WarnFormattedMessage_StateIsGiven_ExpectedOutputIsPrinted() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.warn("Test %s warn message", "formatted");

    assertTrue(outContent.toString().contains("Test formatted warn message"));

    ResetStreams();
  }

  @Test
  public void ErrorMessage_StateIsGiven_ExpectedOutputIsPrinted() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.error("Test error message");

    assertTrue(outContent.toString().contains("Test error message"));

    ResetStreams();
  }

  @Test
  public void ErrorFormattedMessage_StateIsGiven_ExpectedOutputIsPrinted() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.error("Test %s error message", "formatted");

    assertTrue(outContent.toString().contains("Test formatted error message"));

    ResetStreams();
  }

  @Test
  public void TraceWithException_StateIsGiven_ExpectedOutputIsPrinted() {
    Exception e = new Exception("Test exception");

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.trace(e, "Test trace with exception");

    assertTrue(outContent.toString().contains("Test trace with exception"));

    ResetStreams();
  }

  @Test
  public void InfoWithException_StateIsGiven_ExpectedOutputIsPrinted() {
    Exception e = new Exception("Test exception");

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.info(e, "Test info with exception");

    assertTrue(outContent.toString().contains("Test info with exception"));

    ResetStreams();
  }

  @Test
  public void WarnWithException_StateIsGiven_ExpectedOutputIsPrinted() {
    Exception e = new Exception("Test exception");

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.warn(e, "Test warn with exception");

    assertTrue(outContent.toString().contains("Test warn with exception"));

    ResetStreams();
  }

  @Test
  public void ErrorWithException_StateIsGiven_ExpectedOutputIsPrinted() {
    Exception e = new Exception("Test exception");

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.error(e, "Test error with exception");

    assertTrue(outContent.toString().contains("Test error with exception"));

    ResetStreams();
  }

  @Test
  public void TraceWithExceptionAndFormat_StateIsGiven_ExpectedOutputIsPrinted() {
    Exception e = new Exception("Test exception");

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.trace(e, "Test %s trace with exception", "formatted");

    assertTrue(outContent.toString().contains("Test formatted trace with exception"));

    ResetStreams();
  }

  @Test
  public void InfoWithExceptionAndFormat_StateIsGiven_ExpectedOutputIsPrinted() {
    Exception e = new Exception("Test exception");

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.info(e, "Test %s info with exception", "formatted");

    assertTrue(outContent.toString().contains("Test formatted info with exception"));

    ResetStreams();
  }

  @Test
  public void WarnWithExceptionAndFormat_StateIsGiven_ExpectedOutputIsPrinted() {
    Exception e = new Exception("Test exception");

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.warn(e, "Test %s warn with exception", "formatted");

    assertTrue(outContent.toString().contains("Test formatted warn with exception"));

    ResetStreams();
  }

  @Test
  public void ErrorWithExceptionAndFormat_StateIsGiven_ExpectedOutputIsPrinted() {
    Exception e = new Exception("Test exception");

    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Log.error(e, "Test %s error with exception", "formatted");

    assertTrue(outContent.toString().contains("Test formatted error with exception"));

    ResetStreams();
  }
}
