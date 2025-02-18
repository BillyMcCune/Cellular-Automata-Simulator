package configtests;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.config.ParameterRecord;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author Billy McCune
 * JUnit tests for the ParameterRecord record.
 * <p>
 * The test methods follow the naming convention:
 * MethodName_StateUnderTest_ExpectedBehavior
 */
public class ParameterRecordTest {


  @Test
  public void createParameterRecord_ValidMaps_ReturnsExpectedValues() {
    Map<String, Double> doubleParams = Map.of("param1", 1.23, "param2", 4.56);
    Map<String, String> stringParams = Map.of("str1", "value1", "str2", "value2");
    ParameterRecord record = new ParameterRecord(doubleParams, stringParams);

    assertEquals(doubleParams, record.myDoubleParameters(), "Double parameters should match the input map");
    assertEquals(stringParams, record.myStringParameters(), "String parameters should match the input map");
  }

  @Test
  public void equals_SameValues_ReturnsTrue() {
    Map<String, Double> doubleParams = Map.of("param1", 1.23);
    Map<String, String> stringParams = Map.of("str1", "value1");
    ParameterRecord record1 = new ParameterRecord(doubleParams, stringParams);
    ParameterRecord record2 = new ParameterRecord(doubleParams, stringParams);

    assertEquals(record1, record2, "ParameterRecords with the same values should be equal");
  }


  @Test
  public void equals_DifferentDoubleParameters_ReturnsFalse() {
    ParameterRecord record1 = new ParameterRecord(Map.of("param1", 1.23), Map.of("str1", "value1"));
    ParameterRecord record2 = new ParameterRecord(Map.of("param1", 2.34), Map.of("str1", "value1"));

    assertNotEquals(record1, record2, "ParameterRecords with different double parameters should not be equal");
  }


  @Test
  public void equals_DifferentStringParameters_ReturnsFalse() {
    ParameterRecord record1 = new ParameterRecord(Map.of("param1", 1.23), Map.of("str1", "value1"));
    ParameterRecord record2 = new ParameterRecord(Map.of("param1", 1.23), Map.of("str1", "value2"));

    assertNotEquals(record1, record2, "ParameterRecords with different string parameters should not be equal");
  }

  @Test
  public void hashCode_SameValues_ReturnsSameHash() {
    Map<String, Double> doubleParams = Map.of("param1", 1.23);
    Map<String, String> stringParams = Map.of("str1", "value1");
    ParameterRecord record1 = new ParameterRecord(doubleParams, stringParams);
    ParameterRecord record2 = new ParameterRecord(doubleParams, stringParams);

    assertEquals(record1.hashCode(), record2.hashCode(), "Equal ParameterRecords must have the same hash code");
  }

  @Test
  public void toString_ReturnsNonNullString() {
    ParameterRecord record = new ParameterRecord(Map.of("param1", 1.23), Map.of("str1", "value1"));
    String result = record.toString();

    assertNotNull(result, "toString() should not return null");
    assertTrue(result.contains("myDoubleParameters="), "toString() should include the double parameters field");
    assertTrue(result.contains("myStringParameters="), "toString() should include the string parameters field");
  }
}
