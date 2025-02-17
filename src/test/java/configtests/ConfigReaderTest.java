package configtests;


import cellsociety.model.config.ConfigReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import javax.xml.parsers.ParserConfigurationException;



/**
 * Tests for ConfigReader class.
 *
 * @author Billy McCune
 */

/**
 * Rules for Test Document
 * Write as many tests as you can think of to verify the code and, in the process, find any bugs or other issues with the code using these steps:
 *
 * Add a new JUnit test to verify whether or not the code works as intended
 * Run all the tests to verify they all continue to pass (both the old and new tests show green)
 * If you find a bug: (i.e., a test fails by showing red)
 * Comment out the buggy code
 * Write a comment telling the cause of the error
 * Write the correct code that fixes the error
 * Again, run all the tests to verify they all continue to pass
 * Each test should be:
 *
 * annotated with @Test
 * written in separate methods, that follow these naming standards to show what you are intending to test
 * commented as needed to show how the chosen input values relate to the test's goal
 * simple enough that you know the expected output values for each test before you run the code
 * Use the ZOMBIES acronym to help you remember to consider these questions when coming up with useful scenarios to test the program:
 *
 */
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

public class ConfigReaderTest {

  ConfigReader configReader = new ConfigReader();

  // Ensures that getFileNames returns a non-empty list when config directory has files
  @Test
  void getFileNames_ConfigDirectoryHasFiles_IsNotEmpty() {
    assertFalse(configReader.getFileNames().isEmpty());
  }

  @Test
  void readConfig_InvalidGridBounds_ThrowsIllegalArgumentException() {
    Exception e = assertThrows(IllegalArgumentException.class, () -> configReader.readConfig("ErrorGridBounds.xml"));
    System.out.println("[ErrorGridBounds.xml] " + e.getMessage());
  }

  @Test
  void readConfig_CellMissingStateAttribute_ThrowsIllegalArgumentException() {
    Exception e = assertThrows(IllegalArgumentException.class, () -> configReader.readConfig("ErrorCellMissingStateAttribute.xml"));
    System.out.println("[ErrorCellMissingStateAttribute.xml] " + e.getMessage());
  }

  @Test
  void readConfig_MissingTitleParameter_ThrowsIllegalArgumentException() {
    Exception e = assertThrows(IllegalArgumentException.class, () -> configReader.readConfig("ErrorMissingTitleParameter.xml"));
    System.out.println("[ErrorMissingTitleParameter.xml] " + e.getMessage());
  }

  @Test
  void readConfig_ProportionsExceedLimit_ThrowsIllegalArgumentException() {
    Exception e = assertThrows(IllegalArgumentException.class, () -> configReader.readConfig("ErrorProportionsExceedLimit.xml"));
    System.out.println("[ErrorProportionsExceedLimit.xml] " + e.getMessage());
  }

  @Test
  void readConfig_ParamMissingName_ThrowsIllegalArgumentException() {
    Exception e = assertThrows(IllegalArgumentException.class, () -> configReader.readConfig("ErrorParamMissingName.xml"));
    System.out.println("[ErrorParamMissingName.xml] " + e.getMessage());
  }

  @Test
  void readConfig_TooManyTotalStates_ThrowsIllegalArgumentException() {
    Exception e = assertThrows(IllegalArgumentException.class, () -> configReader.readConfig("ErrorTooManyTotalStates.xml"));
    System.out.println("[ErrorTooManyTotalStates.xml] " + e.getMessage());
  }

  @Test
  void readConfig_EmptyInitialCells_ThrowsIllegalArgumentException() {
    Exception e = assertThrows(IllegalArgumentException.class, () -> configReader.readConfig("ErrorEmptyInitialCells.xml"));
    System.out.println("[ErrorEmptyInitialCells.xml] " + e.getMessage());
  }

  @Test
  void readConfig_InvalidAcceptedStateValue_ThrowsIllegalArgumentException() {
    Exception e = assertThrows(IllegalArgumentException.class, () -> configReader.readConfig("ErrorInvalidAcceptedStateValue.xml"));
    System.out.println("[ErrorInvalidAcceptedStateValue.xml] " + e.getMessage());
  }

  @Test
  void readConfig_InvalidValue_ThrowsIllegalArgumentException() {
    Exception e = assertThrows(IllegalArgumentException.class, () -> configReader.readConfig("ErrorInvalidValue.xml"));
    System.out.println("[ErrorInvalidValue.xml] " + e.getMessage());
  }

  @Test
  void readConfig_MultipleGridConfigurationElements_ThrowsParserConfigurationException() {
    Exception e = assertThrows(ParserConfigurationException.class, () -> configReader.readConfig("ErrorMultipleGridConfigurationElements.xml"));
    System.out.println("[ErrorMultipleGridConfigurationElements.xml] " + e.getMessage());
  }

  @Test
  void readConfig_InvalidProportionValue_ThrowsIllegalArgumentException() {
    Exception e = assertThrows(IllegalArgumentException.class, () -> configReader.readConfig("ErrorInvalidProportionValue.xml"));
    System.out.println("[ErrorInvalidProportionValue.xml] " + e.getMessage());
  }

  @Test
  void readConfig_InvalidValueInDoubleParam_ThrowsIllegalArgumentException() {
    Exception e = assertThrows(IllegalArgumentException.class, () -> configReader.readConfig("ErrorInvalidValueInDoubleParam.xml"));
    System.out.println("[ErrorInvalidValueInDoubleParam.xml] " + e.getMessage());
  }

  @Test
  void readConfig_InvalidCellState_ThrowsIllegalArgumentException() {
    Exception e = assertThrows(IllegalArgumentException.class, () -> configReader.readConfig("ErrorInvalidCellState.xml"));
    System.out.println("[ErrorInvalidCellState.xml] " + e.getMessage());
  }

  @Test
  void readConfig_EmptyRow_ThrowsIllegalArgumentException() {
    Exception e = assertThrows(IllegalArgumentException.class, () -> configReader.readConfig("ErrorEmptyRow.xml"));
    System.out.println("[ErrorEmptyRow.xml] " + e.getMessage());
  }

  @Test
  void readConfig_AcceptedStatesEmpty_ThrowsIllegalArgumentException() {
    Exception e = assertThrows(IllegalArgumentException.class, () -> configReader.readConfig("ErrorAcceptedStatesEmpty.xml"));
    System.out.println("[ErrorAcceptedStatesEmpty.xml] " + e.getMessage());
  }
}
