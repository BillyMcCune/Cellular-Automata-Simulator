package ConfigTests;


import cellsociety.model.config.ConfigReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

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
public  class  ConfigReaderTest{
  ConfigReader configReader = new ConfigReader();

  //make sure the getFiles Names is not empty
  @Test
  void getFileNames_isNotEmpty() {
    assertFalse(configReader.getFileNames().isEmpty());
  }
  //TODO create a second test for getFileNames

}