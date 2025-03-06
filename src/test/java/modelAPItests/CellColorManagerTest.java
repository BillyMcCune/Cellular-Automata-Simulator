package modelAPItests;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.constants.EdgeType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.modelAPI.CellColorManager;
import cellsociety.model.modelAPI.ModelApi;
import cellsociety.model.data.states.State;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Updated test suite for the CellColorManager class.
 *
 * <p>Test methods follow the naming convention:
 * [TestedMethod_StateUnderTest_ExpectedOutcome]
 *
 * For example:
 *  - getCellColor_RowColOutOfBounds_ReturnsNull()
 *  - getPropertyColor_WithColoredId_ReturnsUniqueColor()
 *
 * Variables used in tests are named to reflect the input scenario (e.g., OUT_OF_BOUNDS, COLORED_ID_VALUE).
 *
 * Note: The setNewColorPreference method writes an updated SimulationStyle.properties file to the working directory.
 * Since getColorFromPreferences still loads from the classpath resource, the test verifies that the file update occurs as expected.
 */
public class CellColorManagerTest {

  // Temporary file used to test setNewColorPreference.
  private static final String SIM_STYLE_FILENAME = "SimulationStyle.properties";
  private File simulationStyleFile;

  // Test grid and CellColorManager.
  private Grid<TestState> testGrid;
  private CellColorManager colorManager;

  /**
   * A minimal enum implementing State for testing.
   */
  public enum TestState implements State {
    RED, WHITE, SOMETHING;

    @Override
    public int getValue() {
      return ordinal();
    }

    // Dummy fromInt method for the CellFactory.
    public static <T extends Enum<T> & State> T fromInt(Class<T> enumClass, int value) {
      T[] constants = enumClass.getEnumConstants();
      return constants[value % constants.length];
    }
  }

  /**
   * Helper method to create a test Grid.
   * Expected outcome: A grid of specified rows and columns returning the same cell.
   */
  private Grid<TestState> createTestGrid(final int rows, final int cols,
      final TestState initialState,
      final Map<String, Double> properties) {
    CellFactory<TestState> factory = new CellFactory<>(TestState.class);
    // Create a single cell with given initial state and properties.
    Cell<TestState> cell = factory.createCell(initialState.ordinal());
    cell.setAllProperties(properties);

    // Return an anonymous Grid that always returns the created cell.
    return new Grid<TestState>(new java.util.ArrayList<>(), factory,
        GridShape.SQUARE, NeighborType.MOORE, EdgeType.BASE) {
      @Override
      public int getNumRows() {
        return rows;
      }
      @Override
      public int getNumCols() {
        return cols;
      }
      @Override
      public Cell<TestState> getCell(int row, int col) {
        return cell;
      }
    };
  }

  @BeforeEach
  void setUp() throws IOException {
    // Create a temporary SimulationStyle.properties file in the working directory.
    simulationStyleFile = new File(SIM_STYLE_FILENAME);
    Properties simProps = new Properties();
    // Preload with one dummy entry mapping TestState.RED to "#FF0000".
    simProps.setProperty("TestState.RED", "#FF0000");
    try (OutputStream out = new FileOutputStream(simulationStyleFile)) {
      simProps.store(out, "Test simulation style properties");
    }

    // Create a test grid with one cell whose state is RED and no extra properties.
    testGrid = createTestGrid(3, 3, TestState.RED, new HashMap<>());
    colorManager = new CellColorManager(testGrid);
  }

  @AfterEach
  void tearDown() {
    if (simulationStyleFile.exists()) {
      simulationStyleFile.delete();
    }
  }

  @Test
  public void getCellColor_RowColOutOfBounds_ReturnsNull() {
    // Input: Row and column indices (5, 5) outside a 3x3 grid.
    // Expected outcome: getCellColor() returns null for out-of-bounds coordinates.
    String color = colorManager.getCellColor(5, 5, false);
    assertNull(color, "Out-of-bounds cell lookup should return null");
  }

  @Test
  public void getPropertyColor_WithColoredId_ReturnsUniqueColor() {
    // Input: A cell with the "coloredId" property set to 42.
    // Expected outcome: getPropertyColor() returns a unique color computed from coloredId.
    Map<String, Double> props = new HashMap<>();
    props.put("coloredId", 42.0);
    Grid<TestState> grid = createTestGrid(3, 3, TestState.RED, props);
    colorManager.setGrid(grid);

    String propColor = colorManager.getPropertyColor(grid.getCell(0, 0));
    // Replicate uniqueColorGenerator algorithm to compute expected color.
    long multiplier = 2654435761L;
    long scrambled = (multiplier * 42) & 0xffffffffL;
    float hue = (scrambled % 360) / 360f;
    float sat = 0.5f + ((scrambled >> 8) % 50) / 100f;
    float bright = 0.5f + ((scrambled >> 16) % 50) / 100f;
    int rgb = Color.HSBtoRGB(hue, sat, bright);
    int r = (rgb >> 16) & 0xFF;
    int g = (rgb >> 8) & 0xFF;
    int b = rgb & 0xFF;
    String expectedColor = String.format("#%02X%02X%02X", r, g, b);
    assertEquals(expectedColor, propColor, "Expected unique color generated from coloredId");
  }

  @Test
  public void getCellTypesAndDefaultColors_WithTestPrefix_ReturnsNonNullMapping() {
    // Input: The simulation type "Test".
    // Expected outcome: getCellTypesAndDefaultColors() returns a non-null mapping of cell types and their default colors.
    Map<String, String> mapping = colorManager.getCellTypesAndDefaultColors("Test");
    assertNotNull(mapping, "Mapping of cell types and default colors should not be null");
  }

  @Test
  public void setNewColorPreference_NewColorPreference_FileUpdatedCorrectly() throws IOException {
    // Input: A new color preference for state "TestState.GREEN" with new color "#00FF00".
    // Expected outcome: The SimulationStyle.properties file is updated with the new color for "TestState.GREEN".
    String stateName = "TestState.GREEN";
    String newColor = "#00FF00";
    colorManager.setNewColorPreference(stateName, newColor);

    Properties fileProps = new Properties();
    try (FileInputStream fis = new FileInputStream(simulationStyleFile)) {
      fileProps.load(fis);
    }
    assertEquals(newColor, fileProps.getProperty(stateName),
        "Expected file to contain the updated color preference for TestState.GREEN");
  }

  @Test
  public void getColorFromPreferences_StateNotSet_ReturnsDefaultColor() {
    // Input: A state name ("NonExistentState") not set in the preferences.
    // Expected outcome: getColorFromPreferences() returns the default color (as per getDefaultColorByState).
    String stateName = "NonExistentState";
    String expectedDefault = colorManager.getDefaultColorByState(stateName);
    String color = colorManager.getColorFromPreferences(stateName);
    assertEquals(expectedDefault, color,
        "Expected fallback to default color when preference is not set");
  }

  @Test
  public void getDefaultColorByState_KeyNotFound_ReturnsWhite() {
    // Input: A key ("NonExistentState") not present in the default color mapping.
    // Expected outcome: getDefaultColorByState() returns "WHITE".
    String defaultColor = colorManager.getDefaultColorByState("NonExistentState");
    assertEquals("WHITE", defaultColor,
        "Expected fallback color WHITE when key is not found in the default mapping");
  }
}
