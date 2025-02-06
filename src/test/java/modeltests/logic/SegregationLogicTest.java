package modeltests.logic;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.logic.SegregationLogic;
import org.junit.jupiter.api.Test;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.states.SegregationState;
import java.util.ArrayList;
import java.util.List;

public class SegregationLogicTest {

  private List<List<Integer>> createRawGrid(int rows, int cols, int value) {
    List<List<Integer>> raw = new ArrayList<>();
    for (int i = 0; i < rows; i++) {
      List<Integer> row = new ArrayList<>();
      for (int j = 0; j < cols; j++) {
        row.add(value);
      }
      raw.add(row);
    }
    return raw;
  }

  private int countState(Grid<SegregationState> grid, SegregationState state) {
    int count = 0;
    for (int r = 0; r < grid.getNumRows(); r++) {
      for (int c = 0; c < grid.getNumCols(); c++) {
        if (grid.getCell(r, c).getCurrentState() == state) {
          count++;
        }
      }
    }
    return count;
  }

  @Test
  public void SegregationLogic_OneRedNoNeighbors_StaysPut() {
    List<List<Integer>> raw = createRawGrid(1, 1, 1);
    CellFactory<SegregationState> factory = new CellFactory<>(SegregationState.class);
    Grid<SegregationState> grid = new Grid<>(raw, factory);
    SegregationLogic.setSatisfiedThreshold(0.8);
    SegregationLogic logic = new SegregationLogic(grid);
    logic.update();
    assertEquals(1, countState(grid, SegregationState.RED));
  }

  @Test
  public void SegregationLogic_OneBlueNoNeighbors_StaysPut() {
    List<List<Integer>> raw = createRawGrid(1, 1, 2);
    CellFactory<SegregationState> factory = new CellFactory<>(SegregationState.class);
    Grid<SegregationState> grid = new Grid<>(raw, factory);
    SegregationLogic.setSatisfiedThreshold(0.8);
    SegregationLogic logic = new SegregationLogic(grid);
    logic.update();
    assertEquals(1, countState(grid, SegregationState.BLUE));
  }

  @Test
  public void SegregationLogic_OneOpenOneRedUnsatisfied_Moves() {
    List<List<Integer>> raw = new ArrayList<>();
    raw.add(List.of(1, 0));
    CellFactory<SegregationState> factory = new CellFactory<>(SegregationState.class);
    Grid<SegregationState> grid = new Grid<>(raw, factory);
    SegregationLogic.setSatisfiedThreshold(1.0);
    SegregationLogic logic = new SegregationLogic(grid);
    logic.update();
    assertEquals(1, countState(grid, SegregationState.RED));
    assertEquals(SegregationState.OPEN, grid.getCell(0, 0).getCurrentState());
  }

  @Test
  public void SegregationLogic_OneOpenOneBlueUnsatisfied_Moves() {
    List<List<Integer>> raw = new ArrayList<>();
    raw.add(List.of(2, 0));
    CellFactory<SegregationState> factory = new CellFactory<>(SegregationState.class);
    Grid<SegregationState> grid = new Grid<>(raw, factory);
    SegregationLogic.setSatisfiedThreshold(1.0);
    SegregationLogic logic = new SegregationLogic(grid);
    logic.update();
    assertEquals(1, countState(grid, SegregationState.BLUE));
    assertEquals(SegregationState.OPEN, grid.getCell(0, 0).getCurrentState());
  }

  @Test
  public void SegregationLogic_NoOpenCells_NoMovementOccurs() {
    List<List<Integer>> raw = new ArrayList<>();
    raw.add(List.of(1, 2));
    raw.add(List.of(1, 2));
    CellFactory<SegregationState> factory = new CellFactory<>(SegregationState.class);
    Grid<SegregationState> grid = new Grid<>(raw, factory);
    SegregationLogic.setSatisfiedThreshold(1.0);
    SegregationLogic logic = new SegregationLogic(grid);
    logic.update();
    assertEquals(2, countState(grid, SegregationState.RED));
    assertEquals(2, countState(grid, SegregationState.BLUE));
  }

  @Test
  public void SegregationLogic_ThresholdZero_NoMovement() {
    List<List<Integer>> raw = new ArrayList<>();
    raw.add(List.of(1, 0));
    raw.add(List.of(0, 2));
    CellFactory<SegregationState> factory = new CellFactory<>(SegregationState.class);
    Grid<SegregationState> grid = new Grid<>(raw, factory);
    SegregationLogic.setSatisfiedThreshold(0.0);
    SegregationLogic logic = new SegregationLogic(grid);
    logic.update();
    assertEquals(1, countState(grid, SegregationState.RED));
    assertEquals(1, countState(grid, SegregationState.BLUE));
    assertEquals(2, countState(grid, SegregationState.OPEN));
  }

  @Test
  public void SegregationLogic_ThresholdGreaterThanOne_ForcesMovementIfPossible() {
    List<List<Integer>> raw = new ArrayList<>();
    raw.add(List.of(1, 0));
    CellFactory<SegregationState> factory = new CellFactory<>(SegregationState.class);
    Grid<SegregationState> grid = new Grid<>(raw, factory);
    SegregationLogic.setSatisfiedThreshold(2.0);
    SegregationLogic logic = new SegregationLogic(grid);
    logic.update();
    assertEquals(1, countState(grid, SegregationState.RED));
    assertEquals(SegregationState.OPEN, grid.getCell(0, 0).getCurrentState());
  }

  @Test
  public void SegregationLogic_RedAndBlueBothUnsatisfiedOneOpen_OneMoves() {
    List<List<Integer>> raw = new ArrayList<>();
    raw.add(List.of(1, 2));
    raw.add(List.of(0, 0));
    CellFactory<SegregationState> factory = new CellFactory<>(SegregationState.class);
    Grid<SegregationState> grid = new Grid<>(raw, factory);
    SegregationLogic.setSatisfiedThreshold(1.0);
    SegregationLogic logic = new SegregationLogic(grid);
    logic.update();
    assertTrue(countState(grid, SegregationState.RED) == 1 && countState(grid, SegregationState.BLUE) == 1);
    assertEquals(2, countState(grid, SegregationState.OPEN));
  }
}
