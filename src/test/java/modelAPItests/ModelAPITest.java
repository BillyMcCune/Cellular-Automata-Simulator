package modelAPItests;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.config.CellRecord;
import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigInfo.SimulationType;
import cellsociety.model.config.ConfigInfo.cellShapeType;
import cellsociety.model.config.ConfigInfo.gridEdgeType;
import cellsociety.model.config.ConfigInfo.neighborArrangementType;
import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.constants.EdgeType;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.logic.Logic;
import cellsociety.model.modelAPI.ModelApi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

/**
 * A comprehensive test suite for the ModelApi class.
 *
 * This suite creates dummy implementations of configuration (via a helper method to create a ConfigInfo record),
 * logic (FakeLogic), and grid objects so that we can test ModelApi’s behaviors.
 *
 * Test method names follow the naming convention:
 * [TestedMethod_StateUnderTest_ExpectedOutcome]
 *
 * For example:
 *  - updateSimulation_NullGridAndLogic_NoExceptionThrown()
 *  - getDoubleParameters_ValidConfig_ReturnsCorrectDoubleValues()
 *  - getCellColor_OutOfBoundsCoordinates_ReturnsNull()
 */
public class ModelAPITest {

  /**
   * A minimal enum implementing State for testing.
   */
  public enum FakeState implements cellsociety.model.data.states.State {
    STATE1, STATE2;

    @Override
    public int getValue() {
      return ordinal();
    }
  }

  /**
   * A dummy FakeLogic for testing parameter-related methods.
   * (Note: In your project you might already have a FakeLogic; here we define one for ModelApi tests.)
   */
  public static class FakeLogic extends Logic<FakeState> {
    private double speed;
    private String name;

    public FakeLogic(double speed, String name) {
      // Pass null for grid (not used) and an empty ParameterRecord.
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
   * Helper method that creates a simple test Grid.
   *
   * Expected outcome: A grid with given rows and columns that always returns the same cell.
   *
   * @param rows number of rows
   * @param cols number of columns
   * @param initialState initial state for the cell
   * @param properties extra properties to assign to the cell
   * @return a Grid instance for testing
   */
  private Grid<FakeState> createTestGrid(int rows, int cols, FakeState initialState, Map<String, Double> properties) {
    CellFactory<FakeState> factory = new CellFactory<>(FakeState.class);
    Cell<FakeState> cell = factory.createCell(initialState.ordinal());
    cell.setAllProperties(properties);
    return new Grid<FakeState>(new ArrayList<>(), factory, GridShape.SQUARE, NeighborType.MOORE, EdgeType.BASE) {
      @Override
      public int getNumRows() {
        return rows;
      }
      @Override
      public int getNumCols() {
        return cols;
      }
      @Override
      public Cell<FakeState> getCell(int row, int col) {
        return cell;
      }
    };
  }

  /**
   * Overloaded createTestGrid without extra properties.
   */
  private Grid<FakeState> createTestGrid(int rows, int cols, FakeState initialState) {
    return createTestGrid(rows, cols, initialState, new HashMap<>());
  }

  /**
   * Helper method to set a private field via reflection.
   */
  private void setPrivateField(Object target, String fieldName, Object value) {
    try {
      java.lang.reflect.Field field = ModelApi.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Helper method to get a private field via reflection.
   */
  private Object getPrivateField(Object target, String fieldName) {
    try {
      java.lang.reflect.Field field = ModelApi.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field.get(target);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Helper method to create a dummy ConfigInfo record.
   *
   * Expected outcome: A ConfigInfo record with provided grid configuration and parameters.
   */
  private ConfigInfo createFakeConfigInfo(ParameterRecord pr, List<List<CellRecord>> gridConfig) {
    Set<Integer> acceptedStates = new HashSet<>(Arrays.asList(0, 1));
    int width = gridConfig.isEmpty() ? 0 : gridConfig.get(0).size();
    int height = gridConfig.size();
    return new ConfigInfo(
        SimulationType.LIFE,
        cellShapeType.SQUARE,
        gridEdgeType.TORUS,
        neighborArrangementType.MOORE,
        1,
        "Dummy Simulation",
        "Dummy Author",
        "Dummy Description",
        width,
        height,
        1,
        gridConfig,
        pr,
        acceptedStates,
        "dummyFile.xml"
    );
  }

  // ==============================================
  // Tests for ModelApi methods
  // ==============================================

  @Test
  public void updateSimulation_NullGridAndLogic_NoExceptionThrown() {
    // Tested Method: updateSimulation()
    // State: Both grid and gameLogic are null.
    // Expected Outcome: Method returns without error.
    ModelApi api = new ModelApi();
    assertDoesNotThrow(() -> api.updateSimulation());
  }

  @Test
  public void getParameters_ValidConfig_ReturnsCorrectDoubleAndStringValues() {
    // Tested Methods: getDoubleParameters() and getStringParameters()
    // State: Config with a dummy ParameterRecord with specific values.
    // Expected Outcome: Correct double and string parameter values are returned.
    Map<String, Double> doubleParams = new HashMap<>();
    doubleParams.put("param1", 1.0);
    Map<String, String> stringParams = new HashMap<>();
    stringParams.put("param2", "value2");
    ParameterRecord pr = new ParameterRecord(doubleParams, stringParams);

    // Create a dummy 1x1 grid configuration.
    List<List<CellRecord>> gridConfig = new ArrayList<>();
    List<CellRecord> row = new ArrayList<>();
    row.add(new CellRecord(0, new HashMap<>()));
    gridConfig.add(row);

    ConfigInfo config = createFakeConfigInfo(pr, gridConfig);

    ModelApi api = new ModelApi();
    api.setConfigInfo(config);

    assertEquals(1.0, api.getDoubleParameters().get("param1"));
    assertEquals("value2", api.getStringParameters().get("param2"));
  }

  @Test
  public void getCellColor_OutOfBoundsCoordinates_ReturnsNull() {
    // Tested Method: getCellColor()
    // State: Coordinates (5, 5) requested on a 3x3 grid.
    // Expected Outcome: Method returns null for out-of-bound lookup.
    ModelApi api = new ModelApi();
    Grid<FakeState> grid = createTestGrid(3, 3, FakeState.STATE1);
    setPrivateField(api, "grid", grid);
    assertNull(api.getCellColor(5, 5, false));
  }

  @Test
  public void getCellStates_Valid2x2Grid_ReturnsExpectedStateValues() {
    // Tested Method: getCellStates()
    // State: A 2x2 grid where every cell is in FakeState.STATE1 (ordinal 0).
    // Expected Outcome: All returned state values should be 0.
    ModelApi api = new ModelApi();
    Grid<FakeState> grid = createTestGrid(2, 2, FakeState.STATE1);
    setPrivateField(api, "grid", grid);
    List<List<Integer>> states = api.getCellStates();
    for (List<Integer> row : states) {
      for (Integer val : row) {
        assertEquals(0, val);
      }
    }
  }

  @Test
  public void getCellProperties_ValidGrid_ReturnsExpectedProperties() {
    // Tested Method: getCellProperties()
    // State: A grid where each cell has the property "testProp" set to 3.14.
    // Expected Outcome: Every cell's property map contains "testProp" with value 3.14.
    ModelApi api = new ModelApi();
    Map<String, Double> props = new HashMap<>();
    props.put("testProp", 3.14);
    Grid<FakeState> grid = createTestGrid(2, 2, FakeState.STATE1, props);
    setPrivateField(api, "grid", grid);
    List<List<Map<String, Double>>> cellProps = api.getCellProperties();
    for (List<Map<String, Double>> row : cellProps) {
      for (Map<String, Double> map : row) {
        assertEquals(3.14, map.get("testProp"));
      }
    }
  }

  @Test
  public void resetParameters_ValidGameLogic_UpdatesParametersCorrectly() throws Exception {
    // Tested Method: resetParameters()
    // State: A valid FakeLogic instance with a dummy ParameterRecord.
    // Expected Outcome: Parameter consumers update the logic's "speed" and "name" parameters.
    ModelApi api = new ModelApi();
    FakeLogic logic = new FakeLogic(10.0, "default");
    setPrivateField(api, "gameLogic", logic);
    ParameterRecord pr = new ParameterRecord(new HashMap<>(), new HashMap<>());
    setPrivateField(api, "myParameterRecord", pr);
    assertDoesNotThrow(() -> api.resetParameters());
    Consumer<Double> speedConsumer = api.getDoubleParameterConsumer("speed");
    speedConsumer.accept(50.0);
    assertEquals(50.0, logic.getSpeed());
    Consumer<String> nameConsumer = api.getStringParameterConsumer("name");
    nameConsumer.accept("newName");
    assertEquals("newName", logic.getName());
    double[] bounds = api.getParameterBounds("speed");
    assertEquals(0.0, bounds[0]);
    assertEquals(100.0, bounds[1]);
  }

  @Test
  public void setStylePreferences_ValidInputs_UpdatesPreferencesCorrectly() throws Exception {
    // Tested Methods: setNeighborArrangement(), setEdgePolicy(), setCellShape(), setGridOutlinePreference()
    // State: A valid grid is set, and style preferences are updated.
    // Expected Outcome: Updated preferences are correctly applied and retrievable.
    ModelApi api = new ModelApi();
    Grid<FakeState> grid = createTestGrid(3, 3, FakeState.STATE1);
    setPrivateField(api, "grid", grid);
    api.setNeighborArrangement("NEUMANN");
    api.setEdgePolicy("TORUS");
    api.setCellShape("Hex");
    api.setGridOutlinePreference(false);
    assertFalse(api.getGridOutlinePreference());
    assertNotNull(api.getPossibleNeighborArrangements());
    assertNotNull(api.getPossibleEdgePolicies());
    assertNotNull(api.getPossibleCellShapes());
  }

  @Test
  public void resetGrid_NullConfigInfo_DoesNothing() throws ClassNotFoundException {
    // Tested Method: resetGrid()
    // State: configInfo is null.
    // Expected Outcome: resetGrid() does nothing and grid remains null.
    ModelApi api = new ModelApi();
    api.resetGrid(true);
    assertNull(getPrivateField(api, "grid"));
  }

  @Test
  public void resetModel_NullConfigInfo_ThrowsRuntimeException() {
    // Tested Method: resetModel()
    // State: configInfo is null.
    // Expected Outcome: Method throws RuntimeException.
    ModelApi api = new ModelApi();
    assertThrows(RuntimeException.class, () -> api.resetModel());
  }

  @Test
  public void setConfigInfo_ValidInput_UpdatesInternalConfigInfo() {
    // Tested Method: setConfigInfo()
    // State: A valid dummy ConfigInfo is provided.
    // Expected Outcome: Internal configInfo field is updated to the provided config.
    ModelApi api = new ModelApi();
    ParameterRecord pr = new ParameterRecord(new HashMap<>(), new HashMap<>());
    List<List<CellRecord>> gridConfig = new ArrayList<>();
    List<CellRecord> row = new ArrayList<>();
    row.add(new CellRecord(0, new HashMap<>()));
    gridConfig.add(row);
    ConfigInfo config = createFakeConfigInfo(pr, gridConfig);
    api.setConfigInfo(config);
    ConfigInfo internalConfig = (ConfigInfo) getPrivateField(api, "configInfo");
    assertEquals(config, internalConfig);
  }
}
