package modeltests;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.data.cells.Cell;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import cellsociety.model.data.states.State;

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

  @Test
  public void Cell_ConstructorGivenValidState_CurrentStateMatchesGivenState() {
    Cell<TestState> cell = new Cell<>(TestState.ONE);
    assertEquals(TestState.ONE, cell.getCurrentState(), "Expected current state to be ONE");
  }

  @Test
  public void Cell_UpdateNextState_CurrentStateMatchesNextStateAfterUpdate() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    cell.setNextState(TestState.ONE);
    cell.update();
    assertEquals(TestState.ONE, cell.getCurrentState(), "Expected current state to update to ONE");
  }

  @Test
  public void Cell_GetNeighborsWhenSet_ReturnsSetNeighbors() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    Cell<TestState> neighbor1 = new Cell<>(TestState.ONE);
    Cell<TestState> neighbor2 = new Cell<>(TestState.ZERO);
    List<Cell<TestState>> neighbors = new ArrayList<>();
    neighbors.add(neighbor1);
    neighbors.add(neighbor2);
    cell.setNeighbors(neighbors);
    assertEquals(neighbors, cell.getNeighbors(), "Expected neighbors list to match set list");
  }

  @Test
  public void Cell_GetNeighborsWithoutSetting_ReturnsEmptyList() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    assertNotNull(cell.getNeighbors(), "Expected neighbors list not to be null");
    assertTrue(cell.getNeighbors().isEmpty(), "Expected neighbors list to be empty");
  }

  @Test
  public void Cell_SetNeighborsToNull_GetNeighborsThrowsNullPointerExceptionOnIteration() {
    Cell<TestState> cell = new Cell<>(TestState.ZERO);
    cell.setNeighbors(null);
    assertThrows(NullPointerException.class, () -> {
      for (Cell<TestState> neighbor : cell.getNeighbors()) {
      }
    }, "Iterating over null neighbors list should throw NullPointerException");
  }

  @Test
  public void Cell_ConstructorGivenNullState_ThrowsNullPointerExceptionOnUsage() {
    Cell<TestState> cell = new Cell<>(null);
    assertThrows(NullPointerException.class, () -> {
      cell.getCurrentState().toString();
    }, "Using null state should eventually throw NullPointerException");
  }
}
