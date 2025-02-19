package configtests;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.config.CellRecord;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author Billy McCune
 * JUnit tests for the CellRecord record.
 * <p>
 * The test methods follow the naming convention:
 * MethodName_StateUnderTest_ExpectedBehavior
 */
public class CellRecordTest {


  @Test
  public void createCellRecord_StateAndProperties_ReturnsExpectedValues() {
    int state = 1;
    Map<String, Double> properties = Map.of("prop1", 3.14, "prop2", 2.71);
    CellRecord cell = new CellRecord(state, properties);

    assertEquals(state, cell.state(), "The state should match the input state");
    assertEquals(properties, cell.properties(), "The properties should match the input map");
  }


  @Test
  public void equals_SameValues_ReturnsTrue() {
    int state = 2;
    Map<String, Double> properties = Map.of("key", 1.0);
    CellRecord cell1 = new CellRecord(state, properties);
    CellRecord cell2 = new CellRecord(state, properties);

    assertEquals(cell1, cell2, "CellRecords with the same state and properties should be equal");
  }

  @Test
  public void equals_DifferentState_ReturnsFalse() {
    Map<String, Double> properties = Map.of("key", 1.0);
    CellRecord cell1 = new CellRecord(1, properties);
    CellRecord cell2 = new CellRecord(2, properties);

    assertNotEquals(cell1, cell2, "CellRecords with different states should not be equal");
  }

  @Test
  public void equals_DifferentProperties_ReturnsFalse() {
    int state = 1;
    Map<String, Double> properties1 = Map.of("key1", 1.0);
    Map<String, Double> properties2 = Map.of("key2", 2.0);
    CellRecord cell1 = new CellRecord(state, properties1);
    CellRecord cell2 = new CellRecord(state, properties2);

    assertNotEquals(cell1, cell2, "CellRecords with different properties should not be equal");
  }


  @Test
  public void hashCode_SameValues_ReturnsSameHash() {
    int state = 1;
    Map<String, Double> properties = Map.of("key", 1.0);
    CellRecord cell1 = new CellRecord(state, properties);
    CellRecord cell2 = new CellRecord(state, properties);

    assertEquals(cell1.hashCode(), cell2.hashCode(), "Equal CellRecords must have the same hash code");
  }


  @Test
  public void toString_ReturnsNonNullString() {
    int state = 5;
    Map<String, Double> properties = Map.of("a", 1.0, "b", 2.0);
    CellRecord cell = new CellRecord(state, properties);

    String result = cell.toString();
    assertNotNull(result, "toString() should not return null");
    assertTrue(result.contains("state="), "toString() should include the state field");
    assertTrue(result.contains("properties="), "toString() should include the properties field");
  }
}
