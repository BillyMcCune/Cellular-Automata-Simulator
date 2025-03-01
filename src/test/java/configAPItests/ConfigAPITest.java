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
import cellsociety.model.data.states.State;
import cellsociety.model.modelAPI.modelAPI;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

/**
 * JUnit tests for configAPI. These tests cover:
 * <ul>
 *   <li>Retrieval of file names</li>
 *   <li>Behavior when configuration is not loaded (expecting exceptions)</li>
 *   <li>Behavior when a dummy configuration is injected (using reflection)</li>
 *   <li>The saveSimulation flow (using a Testable subclass and dummy dependencies)</li>
 *   <li>The loadSimulation method (simulated)</li>
 * </ul>
 *
 * Dummy classes simulate external dependencies. In a real project you might use dependency injection or a mocking framework.
 */
public class ConfigAPITest {

  /**
   * TestState is used in place of DummyState. It implements the State interface.
   * It defines two constants: ZERO (with value 0) and ONE (with value 1).
   */
  private enum TestState implements State {
    ZERO(0), ONE(1);

    private final int value;

    TestState(int value) {
      this.value = value;
    }

    @Override
    public int getValue() {
      return value;
    }

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
   * DummyModelAPI extends modelAPI.
   * It overrides methods used by configAPI to return a dummy grid and parameter maps.
   */
  private static class DummyModelAPI extends modelAPI {
    public ConfigInfo configInfoDummy; // public so we can verify later
    private DummyGrid grid;
    private Map<String, Double> doubleParams;
    private Map<String, String> stringParams;

    public DummyModelAPI() {
      grid = new DummyGrid(2, 2); // a simple 2x2 grid
      doubleParams = new HashMap<>();
      doubleParams.put("param1", 1.0);
      stringParams = new HashMap<>();
      stringParams.put("param2", "value2");
    }

    @Override
    public void setConfiginfo(ConfigInfo configInfo) {
      this.configInfoDummy = configInfo;
    }

    @Override
    public Grid<Cell<TestState>> getGrid() {
      return grid;
    }

    @Override
    public Map<String, Double> getDoubleParameters() {
      return doubleParams;
    }

    @Override
    public Map<String, String> getStringParameters() {
      return stringParams;
    }
  }

  /**
   * DummyGrid implements the Grid interface with a fixed number of rows and columns.
   * It stores an array of dummy cells whose state type is TestState.
   */
  private static class DummyGrid implements Grid<Cell<TestState>> {
    private int numRows;
    private int numCols;
    private Cell<TestState>[][] cells;

    @SuppressWarnings("unchecked")
    public DummyGrid(int rows, int cols) {
      this.numRows = rows;
      this.numCols = cols;
      cells = new Cell[rows][cols];
      for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
          cells[i][j] = new DummyCell();
        }
      }
    }

    @Override
    public int getNumRows() {
      return numRows;
    }

    @Override
    public int getNumCols() {
      return numCols;
    }

    @Override
    public Cell<TestState> getCell(int row, int col) {
      return cells[row][col];
    }
  }

  /**
   * DummyCell extends Cell using TestState.
   * It calls the superclass constructor with TestState.ONE.
   */
  private static class DummyCell extends Cell<TestState> {
    public DummyCell() {
      super(TestState.ONE);
    }
  }

  /**
   * DummyConfigWriter extends ConfigWriter so that saveCurrentConfig simply stores the file path.
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
   * TestableConfigAPI is a subclass of configAPI that overrides saveSimulation to use DummyConfigWriter.
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
   * Helper method to set a private field via reflection.
   */
  private void setPrivateField(Object target, String fieldName, Object value) {
    try {
      Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (Exception e) {
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

  // ====================
  // Begin JUnit tests
  // ====================

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
    assertEquals(dummyConfig.acceptedStates(), states, "Accepted states should match the dummy config info");
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
    assertEquals(dummyConfig.myGridHeight(), height, "Grid height should match dummy config info");
  }

  @Test
  public void testGetSimulationInformation_NullConfig() {
    configAPI api = new configAPI();
    Exception exception = assertThrows(NullPointerException.class, () -> api.getSimulationInformation());
    assertEquals("error-configInfo-NULL", exception.getMessage());
  }

  @Test
  public void testGetSimulationInformation_WithConfig() {
    configAPI api = new configAPI();
    ConfigInfo dummyConfig = createDummyConfigInfo();
    setPrivateField(api, "configInfo", dummyConfig);
    Map<String, String> simInfo = api.getSimulationInformation();
    assertEquals(dummyConfig.myAuthor(), simInfo.get("author"), "Author should match dummy config info");
    assertEquals(dummyConfig.myTitle(), simInfo.get("title"), "Title should match dummy config info");
    assertEquals(dummyConfig.myType().toString(), simInfo.get("type"), "Type should match dummy config info");
    assertEquals(dummyConfig.myDescription(), simInfo.get("description"), "Description should match dummy config info");
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
  public void testGetSimulationStyles() {
    configAPI api = new configAPI();
    Map<String, String> styles = api.getSimulationStyles();
    assertNotNull(styles, "getSimulationStyles should not return null");
    assertTrue(styles.isEmpty(), "Simulation styles should be empty by default");
  }

  @Test
  public void testSetSimulationStyles() {
    configAPI api = new configAPI();
    Map<String, String> styles = new HashMap<>();
    styles.put("background", "blue");
    api.setSimulationStyles(styles);
  }

  @Test
  public void testSaveSimulation() throws ParserConfigurationException, IOException, TransformerException {
    TestableConfigAPI api = new TestableConfigAPI();
    ConfigInfo dummyConfig = createDummyConfigInfo();
    setPrivateField(api, "configInfo", dummyConfig);
    DummyModelAPI dummyModel = new DummyModelAPI();
    setPrivateField(api, "myModelAPI", dummyModel);
    String filePath = "dummy/path/config.xml";
    String savedFile = api.saveSimulation(filePath);
    assertEquals(filePath, savedFile, "Saved file name should match the provided file path from DummyConfigWriter");
  }

  @Test
  public void testLoadSimulation_Success() throws ParserConfigurationException, IOException, SAXException {
    configAPI api = new configAPI();
    DummyModelAPI dummyModel = new DummyModelAPI();
    ConfigInfo dummyConfig = createDummyConfigInfo();
    setPrivateField(api, "configInfo", dummyConfig);
    api.setModelAPI(dummyModel);
    api.loadSimulation("config.xml");
    assertEquals(dummyConfig, dummyModel.configInfoDummy, "Model API should have the dummy config info set");
  }
}
