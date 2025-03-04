package modeltests;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellQueueRecord;
import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.states.State;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author Jacob You
 */
public class CellTest {

  private enum TestState implements State {
    ZERO(0), ONE(1);

    private final int value;

    TestState(int value) {
      this.value = value;
    }

    @Override
    public int getValue() {
      return value;
    }

    public static TestState fromInt(Class<TestState> enumClass, int value) {
      for (TestState s : enumClass.getEnumConstants()) {
        if (s.getValue() == value) {
          return s;
        }
      }
      throw new IllegalArgumentException("Invalid TestState value: " + value);
    }
  }

  private class DummyCellRecord extends CellQueueRecord {

  }

  @Test
  public void Constructor_NonNullState_CurrentStateIsGiven() {
    Cell<TestState> cell = new Cell<>(TestState.ONE);
    assertEquals(TestState.ONE, cell.getCurrentState());
  }

  @Test
  public void Constructor_NullState_ThrowsException() {
    Cell<TestState> cell = new Cell<>(null);
    assertThrows(NullPointerException.class, () -> cell.getCurrentState().name());
  }

  @Test
  public void GetNeighbors_Default_EmptyMap() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    assertNotNull(cell.getNeighbors());
    assertTrue(cell.getNeighbors().isEmpty());
  }

  @Test
  public void SetNeighbors_ValidMap_GetNeighborsMatches() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    Cell<TestState> n1 = new Cell<>(TestState.ONE);
    Cell<TestState> n2 = new Cell<>(TestState.ZERO);
    Map<Direction, Cell<TestState>> neighborMap = new HashMap<>();
    neighborMap.put(new Direction(0, 1), n1);
    neighborMap.put(new Direction(0, -1), n2);

    cell.setNeighbors(neighborMap);
    assertEquals(neighborMap, cell.getNeighbors());
  }

  @Test
  public void SetNeighbors_Null_ThrowsException() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    cell.setNeighbors(null);
    assertThrows(NullPointerException.class, () -> cell.getNeighbors().size());
  }

  @Test
  public void GetCurrentState_ValidState_ReturnsSame() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    assertEquals(TestState.ZERO, cell.getCurrentState());
  }

  @Test
  public void SetCurrentState_DifferentState_CurrentStateUpdates() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    cell.setCurrentState(TestState.ONE);
    assertEquals(TestState.ONE, cell.getCurrentState());
  }

  @Test
  public void GetNextState_Default_EqualsCurrentState() {
    Cell<TestState> cell = new Cell<>(TestState.ONE);
    assertEquals(TestState.ONE, cell.getNextState());
  }

  @Test
  public void SetNextState_AfterSetting_GetNextStateMatches() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    cell.setNextState(TestState.ONE);
    assertEquals(TestState.ONE, cell.getNextState());
  }

  @Test
  public void Update_NextStateApplied_CurrentStateMatchesNext() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    cell.setNextState(TestState.ONE);
    cell.update();
    assertEquals(TestState.ONE, cell.getCurrentState());
  }

  @Test
  public void SetProperty_NewProperty_StoresValue() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    cell.setProperty("energy", 10.5);
    assertEquals(10.5, cell.getProperty("energy"));
  }

  @Test
  public void SetProperty_OverwriteValue_UpdatesValue() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    cell.setProperty("energy", 5.0);
    cell.setProperty("energy", 7.5);
    assertEquals(7.5, cell.getProperty("energy"));
  }

  @Test
  public void GetProperty_NonExistingKey_ReturnsZero() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    assertEquals(0, cell.getProperty("nonexistent"));
  }

  @Test
  public void SetAllProperties_Null_ClearsProperties() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    cell.setProperty("time", 3.0);
    cell.setAllProperties(null);
    assertEquals(0, cell.getProperty("time"));
  }

  @Test
  public void SetAllProperties_ValidMap_PropertiesAssigned() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    Map<String, Double> props = new HashMap<>();
    props.put("energy", 15.0);
    props.put("speed", 2.5);

    cell.setAllProperties(props);
    assertEquals(15.0, cell.getProperty("energy"));
    assertEquals(2.5, cell.getProperty("speed"));
  }

  @Test
  public void GetAllProperties_EmptyOrNull_ReturnsSameReference() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    assertNull(cell.getAllProperties());

    cell.setProperty("x", 1.0);
    assertNotNull(cell.getAllProperties());
  }

  @Test
  public void CopyAllPropertiesTo_AnotherCell_PropertiesCopied() {
    Cell<TestState> c1 = new Cell<>(TestState.ZERO);
    c1.setProperty("time", 5.0);
    Cell<TestState> c2 = new Cell<>(TestState.ONE);

    c1.copyAllPropertiesTo(c2);
    assertEquals(5.0, c2.getProperty("time"));
  }

  @Test
  public void ClearAllProperties_RemovesAllKeys() {
    Cell<TestState> cell = new Cell<>(TestState.ONE);
    cell.setProperty("energy", 100.0);
    cell.clearAllProperties();
    assertEquals(0, cell.getProperty("energy"));
    assertNull(cell.getAllProperties());
  }

  @Test
  public void SetQueueRecords_CopyingQueue_QueueMatches() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    CellQueueRecord record = new DummyCellRecord();
    cell.addQueueRecord(record);

    Deque<CellQueueRecord> copiedQueue = new ArrayDeque<>(cell.getQueueRecords());
    Cell<TestState> newCell = new Cell<>(TestState.ONE);
    newCell.setQueueRecords(copiedQueue);

    assertEquals(cell.getQueueRecords().size(), newCell.getQueueRecords().size());
    assertEquals(record, newCell.peekQueueRecord());
  }

  @Test
  public void CopyQueueTo_AnotherCell_QueueCopied() {
    Cell<TestState> cell1 = new Cell<>(TestState.ZERO);
    CellQueueRecord record = new DummyCellRecord();
    cell1.addQueueRecord(record);
    Cell<TestState> cell2 = new Cell<>(TestState.ONE);

    cell1.copyQueueTo(cell2);
    assertEquals(cell1.getQueueRecords().size(), cell2.getQueueRecords().size());
    assertEquals(record, cell2.peekQueueRecord());
  }

  @Test
  public void ClearQueueRecords_RemovesAllRecords() {
    Cell<TestState> cell = new Cell<>(TestState.ONE);
    cell.addQueueRecord(new DummyCellRecord());
    cell.clearQueueRecords();

    assertTrue(cell.getQueueRecords().isEmpty());
  }

  @Test
  public void AddQueueRecord_AddsToQueue_QueueSizeIncreases() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    CellQueueRecord record = new DummyCellRecord();
    cell.addQueueRecord(record);

    assertEquals(1, cell.getQueueRecords().size());
    assertEquals(record, cell.peekQueueRecord());
  }

  @Test
  public void RemoveQueueRecord_RemovesMostRecent_QueueSizeDecreases() {
    Cell<TestState> cell = new Cell<>(TestState.ONE);
    CellQueueRecord record1 = new DummyCellRecord();
    CellQueueRecord record2 = new DummyCellRecord();

    cell.addQueueRecord(record1);
    cell.addQueueRecord(record2);
    cell.removeQueueRecord();

    assertEquals(1, cell.getQueueRecords().size());
    assertEquals(record1, cell.peekQueueRecord());
  }
}
