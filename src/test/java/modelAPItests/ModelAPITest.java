


//package modelAPItests;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//import cellsociety.model.data.constants.EdgeType;
//import cellsociety.model.data.constants.GridShape;
//import cellsociety.model.data.constants.NeighborType;
//import cellsociety.model.modelAPI.ModelApi;
//import configAPItests.ConfigAPITest.DummyNeighborCalculator;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.lang.reflect.Field;
//import java.util.List;
//import java.util.Properties;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//public class ModelAPITest {
//
//  private static final String CELL_COLOR_FILE = "CellColor.properties";
//  private static final String SIMULATION_STYLE_FILE = "SimulationStyle.properties";
//
//  // Create an instance of the class containing the methods.
//  private ModelApi model;
//
//  @BeforeEach
//  public void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
//    model = new ModelApi();
//
//    // Create a default CellColor.properties file with known values.
//    Properties defaultColors = new Properties();
//    defaultColors.setProperty("FireState.TREE", "GREEN");
//    defaultColors.setProperty("LifeState.ALIVE", "LIGHTBLUE");
//    try (OutputStream output = new FileOutputStream(CELL_COLOR_FILE)) {
//      defaultColors.store(output, "Default cell colors");
//    }
//
//    // Ensure the SimulationStyle.properties file does not exist before each test.
//    File simulationFile = new File(SIMULATION_STYLE_FILE);
//    if (simulationFile.exists()) {
//      simulationFile.delete();
//    }
//
//    // Inject a DummyNeighborCalculator into the model via reflection.
//    DummyNeighborCalculator dummy = new DummyNeighborCalculator();
//    Field neighborField = ModelApi.class.getDeclaredField("myNeighborCalculator");
//    neighborField.setAccessible(true);
//    neighborField.set(model, dummy);
//
//    // Also, adjust the private property keys to more convenient test values.
//    // For example, we want the neighbor arrangement key to be "NEIGHBORARRANGEMENT.MOORE"
//    Field neighborArrangementKeyField = ModelApi.class.getDeclaredField("neighborArrangementProperty");
//    neighborArrangementKeyField.setAccessible(true);
//    neighborArrangementKeyField.set(model, "NEIGHBORARRANGEMENT.MOORE");
//
//    // For edge policy, use "EDGEPOLICY.TORUS"
//    Field edgePolicyKeyField = ModelApi.class.getDeclaredField("edgePolicyProperty");
//    edgePolicyKeyField.setAccessible(true);
//    edgePolicyKeyField.set(model, "EDGEPOLICY.TORUS");
//
//    // For cell shape, use "CELLSHAPE.SQUARE"
//    Field cellShapeKeyField = ModelApi.class.getDeclaredField("cellShapeProperty");
//    cellShapeKeyField.setAccessible(true);
//    cellShapeKeyField.set(model, "CELLSHAPE.SQUARE");
//  }
//
//  @AfterEach
//  public void tearDown() {
//    File cellColorFile = new File(CELL_COLOR_FILE);
//    if (cellColorFile.exists()) {
//      cellColorFile.delete();
//    }
//    File simulationFile = new File(SIMULATION_STYLE_FILE);
//    if (simulationFile.exists()) {
//      simulationFile.delete();
//    }
//  }
//
//
//  @Test
//  public void testGetDefaultColorByState_ExistingKey() {
//    String color = model.getDefaultColorByState("FireState.TREE");
//    assertEquals("GREEN", color, "Expected default color GREEN for FireState.TREE");
//  }
//
//  @Test
//  public void testGetDefaultColorByState_NonExistingKey() {
//    String color = model.getDefaultColorByState("NonExistentState");
//    // When key is not found, the fallback is WHITE.
//    assertEquals("WHITE", color, "Expected fallback color WHITE for a non-existent state");
//  }
//
//  @Test
//  public void testGetColorFromPreferences_NoPreferenceSet() {
//    // With no SimulationStyle.properties file, getColorFromPreferences should fall back to the default.
//    String color = model.getColorFromPreferences("FireState.TREE");
//    assertEquals("GREEN", color, "Expected default color GREEN when no user preference is set");
//  }
//
//  @Test
//  public void testSetNewColorPreferenceAndGet() {
//    // Set a new color preference for FireState.TREE.
//    model.setNewColorPreference("FireState.TREE", "BLUE");
//    // Now retrieving should return the new value.
//    String color = model.getColorFromPreferences("FireState.TREE");
//    assertEquals("BLUE", color, "Expected updated color BLUE for FireState.TREE");
//  }
//
//  @Test
//  public void testSetNewColorPreference_Persistence() throws IOException {
//    // Set a new color preference for LifeState.ALIVE.
//    model.setNewColorPreference("LifeState.ALIVE", "YELLOW");
//
//    // Directly load SimulationStyle.properties to verify that the change persisted.
//    Properties simulationStyle = new Properties();
//    try (InputStream input = new FileInputStream(SIMULATION_STYLE_FILE)) {
//      simulationStyle.load(input);
//    }
//    assertEquals("YELLOW", simulationStyle.getProperty("LifeState.ALIVE"),
//        "Expected the SimulationStyle.properties file to contain YELLOW for LifeState.ALIVE");
//  }
//
//  // ---------------- Additional Tests for Simulation Style ----------------
//
//  @Test
//  public void testGetPossibleCellShapes() throws IOException {
//    // Create a SimulationStyle.properties file with cell shape entries.
//    Properties simStyle = new Properties();
//    simStyle.setProperty("CELLSHAPE.PREFERENCE", "TriangleGridDrawer");
//    simStyle.setProperty("CELLSHAPE.TRIANGULAR", "TriangleGridDrawer");
//    simStyle.setProperty("CELLSHAPE.HEXAGONAL", "HexagonGridDrawer");
//    simStyle.setProperty("CELLSHAPE.SQUARE", "SquareGridDrawer");
//    try (OutputStream output = new FileOutputStream(SIMULATION_STYLE_FILE)) {
//      simStyle.store(output, "Test simulation style - Cell Shapes");
//    }
//    List<String> cellShapes = model.getPossibleCellShapes();
//    assertEquals(3, cellShapes.size(), "Expected three possible cell shapes");
//    assertTrue(cellShapes.contains("TriangleGridDrawer"));
//    assertTrue(cellShapes.contains("HexagonGridDrawer"));
//    assertTrue(cellShapes.contains("SquareGridDrawer"));
//  }
//
//  @Test
//  public void testGetPossibleNeighborArrangements() throws IOException {
//    // Create a SimulationStyle.properties file with neighbor arrangement entries.
//    Properties simStyle = new Properties();
//    simStyle.setProperty("NEIGHBORARRANGEMENT.PREFERENCE", "");
//    simStyle.setProperty("NEIGHBORARRANGEMENT.MOORE", "Moore");
//    simStyle.setProperty("NEIGHBORARRANGEMENT.NEUMANN", "Neumann");
//    simStyle.setProperty("NEIGHBORARRANGEMENT.RING", "Ring");
//    try (OutputStream output = new FileOutputStream(SIMULATION_STYLE_FILE)) {
//      simStyle.store(output, "Test simulation style - Neighbor Arrangements");
//    }
//    List<String> arrangements = model.getPossibleNeighborArrangements();
//    assertEquals(3, arrangements.size(), "Expected three possible neighbor arrangements");
//    assertTrue(arrangements.contains("Moore"));
//    assertTrue(arrangements.contains("Neumann"));
//    assertTrue(arrangements.contains("Ring"));
//  }
//
//  @Test
//  public void testGetPossibleEdgePolicies() throws IOException {
//    // Create a SimulationStyle.properties file with edge policy entries.
//    Properties simStyle = new Properties();
//    simStyle.setProperty("EDGEPOLICY.PREFERENCE", "");
//    simStyle.setProperty("EDGEPOLICY.STANDARD", "STANDARD");
//    simStyle.setProperty("EDGEPOLICY.TORUS", "TORUS");
//    simStyle.setProperty("EDGEPOLICY.MIRROR", "MIRROR");
//    try (OutputStream output = new FileOutputStream(SIMULATION_STYLE_FILE)) {
//      simStyle.store(output, "Test simulation style - Edge Policies");
//    }
//    List<String> edgePolicies = model.getPossibleEdgePolicies();
//    assertEquals(3, edgePolicies.size(), "Expected three possible edge policies");
//    assertTrue(edgePolicies.contains("STANDARD"));
//    assertTrue(edgePolicies.contains("TORUS"));
//    assertTrue(edgePolicies.contains("MIRROR"));
//  }
//
//  @Test
//  public void testSetNeighborArrangement() throws IOException, NoSuchFieldException, IllegalAccessException {
//    // Call the setter with the value "MOORE"
//    model.setNeighborArrangement("MOORE");
//
//    // Verify that the SimulationStyle.properties file now contains the expected property.
//    Properties simProps = new Properties();
//    try (InputStream input = new FileInputStream(SIMULATION_STYLE_FILE)) {
//      simProps.load(input);
//    }
//    assertEquals("MOORE", simProps.getProperty("NEIGHBORARRANGEMENT.PREFERENCE"),
//        "Expected the neighbor arrangement property to be set to MOORE");
//
//    DummyNeighborCalculator dummy = getDummyNeighborCalculator();
//    assertNotNull(dummy.getNeighborType(), "Neighbor type should have been set on the dummy");
//    assertEquals(NeighborType.valueOf("MOORE"), dummy.getNeighborType(),
//        "Expected neighbor type to match the value from the property key");
//  }
//
//  @Test
//  public void testSetEdgePolicy() throws IOException, NoSuchFieldException, IllegalAccessException {
//    // Call the setter with the value "TORUS"
//    model.setEdgePolicy("TORUS");
//
//    // Verify that the SimulationStyle.properties file now contains the expected property.
//    Properties simProps = new Properties();
//    try (InputStream input = new FileInputStream(SIMULATION_STYLE_FILE)) {
//      simProps.load(input);
//    }
//    assertEquals("TORUS", simProps.getProperty("EDGEPOLICY.PREFERENCE"),
//        "Expected the edge policy property to be set to TORUS");
//
//    // Verify that the dummy neighbor calculator received the correct boundary.
//    DummyNeighborCalculator dummy = getDummyNeighborCalculator();
//    assertNotNull(dummy.getEdgeType(), "Boundary type should have been set on the dummy");
//    assertEquals(EdgeType.valueOf("TORUS"), dummy.getEdgeType(),
//        "Expected boundary type to match TORUS");
//  }
//
//  @Test
//  public void testSetCellShape() throws IOException, NoSuchFieldException, IllegalAccessException {
//    // Call the setter with the value "Square" (the code will convert it to uppercase)
//    model.setCellShape("Square");
//
//    // Verify that the SimulationStyle.properties file now contains the expected property.
//    Properties simProps = new Properties();
//    try (InputStream input = new FileInputStream(SIMULATION_STYLE_FILE)) {
//      simProps.load(input);
//    }
//    assertEquals("Square", simProps.getProperty("CELLSHAPE.PREFERENCE"),
//        "Expected the cell shape property to be set to Square");
//
//    // Verify that the dummy neighbor calculator received the correct grid shape.
//    DummyNeighborCalculator dummy = getDummyNeighborCalculator();
//    assertNotNull(dummy.getShape(), "Grid shape should have been set on the dummy");
//    // The code converts cellShape to uppercase, so expect "SQUARE" as enum constant.
//    assertEquals(GridShape.valueOf("SQUARE"), dummy.getShape(),
//        "Expected grid shape to match SQUARE");
//  }
//
//  @Test
//  public void testSetGridOutlinePreference() throws IOException {
//    // Call the setter to set grid outline preference to true.
//    model.setGridOutlinePreference(true);
//
//    // Verify that the SimulationStyle.properties file now contains the expected property.
//    Properties simProps = new Properties();
//    try (InputStream input = new FileInputStream(SIMULATION_STYLE_FILE)) {
//      simProps.load(input);
//    }
//    // gridOutlineProperty is defined as "GRIDOUTLINE.PREFERENCE" in the modelAPI.
//    assertEquals("true", simProps.getProperty("GRIDOUTLINE.PREFERENCE"),
//        "Expected the grid outline preference property to be set to true");
//  }
//
//  /**
//   * Helper method to retrieve the dummy neighbor calculator from the model.
//   */
//  private DummyNeighborCalculator getDummyNeighborCalculator() throws NoSuchFieldException, IllegalAccessException {
//    Field neighborField = ModelApi.class.getDeclaredField("myNeighborCalculator");
//    neighborField.setAccessible(true);
//    return (DummyNeighborCalculator) neighborField.get(model);
//  }
//}



