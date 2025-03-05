package configAPItests;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.config.CellRecord;
import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigInfo.SimulationType;
import cellsociety.model.config.ConfigInfo.cellShapeType;
import cellsociety.model.config.ConfigInfo.gridEdgeType;
import cellsociety.model.config.ConfigInfo.neighborArrangementType;
import cellsociety.model.config.ParameterRecord;
import cellsociety.model.configAPI.configAPI;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.constants.EdgeType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.data.states.State;
import cellsociety.model.modelAPI.ModelApi;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

/**
 * JUnit tests for configAPI.
 *
 * This test document uses the following rules:
 *
 * - Each test method is annotated with @Test.
 * - The method names follow the [MethodName_StateUnderTest_ExpectedBehavior] format.
 *   For example: getAcceptedStates_NullConfig_ThrowsNullPointerException().
 * - Variable names in tests reflect the input and expected state (e.g., ZERO_DIM_CONFIG, SINGLE_STATE_CONFIG).
 * - The ZOMBIES acronym is used to cover scenarios: Zero, One, Many, Boundary, Invalid, Exception, Stress.
 *
 * If any test fails:
 * 1. Comment out the buggy code.
 * 2. Write a comment indicating the cause of the error.
 * 3. Provide the corrected code.
 * 4. Re-run the tests to verify they pass.
 */
public class ConfigAPITest {

  /**
   * TestState is used instead of a dummy state. It implements the State interface.
   */
  private enum TestState implements State {
    ZERO(0), ONE(1);

    private final int value;
    TestState(int value) { this.value = value; }
    @Override
    public int getValue() { return value; }
    public static TestState fromInt(Class<TestState> enumClass, int value) {
      for (TestState s : enumClass.getEnumConstants()) {
        if (s.getValue() == value) {
          return s;
        }
      }
      throw new IllegalArgumentException("Invalid TestState value: " + value);
    }
  }

  /**
   * DummyCellFactory creates cells with TestState.
   */
  private static class DummyCellFactory extends CellFactory<TestState> {
    public DummyCellFactory() {
      super(TestState.class);
    }
  }

  /**
   * DummyNeighborCalculator provides neighbor directions.
   */
  public static class DummyNeighborCalculator extends NeighborCalculator<TestState> {
    public DummyNeighborCalculator() {
      super(GridShape.SQUARE, NeighborType.MOORE, EdgeType.BASE);
    }
  }

  /**
   * DummyConfigReader overrides readConfig to return a dummy configuration
   * without performing file I/O.
   */
  private static class DummyConfigReader extends cellsociety.model.config.ConfigReader {
    @Override
    public ConfigInfo readConfig(String fileName)
        throws ParserConfigurationException, IOException, SAXException {
      // Return a dummy configuration with standard dimensions.
      return createStaticDummyConfigInfo();
    }
  }

  /**
   * Creates a dummy ConfigInfo with standard dimensions (width=10, height=20).
   * Expected outcome: A valid ConfigInfo with accepted states {0,1}.
   */
  private static ConfigInfo createStaticDummyConfigInfo() {
    int width = 10;
    int height = 20;
    List<List<CellRecord>> gridData = new ArrayList<>();
    for (int i = 0; i < height; i++) {
      List<CellRecord> row = new ArrayList<>();
      for (int j = 0; j < width; j++) {
        row.add(new CellRecord(0, new HashMap<>()));
      }
      gridData.add(row);
    }
    ParameterRecord parameters = new ParameterRecord(new HashMap<>(), new HashMap<>());
    Set<Integer> acceptedStates = new HashSet<>(Arrays.asList(0, 1));
    return new ConfigInfo(
        SimulationType.LIFE,
        cellShapeType.SQUARE,
        gridEdgeType.BASE,
        neighborArrangementType.MOORE,
        1,
        "Dummy Simulation",
        "Dummy Author",
        "Dummy Description",
        width,
        height,
        1,
        gridData,
        parameters,
        acceptedStates,
        "dummyFile.xml"
    );
  }

  /**
   * DummyModelApi extends ModelApi.
   * It creates a dummy grid (2x2) and provides dummy implementations for getCellStates() and getCellProperties().
   * It also overrides setConfigInfo to capture the config for verification.
   */
  @Nested
  public class DummyModelApi extends ModelApi {

    public ConfigInfo configInfoDummy; // For later verification
    private Grid<TestState> grid;
    private Map<String, Double> doubleParams;
    private Map<String, String> stringParams;

    public DummyModelApi() {
      // Create a dummy grid of size 2x2; each cell's state is set to 1.
      List<List<CellRecord>> raw = new ArrayList<>();
      for (int i = 0; i < 2; i++) {
        List<CellRecord> row = new ArrayList<>();
        for (int j = 0; j < 2; j++) {
          row.add(new CellRecord(1, new HashMap<>()));
        }
        raw.add(row);
      }
      grid = new Grid<>(raw, new DummyCellFactory(), GridShape.SQUARE, NeighborType.MOORE, EdgeType.BASE);
      doubleParams = new HashMap<>();
      doubleParams.put("param1", 1.0);
      stringParams = new HashMap<>();
      stringParams.put("param2", "value2");
    }

    @Override
    public void setConfigInfo(ConfigInfo configInfo) {
      // Expected outcome: Provided configInfo is stored in configInfoDummy.
      this.configInfoDummy = configInfo;
    }

    @Override
    public Map<String, Double> getDoubleParameters() {
      return doubleParams;
    }

    @Override
    public Map<String, String> getStringParameters() {
      return stringParams;
    }

    /**
     * Returns a 2x2 grid of integers (all 1's).
     * Expected outcome: A 2x2 matrix where every element is 1.
     */
    @Override
    public List<List<Integer>> getCellStates() {
      List<List<Integer>> states = new ArrayList<>();
      for (int i = 0; i < 2; i++) {
        List<Integer> row = new ArrayList<>();
        for (int j = 0; j < 2; j++) {
          row.add(1);
        }
        states.add(row);
      }
      return states;
    }

    /**
     * Returns a 2x2 grid of empty property maps.
     * Expected outcome: A 2x2 matrix where each cell's property map is empty.
     */
    @Override
    public List<List<Map<String, Double>>> getCellProperties() {
      List<List<Map<String, Double>>> properties = new ArrayList<>();
      for (int i = 0; i < 2; i++) {
        List<Map<String, Double>> row = new ArrayList<>();
        for (int j = 0; j < 2; j++) {
          row.add(new HashMap<>());
        }
        properties.add(row);
      }
      return properties;
    }

    /**
     * DummyConfigWriter to simulate saving configuration.
     * Expected outcome: lastFileSaved contains the file path provided to saveSimulation.
     */
    private static class DummyConfigWriter extends cellsociety.model.config.ConfigWriter {

      private String lastFileSaved;

      @Override
      public void saveCurrentConfig(ConfigInfo configInfo, String filePath)
          throws ParserConfigurationException, IOException, TransformerException {
        // Capture the file path instead of writing to disk.
        lastFileSaved = filePath;
      }

      @Override
      public String getLastFileSaved() {
        return lastFileSaved;
      }
    }

    /**
     * TestableConfigAPI overrides saveSimulation to use DummyConfigWriter.
     * Expected outcome: Calling saveSimulation returns the provided file path.
     */
    private static class TestableConfigAPI extends configAPI {

      @Override
      public String saveSimulation(String FilePath)
          throws ParserConfigurationException, IOException, TransformerException {
        try {
          Field writerField = configAPI.class.getDeclaredField("configWriter");
          writerField.setAccessible(true);
          writerField.set(this, new DummyConfigWriter());
        } catch (Exception e) {
          fail("Reflection error setting configWriter: " + e.getMessage());
        }
        return super.saveSimulation(FilePath);
      }
    }

    /**
     * Helper method to set private fields using reflection.
     */
    private void setPrivateField(Object target, String fieldName, Object value) {
      Field field = null;
      Class<?> clazz = target.getClass();
      while (clazz != null) {
        try {
          field = clazz.getDeclaredField(fieldName);
          break;
        } catch (NoSuchFieldException e) {
          clazz = clazz.getSuperclass();
        }
      }
      if (field == null) {
        fail("Field '" + fieldName + "' not found in class hierarchy of " + target.getClass().getName());
      }
      try {
        field.setAccessible(true);
        field.set(target, value);
      } catch (IllegalAccessException e) {
        fail("Failed to set private field " + fieldName + ": " + e.getMessage());
      }
    }

    /**
     * Creates a dummy ConfigInfo record with standard dimensions.
     * Expected outcome: A valid ConfigInfo with width=10 and height=20.
     */
    private ConfigInfo createDummyConfigInfo() {
      int width = 10;
      int height = 20;
      List<List<CellRecord>> gridData = new ArrayList<>();
      for (int i = 0; i < height; i++) {
        List<CellRecord> row = new ArrayList<>();
        for (int j = 0; j < width; j++) {
          row.add(new CellRecord(0, new HashMap<>()));
        }
        gridData.add(row);
      }
      ParameterRecord parameters = new ParameterRecord(new HashMap<>(), new HashMap<>());
      Set<Integer> acceptedStates = new HashSet<>(Arrays.asList(0, 1));
      return new ConfigInfo(
          SimulationType.LIFE,
          cellShapeType.SQUARE,
          gridEdgeType.BASE,
          neighborArrangementType.MOORE,
          1,
          "Dummy Simulation",
          "Dummy Author",
          "Dummy Description",
          width,
          height,
          1,
          gridData,
          parameters,
          acceptedStates,
          "dummyFile.xml"
      );
    }

    /**
     * Creates a dummy ConfigInfo record with zero dimensions.
     * Expected outcome: getGridWidth() and getGridHeight() return 0.
     */
    private ConfigInfo createZeroDimensionConfigInfo() {
      int width = 0;
      int height = 0;
      List<List<CellRecord>> gridData = new ArrayList<>();
      for (int i = 0; i < height; i++) {
        gridData.add(new ArrayList<>());
      }
      ParameterRecord parameters = new ParameterRecord(new HashMap<>(), new HashMap<>());
      Set<Integer> acceptedStates = new HashSet<>(Arrays.asList(0));
      return new ConfigInfo(
          SimulationType.LIFE,
          cellShapeType.SQUARE,
          gridEdgeType.BASE,
          neighborArrangementType.MOORE,
          1,
          "Zero Dimension Simulation",
          "Dummy Author",
          "Dummy Description",
          width,
          height,
          1,
          gridData,
          parameters,
          acceptedStates,
          "dummyFile.xml"
      );
    }

    /**
     * Creates a dummy ConfigInfo record with a single accepted state.
     * Expected outcome: getAcceptedStates() returns a set with one element.
     */
    private ConfigInfo createSingleStateConfigInfo() {
      int width = 10;
      int height = 20;
      List<List<CellRecord>> gridData = new ArrayList<>();
      for (int i = 0; i < height; i++) {
        List<CellRecord> row = new ArrayList<>();
        for (int j = 0; j < width; j++) {
          row.add(new CellRecord(0, new HashMap<>()));
        }
        gridData.add(row);
      }
      ParameterRecord parameters = new ParameterRecord(new HashMap<>(), new HashMap<>());
      Set<Integer> acceptedStates = new HashSet<>(Arrays.asList(0)); // Only one accepted state
      return new ConfigInfo(
          SimulationType.LIFE,
          cellShapeType.SQUARE,
          gridEdgeType.BASE,
          neighborArrangementType.MOORE,
          1,
          "Single State Simulation",
          "Dummy Author",
          "Dummy Description",
          width,
          height,
          1,
          gridData,
          parameters,
          acceptedStates,
          "dummyFile.xml"
      );
    }

    // ==================== Begin JUnit tests with descriptive names ====================

    @Test
    public void getFileNames_ReturnsNonNullList() {
      // Testing configAPI.getFileNames()
      configAPI api = new configAPI();
      List<String> fileNames = api.getFileNames();
      assertNotNull(fileNames, "getFileNames() should return a non-null list");
    }

    @Test
    public void getAcceptedStates_NullConfig_ThrowsNullPointerException() {
      // Testing configAPI.getAcceptedStates() when configInfo is null.
      configAPI api = new configAPI();
      Exception exception = assertThrows(NullPointerException.class, () -> api.getAcceptedStates());
      assertEquals("error-configInfo-NULL", exception.getMessage());
    }

    @Test
    public void getAcceptedStates_ConfigSet_ReturnsCorrectStates() {
      // Testing configAPI.getAcceptedStates() with a valid config.
      configAPI api = new configAPI();
      ConfigInfo dummyConfig = createDummyConfigInfo();
      setPrivateField(api, "configInfo", dummyConfig);
      Set<Integer> states = api.getAcceptedStates();
      assertEquals(dummyConfig.acceptedStates(), states, "getAcceptedStates() should return accepted states from config");
    }

    @Test
    public void getAcceptedStates_SingleState_ReturnsSingleElementSet() {
      // Testing configAPI.getAcceptedStates() with a config that accepts only one state.
      configAPI api = new configAPI();
      ConfigInfo singleStateConfig = createSingleStateConfigInfo();
      setPrivateField(api, "configInfo", singleStateConfig);
      Set<Integer> states = api.getAcceptedStates();
      assertEquals(1, states.size(), "Expected one accepted state in the config");
      assertTrue(states.contains(0), "Expected the accepted state to be 0");
    }

    @Test
    public void getGridWidth_NullConfig_ThrowsNumberFormatException() {
      // Testing configAPI.getGridWidth() when configInfo is null.
      configAPI api = new configAPI();
      Exception exception = assertThrows(NumberFormatException.class, () -> api.getGridWidth());
      assertEquals("error-configInfo-NULL", exception.getMessage());
    }

    @Test
    public void getGridWidth_ConfigSet_ReturnsCorrectWidth() {
      // Testing configAPI.getGridWidth() with a valid config.
      configAPI api = new configAPI();
      ConfigInfo dummyConfig = createDummyConfigInfo();
      setPrivateField(api, "configInfo", dummyConfig);
      int width = api.getGridWidth();
      assertEquals(dummyConfig.myGridWidth(), width, "getGridWidth() should return the grid width from config");
    }

    @Test
    public void getGridWidth_ZeroDimensions_ReturnsZero() {
      // Testing configAPI.getGridWidth() when configInfo has zero dimensions.
      configAPI api = new configAPI();
      ConfigInfo zeroConfig = createZeroDimensionConfigInfo();
      setPrivateField(api, "configInfo", zeroConfig);
      int width = api.getGridWidth();
      assertEquals(0, width, "Expected grid width to be 0 for zero-dimension config");
    }

    @Test
    public void getGridHeight_NullConfig_ThrowsNumberFormatException() {
      // Testing configAPI.getGridHeight() when configInfo is null.
      configAPI api = new configAPI();
      Exception exception = assertThrows(NumberFormatException.class, () -> api.getGridHeight());
      assertEquals("error-configInfo-NULL", exception.getMessage());
    }

    @Test
    public void getGridHeight_ConfigSet_ReturnsCorrectHeight() {
      // Testing configAPI.getGridHeight() with a valid config.
      configAPI api = new configAPI();
      ConfigInfo dummyConfig = createDummyConfigInfo();
      setPrivateField(api, "configInfo", dummyConfig);
      int height = api.getGridHeight();
      assertEquals(dummyConfig.myGridHeight(), height, "getGridHeight() should return the grid height from config");
    }

    @Test
    public void getGridHeight_ZeroDimensions_ReturnsZero() {
      // Testing configAPI.getGridHeight() when configInfo has zero dimensions.
      configAPI api = new configAPI();
      ConfigInfo zeroConfig = createZeroDimensionConfigInfo();
      setPrivateField(api, "configInfo", zeroConfig);
      int height = api.getGridHeight();
      assertEquals(0, height, "Expected grid height to be 0 for zero-dimension config");
    }

    @Test
    public void getSimulationInformation_NullConfig_ThrowsNullPointerException() {
      // Testing configAPI.getSimulationInformation() when configInfo is null.
      configAPI api = new configAPI();
      Exception exception = assertThrows(NullPointerException.class, () -> api.getSimulationInformation());
      assertEquals("error-configInfo-NULL", exception.getMessage());
    }

    @Test
    public void getSimulationInformation_ConfigSet_ReturnsCorrectInfo() {
      // Testing configAPI.getSimulationInformation() with a valid config.
      configAPI api = new configAPI();
      ConfigInfo dummyConfig = createDummyConfigInfo();
      setPrivateField(api, "configInfo", dummyConfig);
      Map<String, String> simInfo = api.getSimulationInformation();
      assertEquals(dummyConfig.myAuthor(), simInfo.get("author"), "Author in simulation information should match config");
      assertEquals(dummyConfig.myTitle(), simInfo.get("title"), "Title in simulation information should match config");
      assertEquals(dummyConfig.myType().toString(), simInfo.get("type"), "Type in simulation information should match config");
      assertEquals(dummyConfig.myDescription(), simInfo.get("description"), "Description in simulation information should match config");
    }

    @Test
    public void getConfigSpeed_NullConfig_ThrowsNumberFormatException() {
      // Testing configAPI.getConfigSpeed() when configInfo is null.
      configAPI api = new configAPI();
      Exception exception = assertThrows(NumberFormatException.class, () -> api.getConfigSpeed());
      assertEquals("error-configInfo-NULL", exception.getMessage());
    }

    @Test
    public void getConfigSpeed_ConfigSet_ReturnsCorrectSpeed() {
      // Testing configAPI.getConfigSpeed() with a valid config.
      configAPI api = new configAPI();
      ConfigInfo dummyConfig = createDummyConfigInfo();
      setPrivateField(api, "configInfo", dummyConfig);
      double speed = api.getConfigSpeed();
      assertEquals(dummyConfig.myTickSpeed(), speed, "getConfigSpeed() should return the tick speed from config");
    }

    @Test
    public void saveSimulation_ValidInput_ReturnsProvidedFilePath() throws ParserConfigurationException, IOException, TransformerException {
      // Testing configAPI.saveSimulation() using TestableConfigAPI.
      TestableConfigAPI api = new TestableConfigAPI();
      ConfigInfo dummyConfig = createDummyConfigInfo();
      setPrivateField(api, "configInfo", dummyConfig);
      DummyModelApi dummyModel = new DummyModelApi();
      api.setModelAPI(dummyModel); // Set the model API via setter
      String filePath = "dummy/path/config.xml";
      String savedFile = api.saveSimulation(filePath);
      assertEquals(filePath, savedFile, "saveSimulation() should return the file path provided as argument");
    }

    @Test
    public void loadSimulation_ValidFile_SetsModelApiConfigInfo() throws ParserConfigurationException, IOException, SAXException {
      // Testing configAPI.loadSimulation() to ensure that DummyModelApi receives the dummy config.
      configAPI api = new configAPI();
      DummyModelApi dummyModel = new DummyModelApi();
      api.setModelAPI(dummyModel); // Set the model API before loading
      // Inject DummyConfigReader so that readConfig returns a dummy config instead of reading a file.
      setPrivateField(api, "configReader", new DummyConfigReader());
      api.loadSimulation("dummyFile.xml");
      assertEquals(createStaticDummyConfigInfo(), dummyModel.configInfoDummy, "After loadSimulation(), model API should have the dummy config info set");
    }
  }
}
