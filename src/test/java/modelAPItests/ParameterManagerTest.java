package modelAPItests;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.logic.Logic;
import cellsociety.model.modelAPI.ParameterManager;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.State;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test suite for the ParameterManager class.
 */
public class ParameterManagerTest {

  /**
   * A simple enum to satisfy the generic type for Logic.
   */
  public enum FakeState implements State {
    DEFAULT;

    @Override
    public int getValue() {
      return ordinal();
    }
  }

  /**
   * A fake implementation of Logic used for testing.
   *
   * This FakeLogic provides public setter/getter pairs for a double ("speed") and a String ("name"),
   * and overrides getMinParam and getMaxParam for "speed". A no-op implementation is provided for updateSingleCell.
   */
  public static class FakeLogic extends Logic<FakeState> {
    private double speed;
    private String name;

    public FakeLogic(double speed, String name) {
      // Pass a dummy grid (null) and a dummy ParameterRecord since they are not used in these tests.
      super(null, new ParameterRecord(new HashMap<>(), new HashMap<>()));
      this.speed = speed;
      this.name = name;
    }

    public void setSpeed(double speed) {
      this.speed = speed;
    }

    public double getSpeed() {
      return this.speed;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getName() {
      return this.name;
    }

    @Override
    protected void updateSingleCell(Cell<FakeState> cell) {
      // No-op for testing.
    }

    @Override
    public double getMinParam(String paramName) {
      if ("speed".equals(paramName)) {
        return 0.0;
      }
      throw new IllegalArgumentException("No min bound for parameter: " + paramName);
    }

    @Override
    public double getMaxParam(String paramName) {
      if ("speed".equals(paramName)) {
        return 100.0;
      }
      throw new IllegalArgumentException("No max bound for parameter: " + paramName);
    }
  }

  /**
   * A FakeLogic subclass that introduces a setter without a corresponding getter.
   * This is used to test that resetParameters throws a NoSuchMethodException when the matching getter is missing.
   */
  public static class FakeLogicMissingGetter extends FakeLogic {
    public FakeLogicMissingGetter(double speed, String name) {
      super(speed, name);
    }

    // Setter without a corresponding getter.
    public void setBadParam(double value) {
      // Intentionally left blank.
    }
  }

  // Create a real ParameterRecord with mutable maps.
  private ParameterRecord parameterRecord;
  private FakeLogic fakeLogic;
  private ParameterManager parameterManager;

  @BeforeEach
  public void setUp() {
    parameterRecord = new ParameterRecord(new HashMap<>(), new HashMap<>());
    fakeLogic = new FakeLogic(10.0, "defaultName");
    parameterManager = new ParameterManager(fakeLogic, parameterRecord);
  }

  @Test
  public void testResetParameters_Success() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    // resetParameters should iterate over setters (for "speed" and "name")
    // and update the parameter record with default values obtained from their getters.
    parameterManager.resetParameters();
    Map<String, Double> doubleParams = parameterRecord.myDoubleParameters();
    Map<String, String> stringParams = parameterRecord.myStringParameters();
    assertEquals(10.0, doubleParams.get("speed"), "Expected 'speed' default value to be recorded");
    assertEquals("defaultName", stringParams.get("name"), "Expected 'name' default value to be recorded");
  }

  @Test
  public void testResetParameters_WithMissingGetter() {
    // Using FakeLogicMissingGetter which defines setBadParam without a corresponding getter.
    FakeLogicMissingGetter logicMissingGetter = new FakeLogicMissingGetter(20.0, "defaultMissing");
    ParameterManager pm = new ParameterManager(logicMissingGetter, parameterRecord);
    assertThrows(NoSuchMethodException.class, () -> pm.resetParameters(),
        "Expected NoSuchMethodException when a getter is missing for a setter");
  }

  @Test
  public void testResetParameters_GameLogicNull() {
    // ParameterManager with null gameLogic should throw an IllegalStateException when resetParameters is called.
    ParameterManager pm = new ParameterManager(null, parameterRecord);
    assertThrows(IllegalStateException.class, () -> pm.resetParameters(),
        "Expected IllegalStateException when gameLogic is not initialized");
  }

  @Test
  public void testGetDoubleParameterConsumer_Success() {
    // Retrieve a consumer for the "speed" parameter and update its value.
    Consumer<Double> speedConsumer = parameterManager.getDoubleParameterConsumer("speed");
    speedConsumer.accept(50.0);
    assertEquals(50.0, fakeLogic.getSpeed(), "Expected speed to be updated to 50.0");
  }

  @Test
  public void testGetDoubleParameterConsumer_NoSuchMethod() {
    // Request a double consumer for a non-existent parameter.
    assertThrows(NoSuchElementException.class, () -> {
      parameterManager.getDoubleParameterConsumer("nonExistentParam");
    }, "Expected NoSuchElementException for a non-existent double parameter setter");
  }

  @Test
  public void testGetStringParameterConsumer_Success() {
    // Retrieve a consumer for the "name" parameter and update its value.
    Consumer<String> nameConsumer = parameterManager.getStringParameterConsumer("name");
    nameConsumer.accept("newName");
    assertEquals("newName", fakeLogic.getName(), "Expected name to be updated to 'newName'");
  }

  @Test
  public void testGetStringParameterConsumer_NoSuchMethod() {
    // Request a string consumer for a non-existent parameter.
    assertThrows(NoSuchElementException.class, () -> {
      parameterManager.getStringParameterConsumer("nonExistentParam");
    }, "Expected NoSuchElementException for a non-existent string parameter setter");
  }

  @Test
  public void testGetParameterBounds_Success() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    // For the "speed" parameter, FakeLogic returns min=0.0 and max=100.0.
    double[] bounds = parameterManager.getParameterBounds("speed");
    assertNotNull(bounds, "Bounds array should not be null");
    assertEquals(0.0, bounds[0], "Expected minimum bound of 0.0 for 'speed'");
    assertEquals(100.0, bounds[1], "Expected maximum bound of 100.0 for 'speed'");
  }

  @Test
  public void testGetParameterBounds_GameLogicNull() {
    // With null gameLogic, getParameterBounds should throw an IllegalStateException.
    ParameterManager pm = new ParameterManager(null, parameterRecord);
    assertThrows(IllegalStateException.class, () -> pm.getParameterBounds("speed"),
        "Expected IllegalStateException when gameLogic is not initialized");
  }
}
