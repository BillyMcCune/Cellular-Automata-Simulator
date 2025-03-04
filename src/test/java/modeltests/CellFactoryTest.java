package modeltests;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import org.junit.jupiter.api.Test;
import cellsociety.model.data.states.State;

/**
 * @author Jacob You
 */
public class CellFactoryTest {

  private enum TestState implements State {
    ZERO(0), ONE(1);
    private final int value;

    TestState(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    public static TestState fromInt(Class<TestState> enumClass, int value) {
      for (TestState s : enumClass.getEnumConstants()) {
        if (s.getValue() == value) {
          return s;
        }
      }
      return enumClass.getEnumConstants()[0];
    }
  }

  @Test
  public void CellFactory_CreateCell_ValidInputZero_ReturnsStateZero() {
    CellFactory<TestState> factory = new CellFactory<>(TestState.class);
    Cell<TestState> cell = factory.createCell(0);
    assertEquals(TestState.ZERO, cell.getCurrentState(), "For input 0, expected state ZERO.");
  }

  @Test
  public void CellFactory_CreateCell_ValidInputOne_ReturnsStateOne() {
    CellFactory<TestState> factory = new CellFactory<>(TestState.class);
    Cell<TestState> cell = factory.createCell(1);
    assertEquals(TestState.ONE, cell.getCurrentState(), "For input 1, expected state ONE.");
  }

  @Test
  public void CellFactory_CreateCell_InvalidInputNegativeOne_ReturnsDefaultState() {
    CellFactory<TestState> factory = new CellFactory<>(TestState.class);
    Cell<TestState> cell = factory.createCell(-1);
    assertEquals(TestState.ZERO, cell.getCurrentState(),
        "Invalid input -1 should return default state ZERO.");
  }

  @Test
  public void CellFactory_CreateCell_InvalidInputTwo_ReturnsDefaultState() {
    CellFactory<TestState> factory = new CellFactory<>(TestState.class);
    Cell<TestState> cell = factory.createCell(2);
    assertEquals(TestState.ZERO, cell.getCurrentState(),
        "Invalid input 2 should return default state ZERO.");
  }

  @Test
  public void CellFactory_CreateCell_InvalidInputMinInt_ReturnsDefaultState() {
    CellFactory<TestState> factory = new CellFactory<>(TestState.class);
    Cell<TestState> cell = factory.createCell(Integer.MIN_VALUE);
    assertEquals(TestState.ZERO, cell.getCurrentState(),
        "Invalid input Integer.MIN_VALUE should return default state ZERO.");
  }

  @Test
  public void CellFactory_CreateCell_InvalidInputMaxInt_ReturnsDefaultState() {
    CellFactory<TestState> factory = new CellFactory<>(TestState.class);
    Cell<TestState> cell = factory.createCell(Integer.MAX_VALUE);
    assertEquals(TestState.ZERO, cell.getCurrentState(),
        "Invalid input Integer.MAX_VALUE should return default state ZERO.");
  }
}
