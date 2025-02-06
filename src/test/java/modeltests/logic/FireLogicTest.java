package modeltests.logic;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.logic.FireLogic;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.states.FireState;

public class FireLogicTest {

  private List<List<Integer>> createRawGrid(int rows, int cols, int defaultValue) {
    List<List<Integer>> rawGrid = new ArrayList<>();
    for (int i = 0; i < rows; i++) {
      List<Integer> row = new ArrayList<>();
      for (int j = 0; j < cols; j++) {
        row.add(defaultValue);
      }
      rawGrid.add(row);
    }
    return rawGrid;
  }

  private Grid<FireState> createGrid(List<List<Integer>> rawGrid) {
    CellFactory<FireState> factory = new CellFactory<>(FireState.class);
    return new Grid<>(rawGrid, factory);
  }

  @Test
  public void FireLogic_Update_BurningCellWithTreeNeighbors_AllTreeNeighborsCatchFire() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(1, 1, 1));
    rawGrid.add(List.of(1, 2, 1));
    rawGrid.add(List.of(1, 1, 1));
    Grid<FireState> grid = createGrid(rawGrid);
    FireLogic logic = new FireLogic(grid);
    FireLogic.setProbCatch(1.0);
    logic.update();
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(FireState.BURNING, grid.getCell(i, j).getCurrentState());
      }
    }
  }

  @Test
  public void FireLogic_Update_BurningCellWithTreeNeighbors_NoPropagationWhenProbabilityZero() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(1, 1, 1));
    rawGrid.add(List.of(1, 2, 1));
    rawGrid.add(List.of(1, 1, 1));
    Grid<FireState> grid = createGrid(rawGrid);
    FireLogic logic = new FireLogic(grid);
    FireLogic.setProbCatch(0.0);
    logic.update();
    assertEquals(FireState.BURNING, grid.getCell(1, 1).getCurrentState());
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        if (i == 1 && j == 1) continue;
        assertEquals(FireState.TREE, grid.getCell(i, j).getCurrentState());
      }
    }
  }

  @Test
  public void FireLogic_Update_BurningCellWithNoTreeNeighbors_NoPropagationOccurs() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(0, 0, 0));
    rawGrid.add(List.of(0, 2, 0));
    rawGrid.add(List.of(0, 0, 0));
    Grid<FireState> grid = createGrid(rawGrid);
    FireLogic logic = new FireLogic(grid);
    FireLogic.setProbCatch(1.0);
    logic.update();
    assertEquals(FireState.BURNING, grid.getCell(1, 1).getCurrentState());
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        if (i == 1 && j == 1) continue;
        assertEquals(FireState.EMPTY, grid.getCell(i, j).getCurrentState());
      }
    }
  }

  @Test
  public void FireLogic_Update_NonBurningCell_DoesNotPropagateFire() {
    List<List<Integer>> rawGrid = createRawGrid(3, 3, 1);
    Grid<FireState> grid = createGrid(rawGrid);
    FireLogic logic = new FireLogic(grid);
    FireLogic.setProbCatch(1.0);
    logic.update();
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(FireState.TREE, grid.getCell(i, j).getCurrentState());
      }
    }
  }

  @Test
  public void FireLogic_Update_MixedCells_OnlyTreeNeighborsCatchFire() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(0, 1, 1));
    rawGrid.add(List.of(1, 2, 1));
    rawGrid.add(List.of(1, 1, 0));
    Grid<FireState> grid = createGrid(rawGrid);
    FireLogic logic = new FireLogic(grid);
    FireLogic.setProbCatch(1.0);
    logic.update();
    assertEquals(FireState.EMPTY, grid.getCell(0, 0).getCurrentState());
    assertEquals(FireState.BURNING, grid.getCell(0, 1).getCurrentState());
    assertEquals(FireState.BURNING, grid.getCell(0, 2).getCurrentState());
    assertEquals(FireState.BURNING, grid.getCell(1, 0).getCurrentState());
    assertEquals(FireState.BURNING, grid.getCell(1, 1).getCurrentState());
    assertEquals(FireState.BURNING, grid.getCell(1, 2).getCurrentState());
    assertEquals(FireState.BURNING, grid.getCell(2, 0).getCurrentState());
    assertEquals(FireState.BURNING, grid.getCell(2, 1).getCurrentState());
    assertEquals(FireState.EMPTY, grid.getCell(2, 2).getCurrentState());
  }

  @Test
  public void FireLogic_Update_ProbCatchNegative_NoPropagationOccurs() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(1, 1));
    rawGrid.add(List.of(2, 1));
    Grid<FireState> grid = createGrid(rawGrid);
    FireLogic logic = new FireLogic(grid);
    FireLogic.setProbCatch(-0.5);
    logic.update();
    assertEquals(FireState.BURNING, grid.getCell(1, 0).getCurrentState());
    assertEquals(FireState.TREE, grid.getCell(0, 0).getCurrentState());
    assertEquals(FireState.TREE, grid.getCell(0, 1).getCurrentState());
    assertEquals(FireState.TREE, grid.getCell(1, 1).getCurrentState());
  }

  @Test
  public void FireLogic_Update_ProbCatchGreaterThanOne_PropagatesToAllTreeNeighbors() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(1, 1, 1));
    rawGrid.add(List.of(1, 2, 1));
    rawGrid.add(List.of(1, 1, 1));
    Grid<FireState> grid = createGrid(rawGrid);
    FireLogic logic = new FireLogic(grid);
    FireLogic.setProbCatch(1.5);
    logic.update();
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(FireState.BURNING, grid.getCell(i, j).getCurrentState());
      }
    }
  }
}
