//package modelAPItests;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import cellsociety.model.config.CellRecord;
//import cellsociety.model.data.Grid;
//import cellsociety.model.data.cells.CellFactory;
//import cellsociety.model.data.constants.EdgeType;
//import cellsociety.model.data.constants.GridShape;
//import cellsociety.model.data.constants.NeighborType;
//import cellsociety.model.data.neighbors.NeighborCalculator;
//import cellsociety.model.data.states.State;
//import cellsociety.model.modelAPI.StyleManager;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.util.*;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//public class StyleManagerTest {
//
//  private static final String FILENAME = "SimulationStyle.properties";
//  private File propertiesFile;
//  private Grid<DummyState> testGrid;
//  private NeighborCalculator<DummyState> neighborCalculator;
//  private StyleManager styleManager;
//
//  // Minimal CellFactory and DummyState for creating a test grid.
//  private static final CellFactory<DummyState> FACTORY = new CellFactory<>(DummyState.class);
//
//  public enum DummyState implements State {
//    DUMMY;
//
//    @Override
//    public int getValue() {
//      return 0;
//    }
//  }
//
//  // Helper method to create a grid with given rows, columns, shape, neighbor type, and edge type.
//  private Grid<DummyState> createGrid(int rows, int cols, GridShape shape, NeighborType type, EdgeType edgeType) {
//    List<List<CellRecord>> raw = new ArrayList<>();
//    for (int r = 0; r < rows; r++) {
//      List<CellRecord> row = new ArrayList<>();
//      for (int c = 0; c < cols; c++) {
//        row.add(new CellRecord(0, new HashMap<>()));
//      }
//      raw.add(row);
//    }
//    return new Grid<>(raw, FACTORY, shape, type, edgeType);
//  }
//
//  @BeforeEach
//  void setUp() throws IOException {
//    // Create a temporary properties file with known values.
//    propertiesFile = new File(FILENAME);
//    Properties testProperties = new Properties();
//
//    // Neighbor Arrangement properties.
//    testProperties.setProperty("NEIGHBORARRANGEMENT.ONE", "MOORE");
//    testProperties.setProperty("NEIGHBORARRANGEMENT.TWO", "VONNEUMANN");
//    testProperties.setProperty("NEIGHBORARRANGEMENT.PREFERENCE", "MOORE");
//
//    // Edge Policy properties.
//    testProperties.setProperty("EDGEPOLICY.ONE", "FINITE");
//    testProperties.setProperty("EDGEPOLICY.TWO", "TOROIDAL");
//    testProperties.setProperty("EDGEPOLICY.PREFERENCE", "FINITE");
//
//    // Cell Shape properties.
//    testProperties.setProperty("CELLSHAPE.ONE", "SQUARE");
//    testProperties.setProperty("CELLSHAPE.TWO", "HEXAGON");
//    testProperties.setProperty("CELLSHAPE.PREFERENCE", "SQUARE");
//
//    // Grid Outline preference.
//    testProperties.setProperty("GRIDOUTLINE.PREFERENCE", "true");
//
//    try (OutputStream output = new FileOutputStream(propertiesFile)) {
//      testProperties.store(output, "Test properties for StyleManager");
//    }
//
//    // Create a test grid. Initially we use a SQUARE grid with MOORE neighbor type.
//    testGrid = createGrid(3, 3, GridShape.SQUARE, NeighborType.MOORE, EdgeType.BASE);
//    neighborCalculator = testGrid.getNeighborCalculator();
//    styleManager = new StyleManager();
//  }
//
//  @AfterEach
//  void tearDown() {
//    if (propertiesFile.exists()) {
//      propertiesFile.delete();
//    }
//  }
//
//  @Test
//  public void testGetPossibleNeighborArrangements() {
//    List<String> arrangements = styleManager.getPossibleNeighborArrangements();
//    // Verify that the list contains both MOORE and VONNEUMANN options.
//    assertTrue(arrangements.contains("MOORE"), "Expected to contain MOORE");
//    assertTrue(arrangements.contains("VONNEUMANN"), "Expected to contain VONNEUMANN");
//    // Ensure that the key for the current preference is not included.
//    assertFalse(arrangements.contains("NEIGHBORARRANGEMENT.PREFERENCE"), "Preference key should not be included");
//  }
//
//  @Test
//  public void testSetNeighborArrangement() throws IOException {
//    // Before change: for a SQUARE grid with MOORE, the center cell (1,1) should have 8 neighbors.
//    neighborCalculator.setSteps(1);
//    int initialCount = neighborCalculator.getNeighbors(testGrid, 1, 1).size();
//    assertEquals(8, initialCount, "MOORE arrangement should yield 8 neighbors");
//
//    // Change the neighbor arrangement to VONNEUMANN via the StyleManager.
//    styleManager.setNeighborArrangement("NEUMANN");
//
//    // After change, the same center cell should have 4 neighbors.
//    int updatedCount = neighborCalculator.getNeighbors(testGrid, 1, 1).size();
//    assertEquals(4, updatedCount, "NEUMANN arrangement should yield 4 neighbors");
//
//    // Verify that the properties file was updated.
//    Properties updatedProperties = new Properties();
//    try (FileInputStream fis = new FileInputStream(propertiesFile)) {
//      updatedProperties.load(fis);
//    }
//    assertEquals("NEUMANN", updatedProperties.getProperty("NEIGHBORARRANGEMENT.PREFERENCE"));
//  }
//
//  @Test
//  public void testGetPossibleEdgePolicies() {
//    List<String> policies = styleManager.getPossibleEdgePolicies();
//    assertTrue(policies.contains("FINITE"), "Expected to contain FINITE");
//    assertTrue(policies.contains("TOROIDAL"), "Expected to contain TOROIDAL");
//  }
//
//  @Test
//  public void testSetEdgePolicy() throws IOException {
//    // Change edge policy to TOROIDAL.
//    styleManager.setEdgePolicy("TORUS");
//
//    // Verify that the neighbor calculator's edge type has been updated.
//    assertEquals(EdgeType.valueOf("TORUS"), neighborCalculator.getEdgeType());
//
//    // Reload the properties file and verify the update.
//    Properties updatedProperties = new Properties();
//    try (FileInputStream fis = new FileInputStream(propertiesFile)) {
//      updatedProperties.load(fis);
//    }
//    assertEquals("TORUS", updatedProperties.getProperty("EDGEPOLICY.PREFERENCE"));
//  }
//
//  @Test
//  public void testGetPossibleCellShapes() {
//    List<String> shapes = styleManager.getPossibleCellShapes();
//    assertTrue(shapes.contains("SQUARE"), "Expected to contain SQUARE");
//    assertTrue(shapes.contains("HEXAGON"), "Expected to contain HEXAGON");
//  }
//
//  @Test
//  public void testSetCellShape() throws IOException {
//    // Update the cell shape to HEXAGON (input provided in lower-case to test conversion).
//    styleManager.setCellShape("HEX");
//    // Verify that the neighbor calculator's shape is updated.
//    assertEquals(GridShape.valueOf("HEX"), neighborCalculator.getShape());
//
//    // Reload the properties file and verify that the preference is saved exactly as provided.
//    Properties updatedProperties = new Properties();
//    try (FileInputStream fis = new FileInputStream(propertiesFile)) {
//      updatedProperties.load(fis);
//    }
//    assertEquals("HEX", updatedProperties.getProperty("CELLSHAPE.PREFERENCE"));
//  }
//
//  @Test
//  public void testSetGridOutlinePreference() throws IOException {
//    // Update grid outline preference to false.
//    styleManager.setGridOutlinePreference(false);
//
//    // Reload properties file and verify the update.
//    Properties updatedProperties = new Properties();
//    try (FileInputStream fis = new FileInputStream(propertiesFile)) {
//      updatedProperties.load(fis);
//    }
//    assertEquals("false", updatedProperties.getProperty("GRIDOUTLINE.PREFERENCE"));
//  }
//
//  @Test
//  public void testLoadPropertiesMissingResource() {
//    // Delete the properties file to force the StyleManager to load the default resource.
//    if (propertiesFile.exists()) {
//      propertiesFile.delete();
//    }
//    try {
//      List<String> shapes = styleManager.getPossibleCellShapes();
//      assertNotNull(shapes, "Expected non-null list from resource");
//      assertFalse(shapes.isEmpty(), "Expected non-empty list from resource");
//    } catch (NoSuchElementException e) {
//      fail("Expected default resource properties file to be available in the classpath.");
//    }
//  }
//}
