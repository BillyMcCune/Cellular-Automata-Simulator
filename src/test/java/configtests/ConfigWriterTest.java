package configtests;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.config.CellRecord;
import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigInfo.SimulationType;
import cellsociety.model.config.ConfigInfo.cellShapeType;
import cellsociety.model.config.ConfigInfo.gridEdgeType;
import cellsociety.model.config.ConfigWriter;
import cellsociety.model.config.ParameterRecord;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * @author Billy McCune
 * JUnit tests for the ConfigWriter class.
 * <p>
 * The test method names follow the convention:
 * MethodName_StateUnderTest_ExpectedBehavior
 */
public class ConfigWriterTest {

  private ConfigInfo validConfig;

  @BeforeEach
  public void setUp() {
    validConfig = createValidConfig();
  }

  private ConfigInfo createValidConfig() {
    List<List<CellRecord>> grid = List.of(
        List.of(new CellRecord(0, Map.of()), new CellRecord(1, Map.of())),
        List.of(new CellRecord(1, Map.of()), new CellRecord(0, Map.of()))
    );
    ParameterRecord parameters = new ParameterRecord(Map.of("param1", 1.0), Map.of("str", "value"));
    return new ConfigInfo(
        SimulationType.LIFE,
        cellShapeType.SQUARE,
        gridEdgeType.BASE,
        "TestTitle",
        "TestAuthor",
        "TestDescription",
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
  public void saveCurrentConfig_ValidConfig_XmlFileCreated(@TempDir Path tempDir) throws Exception {
    ConfigWriter writer = new ConfigWriter();
    writer.saveCurrentConfig(validConfig, tempDir.toString());
    String lastSaved = writer.getLastFileSaved();
    File savedFile = new File(tempDir.toString(), lastSaved);
    assertTrue(savedFile.exists(), "The saved XML file should exist in the provided directory.");
  }

  @Test
  public void getLastFileSaved_AfterSave_ReturnsFilename(@TempDir Path tempDir) throws Exception {
    ConfigWriter writer = new ConfigWriter();
    writer.saveCurrentConfig(validConfig, tempDir.toString());
    String lastSaved = writer.getLastFileSaved();
    assertNotNull(lastSaved, "Last file saved should not be null after a successful save.");
  }


  @Test
  public void getLastFileSaved_WithoutSave_ThrowsError() {
    ConfigWriter writer = new ConfigWriter();
    assertThrows(Error.class, writer::getLastFileSaved,
        "Expected an Error when getLastFileSaved() is called before any save operation.");
  }


  @Test
  public void saveCurrentConfig_NullConfig_ThrowsNullPointerException(@TempDir Path tempDir) {
    ConfigWriter writer = new ConfigWriter();
    assertThrows(NullPointerException.class, () -> writer.saveCurrentConfig(null, tempDir.toString()),
        "Expected a NullPointerException when saving a null configuration.");
  }

  @Test
  public void saveCurrentConfig_NullPath_ThrowsNullPointerException() {
    ConfigWriter writer = new ConfigWriter();
    assertThrows(NullPointerException.class, () -> writer.saveCurrentConfig(validConfig, null),
        "Expected a NullPointerException when the provided path is null.");
  }

  @Test
  public void saveCurrentConfig_ConfigWithNullParameters_ThrowsNullPointerException(@TempDir Path tempDir) {
    ConfigInfo configWithNullParams = new ConfigInfo(
        validConfig.myType(),
        validConfig.myCellShapeType(),
        validConfig.myGridEdgeType(),
        validConfig.myTitle(),
        validConfig.myAuthor(),
        validConfig.myDescription(),
        validConfig.myGridWidth(),
        validConfig.myGridHeight(),
        validConfig.myTickSpeed(),
        validConfig.myGrid(),
        null, // null parameters
        validConfig.acceptedStates(),
        validConfig.myFileName()
    );
    ConfigWriter writer = new ConfigWriter();
    assertThrows(NullPointerException.class, () -> writer.saveCurrentConfig(configWithNullParams, tempDir.toString()),
        "Expected a NullPointerException when the configuration parameters are null.");
  }


  @Test
  public void saveCurrentConfig_ConfigWithNullGrid_ThrowsNullPointerException(@TempDir Path tempDir) {
    ConfigInfo configWithNullGrid = new ConfigInfo(
        validConfig.myType(),
        validConfig.myCellShapeType(),
        validConfig.myGridEdgeType(),
        validConfig.myTitle(),
        validConfig.myAuthor(),
        validConfig.myDescription(),
        validConfig.myGridWidth(),
        validConfig.myGridHeight(),
        validConfig.myTickSpeed(),
        null, // null grid
        validConfig.myParameters(),
        validConfig.acceptedStates(),
        validConfig.myFileName()
    );
    ConfigWriter writer = new ConfigWriter();
    assertThrows(NullPointerException.class, () -> writer.saveCurrentConfig(configWithNullGrid, tempDir.toString()),
        "Expected a NullPointerException when the configuration grid is null.");
  }


  @Test
  public void saveCurrentConfig_ConfigWithNullAcceptedStates_ThrowsNullPointerException(@TempDir Path tempDir) {
    ConfigInfo configWithNullAcceptedStates = new ConfigInfo(
        validConfig.myType(),
        validConfig.myCellShapeType(),
        validConfig.myGridEdgeType(),
        validConfig.myTitle(),
        validConfig.myAuthor(),
        validConfig.myDescription(),
        validConfig.myGridWidth(),
        validConfig.myGridHeight(),
        validConfig.myTickSpeed(),
        validConfig.myGrid(),
        validConfig.myParameters(),
        null, // null accepted states
        validConfig.myFileName()
    );
    ConfigWriter writer = new ConfigWriter();
    assertThrows(NullPointerException.class,
        () -> writer.saveCurrentConfig(configWithNullAcceptedStates, tempDir.toString()),
        "Expected a NullPointerException when the accepted states are null.");
  }


  @Test
  public void saveCurrentConfig_PathIsFile_ThrowsParserConfigurationException(@TempDir Path tempDir) throws Exception {
    // Create a temporary file and use its path as the directory.
    File tempFile = File.createTempFile("temp", ".txt", tempDir.toFile());
    assertTrue(tempFile.exists(), "The temporary file should exist.");
    ConfigWriter writer = new ConfigWriter();
    assertThrows(ParserConfigurationException.class,
        () -> writer.saveCurrentConfig(validConfig, tempFile.getAbsolutePath()),
        "Expected a ParserConfigurationException when the provided path is a file instead of a directory.");
  }
}
