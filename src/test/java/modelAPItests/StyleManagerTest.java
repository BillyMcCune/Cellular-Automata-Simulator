package modelAPItests;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.modelAPI.StyleManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StyleManagerTest {

  private static final String FILENAME = "SimulationStyle.properties";
  private File propertiesFile;
  private StyleManager styleManager;

  @BeforeEach
  public void setUp() throws Exception {
    // Create a temporary SimulationStyle.properties file with known test values.
    propertiesFile = new File(FILENAME);
    Properties testProperties = new Properties();

    // Neighbor Arrangement properties.
    testProperties.setProperty("NEIGHBORARRANGEMENT.ONE", "MOORE");
    testProperties.setProperty("NEIGHBORARRANGEMENT.TWO", "VONNEUMANN");
    testProperties.setProperty("NEIGHBORARRANGEMENT.PREFERENCE", "MOORE");

    // Edge Policy properties.
    testProperties.setProperty("EDGEPOLICY.ONE", "FINITE");
    testProperties.setProperty("EDGEPOLICY.TWO", "TOROIDAL");
    testProperties.setProperty("EDGEPOLICY.PREFERENCE", "FINITE");

    // Cell Shape properties.
    testProperties.setProperty("CELLSHAPE.ONE", "SQUARE");
    testProperties.setProperty("CELLSHAPE.TWO", "HEXAGON");
    testProperties.setProperty("CELLSHAPE.PREFERENCE", "SQUARE");

    // Grid Outline preference.
    testProperties.setProperty("GRIDOUTLINE.PREFERENCE", "true");

    try (OutputStream output = new FileOutputStream(propertiesFile)) {
      testProperties.store(output, "Test properties for StyleManager");
    }

    // Instantiate the StyleManager (which will use the external file).
    styleManager = new StyleManager();
  }

  @AfterEach
  public void tearDown() {
    if (propertiesFile.exists()) {
      propertiesFile.delete();
    }
  }

  @Test
  public void getPossibleNeighborArrangements_DefaultProperties_ReturnsListWithMooreAndVonneumann() {
    // Tested Method: getPossibleNeighborArrangements()
    // State: Using default properties from the temporary file.
    // Expected Outcome: The returned list is non-null, contains "MOORE" and "VONNEUMANN", and has exactly 2 elements.
    List<String> arrangements = styleManager.getPossibleNeighborArrangements();
    assertNotNull(arrangements, "Neighbor arrangements should not be null");
    assertTrue(arrangements.contains("MOORE"), "Expected to contain MOORE");
    assertTrue(arrangements.contains("VONNEUMANN"), "Expected to contain VONNEUMANN");
    assertEquals(2, arrangements.size(), "Expected exactly 2 neighbor arrangements");
  }

  @Test
  public void setNeighborArrangement_ValidInput_UpdatesPreferenceToVonneumannInPropertiesFile() throws Exception {
    // Tested Method: setNeighborArrangement()
    // State: Input value "VONNEUMANN" is provided.
    // Expected Outcome: The NEIGHBORARRANGEMENT.PREFERENCE in the properties file is updated to "VONNEUMANN".
    styleManager.setNeighborArrangement("VONNEUMANN");
    Properties updatedProperties = new Properties();
    try (FileInputStream fis = new FileInputStream(propertiesFile)) {
      updatedProperties.load(fis);
    }
    assertEquals("VONNEUMANN", updatedProperties.getProperty("NEIGHBORARRANGEMENT.PREFERENCE"),
        "Neighbor arrangement preference should be updated to VONNEUMANN");
  }

  @Test
  public void getPossibleEdgePolicies_DefaultProperties_ReturnsListWithFiniteAndToroidal() {
    // Tested Method: getPossibleEdgePolicies()
    // State: Using default properties.
    // Expected Outcome: The returned list is non-null, contains "FINITE" and "TOROIDAL", and has exactly 2 elements.
    List<String> policies = styleManager.getPossibleEdgePolicies();
    assertNotNull(policies, "Edge policies should not be null");
    assertTrue(policies.contains("FINITE"), "Expected to contain FINITE");
    assertTrue(policies.contains("TOROIDAL"), "Expected to contain TOROIDAL");
    assertEquals(2, policies.size(), "Expected exactly 2 edge policies");
  }

  @Test
  public void setEdgePolicy_ValidInput_UpdatesPreferenceToTorusInPropertiesFile() throws Exception {
    // Tested Method: setEdgePolicy()
    // State: Input value "TORUS" is provided.
    // Expected Outcome: The EDGEPOLICY.PREFERENCE in the properties file is updated to "TORUS".
    styleManager.setEdgePolicy("TORUS");
    Properties updatedProperties = new Properties();
    try (FileInputStream fis = new FileInputStream(propertiesFile)) {
      updatedProperties.load(fis);
    }
    assertEquals("TORUS", updatedProperties.getProperty("EDGEPOLICY.PREFERENCE"),
        "Edge policy preference should be updated to TORUS");
  }

  @Test
  public void getPossibleCellShapes_DefaultProperties_ReturnsListWithSquareAndHexagon() {
    // Tested Method: getPossibleCellShapes()
    // State: Using default properties.
    // Expected Outcome: The returned list is non-null, contains "SQUARE" and "HEXAGON", and has exactly 2 elements.
    List<String> shapes = styleManager.getPossibleCellShapes();
    assertNotNull(shapes, "Cell shapes should not be null");
    assertTrue(shapes.contains("SQUARE"), "Expected to contain SQUARE");
    assertTrue(shapes.contains("HEXAGON"), "Expected to contain HEXAGON");
    assertEquals(2, shapes.size(), "Expected exactly 2 cell shapes");
  }

  @Test
  public void setCellShape_LowercaseInput_UpdatesPreferenceToUppercaseHexagon() throws Exception {
    // Tested Method: setCellShape()
    // State: Input value "hexagon" in lowercase.
    // Expected Outcome: The CELLSHAPE.PREFERENCE in the properties file is updated to "HEXAGON" (uppercase).
    styleManager.setCellShape("hexagon");
    Properties updatedProperties = new Properties();
    try (FileInputStream fis = new FileInputStream(propertiesFile)) {
      updatedProperties.load(fis);
    }
    assertEquals("HEXAGON", updatedProperties.getProperty("CELLSHAPE.PREFERENCE"),
        "Cell shape preference should be updated to uppercase HEXAGON");
  }

  @Test
  public void setGridOutlinePreference_FalseInput_UpdatesPreferenceToFalseInPropertiesFile() throws Exception {
    // Tested Method: setGridOutlinePreference()
    // State: Input value false is provided.
    // Expected Outcome: The GRIDOUTLINE.PREFERENCE in the properties file is updated to "false".
    styleManager.setGridOutlinePreference(false);
    Properties updatedProperties = new Properties();
    try (FileInputStream fis = new FileInputStream(propertiesFile)) {
      updatedProperties.load(fis);
    }
    assertEquals("false", updatedProperties.getProperty("GRIDOUTLINE.PREFERENCE"),
        "Grid outline preference should be updated to false");
  }

  @Test
  public void getGridOutlinePreference_DefaultProperties_ReturnsTrue() {
    // Tested Method: getGridOutlinePreference()
    // State: Using default properties.
    // Expected Outcome: The grid outline preference is true.
    boolean pref = styleManager.getGridOutlinePreference();
    assertTrue(pref, "Grid outline preference should be true");
  }

  @Test
  public void getNeighborArrangementPreference_DefaultProperties_ReturnsMoore() {
    // Tested Method: getNeighborArrangementPreference()
    // State: Using default properties.
    // Expected Outcome: The neighbor arrangement preference is "MOORE".
    String pref = styleManager.getNeighborArrangementPreference();
    assertEquals("MOORE", pref, "Expected neighbor arrangement preference to be MOORE");
  }

  @Test
  public void getEdgePolicyPreference_DefaultProperties_ReturnsFinite() {
    // Tested Method: getEdgePolicyPreference()
    // State: Using default properties.
    // Expected Outcome: The edge policy preference is "FINITE".
    String pref = styleManager.getEdgePolicyPreference();
    assertEquals("FINITE", pref, "Expected edge policy preference to be FINITE");
  }

  @Test
  public void getCellShapePreference_DefaultProperties_ReturnsSquare() {
    // Tested Method: getCellShapePreference()
    // State: Using default properties.
    // Expected Outcome: The cell shape preference is "SQUARE".
    String pref = styleManager.getCellShapePreference();
    assertEquals("SQUARE", pref, "Expected cell shape preference to be SQUARE");
  }

  @Test
  public void loadProperties_MissingResource_DefaultResourceCopiedAndPropertiesLoaded() {
    // Tested Behavior: Loading properties when the external file is missing.
    // State: The external properties file is deleted.
    // Expected Outcome: A new properties file is created by copying the default resource, and properties are available.
    if (propertiesFile.exists()) {
      propertiesFile.delete();
    }
    StyleManager sm = new StyleManager();
    assertTrue(propertiesFile.exists(), "Properties file should exist after default resource copy");
    List<String> shapes = sm.getPossibleCellShapes();
    assertNotNull(shapes, "Expected a non-null list from default resource");
    assertFalse(shapes.isEmpty(), "Expected a non-empty list from default resource");
  }
}
