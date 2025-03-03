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
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.constants.BoundaryType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.data.states.State;
import cellsociety.model.modelAPI.modelAPI;
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
 * Dummy classes simulate external dependencies.
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
      super(GridShape.SQUARE, NeighborType.MOORE, BoundaryType.STANDARD);
    }
  }

  /**
   * DummyConfigReader overrides readConfig to return a dummy configuration
   * (instead of performing file I/O).
   */
  private static class DummyConfigReader extends cellsociety.model.config.ConfigReader {
    @Override
    public ConfigInfo readConfig(String fileName)
        throws ParserConfigurationException, IOException, SAXException {
      // Return a dummy configuration (could be customized further)
      return createStaticDummyConfigInfo();
    }
  }

  // A static helper to create a dummy ConfigInfo (used by DummyConfigReader)
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
   * DummyModelAPI extends modelAPI.
   * It creates a dummy grid using the actual Grid constructor and provides dummy implementations
   * for getCellStates() and getCellProperties().
   */
  @Nested
  public class DummyModelAPI extends modelAPI {

    public ConfigInfo configInfoDummy; // For verification
    private Grid<TestState> grid;
    private Map<String, Double> doubleParams;
    private Map<String, String> stringParams;

    public DummyModelAPI() {
      // Create a dummy raw grid for a 2x2 grid.
      List<List<CellRecord>> raw = new ArrayList<>();
      for (int i = 0; i < 2; i++) {
        List<CellRecord> row = new ArrayList<>();
        for (int j = 0; j < 2; j++) {
          // For testing, set state value to 1.
          row.add(new CellRecord(1, new HashMap<>()));
        }
        raw.add(row);
      }
      grid = new Grid<>(raw, new DummyCellFactory(), new DummyNeighborCalculator());
      doubleParams = new HashMap<>();
      doubleParams.put("param1", 1.0);
      stringParams = new HashMap<>();
      stringParams.put("param2", "value2");
    }

    public void setConfiginfo(ConfigInfo configInfo) {
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
     * DummyConfigWriter extends ConfigWriter so that saveCurrentConfig stores the file path.
     */
    private static class DummyConfigWriter extends cellsociety.model.config.ConfigWriter {

      private String lastFileSaved;

      @Override
      public void saveCurrentConfig(ConfigInfo configInfo, String filePath)
          throws ParserConfigurationException, IOException, TransformerException {
        lastFileSaved = filePath;
      }

      @Override
      public String getLastFileSaved() {
        return lastFileSaved;
      }
    }

    /**
     * TestableConfigAPI is a subclass of configAPI that overrides saveSimulation to use
     * DummyConfigWriter.
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
     * A robust reflection helper that searches the class hierarchy for the given field.
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
        fail("Field '" + fieldName + "' not found in class hierarchy of " + target.getClass()
            .getName());
      }
      try {
        field.setAccessible(true);
        field.set(target, value);
      } catch (IllegalAccessException e) {
        fail("Failed to set private field " + fieldName + ": " + e.getMessage());
      }
    }

    /**
     * Helper method to create a dummy ConfigInfo record.
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

    // ==================== Begin JUnit tests ====================

    @Test
    public void testGetFileNames() {
      configAPI api = new configAPI();
      List<String> fileNames = api.getFileNames();
      assertNotNull(fileNames, "getFileNames should not return null");
    }

    @Test
    public void testGetAcceptedStates_NullConfig() {
      configAPI api = new configAPI();
      Exception exception = assertThrows(NullPointerException.class, () -> api.getAcceptedStates());
      assertEquals("error-configInfo-NULL", exception.getMessage());
    }

    @Test
    public void testGetAcceptedStates_WithConfig() {
      configAPI api = new configAPI();
      ConfigInfo dummyConfig = createDummyConfigInfo();
      setPrivateField(api, "configInfo", dummyConfig);
      Set<Integer> states = api.getAcceptedStates();
      assertEquals(dummyConfig.acceptedStates(), states,
          "Accepted states should match dummy config info");
    }

    @Test
    public void testGetGridWidth_NullConfig() {
      configAPI api = new configAPI();
      Exception exception = assertThrows(NumberFormatException.class, () -> api.getGridWidth());
      assertEquals("error-configInfo-NULL", exception.getMessage());
    }

    @Test
    public void testGetGridWidth_WithConfig() {
      configAPI api = new configAPI();
      ConfigInfo dummyConfig = createDummyConfigInfo();
      setPrivateField(api, "configInfo", dummyConfig);
      int width = api.getGridWidth();
      assertEquals(dummyConfig.myGridWidth(), width, "Grid width should match dummy config info");
    }

    @Test
    public void testGetGridHeight_NullConfig() {
      configAPI api = new configAPI();
      Exception exception = assertThrows(NumberFormatException.class, () -> api.getGridHeight());
      assertEquals("error-configInfo-NULL", exception.getMessage());
    }

    @Test
    public void testGetGridHeight_WithConfig() {
      configAPI api = new configAPI();
      ConfigInfo dummyConfig = createDummyConfigInfo();
      setPrivateField(api, "configInfo", dummyConfig);
      int height = api.getGridHeight();
      assertEquals(dummyConfig.myGridHeight(), height,
          "Grid height should match dummy config info");
    }

    @Test
    public void testGetSimulationInformation_NullConfig() {
      configAPI api = new configAPI();
      Exception exception = assertThrows(NullPointerException.class,
          () -> api.getSimulationInformation());
      assertEquals("error-configInfo-NULL", exception.getMessage());
    }

    @Test
    public void testGetSimulationInformation_WithConfig() {
      configAPI api = new configAPI();
      ConfigInfo dummyConfig = createDummyConfigInfo();
      setPrivateField(api, "configInfo", dummyConfig);
      Map<String, String> simInfo = api.getSimulationInformation();
      assertEquals(dummyConfig.myAuthor(), simInfo.get("author"),
          "Author should match dummy config info");
      assertEquals(dummyConfig.myTitle(), simInfo.get("title"),
          "Title should match dummy config info");
      assertEquals(dummyConfig.myType().toString(), simInfo.get("type"),
          "Type should match dummy config info");
      assertEquals(dummyConfig.myDescription(), simInfo.get("description"),
          "Description should match dummy config info");
    }

    @Test
    public void testGetConfigSpeed_NullConfig() {
      configAPI api = new configAPI();
      Exception exception = assertThrows(NumberFormatException.class, () -> api.getConfigSpeed());
      assertEquals("error-configInfo-NULL", exception.getMessage());
    }

    @Test
    public void testGetConfigSpeed_WithConfig() {
      configAPI api = new configAPI();
      ConfigInfo dummyConfig = createDummyConfigInfo();
      setPrivateField(api, "configInfo", dummyConfig);
      double speed = api.getConfigSpeed();
      assertEquals(dummyConfig.myTickSpeed(), speed, "Tick speed should match dummy config info");
    }

    @Test
    public void testSaveSimulation()
        throws ParserConfigurationException, IOException, TransformerException {
      TestableConfigAPI api = new TestableConfigAPI();
      ConfigInfo dummyConfig = createDummyConfigInfo();
      setPrivateField(api, "configInfo", dummyConfig);
      DummyModelAPI dummyModel = new DummyModelAPI();
      api.setModelAPI(dummyModel); // Set modelAPI via setter
      String filePath = "dummy/path/config.xml";
      String savedFile = api.saveSimulation(filePath);
      assertEquals(filePath, savedFile,
          "Saved file name should match the provided file path from DummyConfigWriter");
    }

    @Test
    public void testLoadSimulation_Success()
        throws ParserConfigurationException, IOException, SAXException {
      configAPI api = new configAPI();
      DummyModelAPI dummyModel = new DummyModelAPI();
      api.setModelAPI(dummyModel); // Set modelAPI before loading
      // Inject DummyConfigReader so that readConfig returns a dummy config instead of reading a file.
      setPrivateField(api, "configReader", new DummyConfigReader());
      api.loadSimulation("dummyFile.xml");
      assertEquals(createStaticDummyConfigInfo(), dummyModel.configInfoDummy,
          "Model API should have the dummy config info set");
    }
  }
}
