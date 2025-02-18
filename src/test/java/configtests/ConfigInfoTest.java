package configtests;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.config.CellRecord;
import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigInfo.SimulationType;
import cellsociety.model.config.ParameterRecord;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * @author Billy McCune
 * JUnit tests for the ConfigInfo record.
 * <p>
 * The test method names follow the convention:
 * MethodName_StateUnderTest_ExpectedBehavior
 */
public class ConfigInfoTest {

  /**
   * Helper method to create a valid ConfigInfo instance.
   *
   * @return a valid ConfigInfo instance
   */
  private ConfigInfo createValidConfigInfo() {
    List<List<CellRecord>> grid = List.of(
        List.of(new CellRecord(0, Map.of()), new CellRecord(1, Map.of("prop", 1.0))),
        List.of(new CellRecord(1, Map.of()), new CellRecord(0, Map.of("prop", 2.0)))
    );
    ParameterRecord parameters = new ParameterRecord(Map.of("param", 3.14), Map.of("str", "test"));
    return new ConfigInfo(
        SimulationType.LIFE,
        "Test Simulation",
        "Test Author",
        "Test Description",
        2,
        2,
        10,
        grid,
        parameters,
        Set.of(0, 1),
        "TestFile.xml"
    );
  }

  @Test
  @DisplayName("createConfigInfo_ValidInputs_ReturnsExpectedValues")
  public void createConfigInfo_ValidInputs_ReturnsExpectedValues() {
    ConfigInfo config = createValidConfigInfo();

    assertEquals(SimulationType.LIFE, config.myType());
    assertEquals("Test Simulation", config.myTitle());
    assertEquals("Test Author", config.myAuthor());
    assertEquals("Test Description", config.myDescription());
    assertEquals(2, config.myGridWidth());
    assertEquals(2, config.myGridHeight());
    assertEquals(10, config.myTickSpeed());
    // Validate grid dimensions: expect 2 rows, each with 2 cells
    assertEquals(2, config.myGrid().size());
    config.myGrid().forEach(row -> assertEquals(2, row.size()));
    // Validate accepted states and file name
    assertEquals(Set.of(0, 1), config.acceptedStates());
    assertEquals("TestFile.xml", config.myFileName());
  }


  @Test
  @DisplayName("equals_SameValues_ReturnsTrue")
  public void equals_SameValues_ReturnsTrue() {
    ConfigInfo config1 = createValidConfigInfo();
    ConfigInfo config2 = createValidConfigInfo();

    assertEquals(config1, config2, "ConfigInfo instances with identical values should be equal");
  }

  @Test
  @DisplayName("equals_DifferentTitle_ReturnsFalse")
  public void equals_DifferentTitle_ReturnsFalse() {
    ConfigInfo config1 = createValidConfigInfo();
    ConfigInfo config2 = new ConfigInfo(
        config1.myType(),
        "Different Title", // change title
        config1.myAuthor(),
        config1.myDescription(),
        config1.myGridWidth(),
        config1.myGridHeight(),
        config1.myTickSpeed(),
        config1.myGrid(),
        config1.myParameters(),
        config1.acceptedStates(),
        config1.myFileName()
    );

    assertNotEquals(config1, config2, "ConfigInfo instances with different titles should not be equal");
  }


  @Test
  @DisplayName("hashCode_SameValues_ReturnsSameHash")
  public void hashCode_SameValues_ReturnsSameHash() {
    ConfigInfo config1 = createValidConfigInfo();
    ConfigInfo config2 = createValidConfigInfo();

    assertEquals(config1.hashCode(), config2.hashCode(), "Equal ConfigInfo instances must have the same hash code");
  }


  @Test
  @DisplayName("toString_ReturnsNonNullString")
  public void toString_ReturnsNonNullString() {
    ConfigInfo config = createValidConfigInfo();
    String result = config.toString();

    assertNotNull(result, "toString() should not return null");
    assertTrue(result.contains("myTitle="), "toString() should include the title field");
    assertTrue(result.contains("myAuthor="), "toString() should include the author field");
  }


  @ParameterizedTest(name = "Tick speed: {0}")
  @CsvSource({
      "5",
      "10",
      "15"
  })
  @DisplayName("parameterizedTest_TickSpeed_ReturnsExpectedTickSpeed")
  public void parameterizedTest_TickSpeed_ReturnsExpectedTickSpeed(int tickSpeed) {
    ConfigInfo config = new ConfigInfo(
        SimulationType.FIRE,
        "Simulation",
        "Author",
        "Description",
        3,
        3,
        tickSpeed,
        List.of(List.of(new CellRecord(0, Map.of()))),
        new ParameterRecord(Map.of(), Map.of()),
        Set.of(0),
        "File.xml"
    );

    assertEquals(tickSpeed, config.myTickSpeed(), "Tick speed should match the parameterized value");
  }
}
