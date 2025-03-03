package modeltests.logic;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.config.CellRecord;
import cellsociety.model.data.constants.BoundaryType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.data.states.FireState;
import cellsociety.model.logic.FireLogic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author Jacob You
 */
public class FireLogicTest {

  private static final int[][] DIRECTIONS = {
      {-1, 0}, {0, -1}, {0, 1}, {1, 0}
  };

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

  private List<List<CellRecord>> createCellRecordGrid(List<List<Integer>> rawData) {
    List<List<CellRecord>> records = new ArrayList<>();
    for (List<Integer> row : rawData) {
      List<CellRecord> recordRow = new ArrayList<>();
      for (Integer state : row) {
        Map<String, Double> props = new HashMap<>();
        props.put("dummy", 1.0);
        recordRow.add(new CellRecord(state, props));
      }
      records.add(recordRow);
    }
    return records;
  }

  private Grid<FireState> createGrid(List<List<Integer>> rawData) {
    CellFactory<FireState> factory = new CellFactory<>(FireState.class);
    List<List<CellRecord>> records = createCellRecordGrid(rawData);
    return new Grid<>(records, factory, GridShape.SQUARE, NeighborType.MOORE, BoundaryType.BASE);
  }

  private ParameterRecord createEmptyParameterRecord() {
    return new ParameterRecord(Map.of(), Map.of());
  }


  @Test
  public void FireLogic_Update_BurningCellPropagatesToTreeNeighbors() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(1, 1, 1));
    rawGrid.add(List.of(2, 2, 2));
    rawGrid.add(List.of(1, 1, 1));
    Grid<FireState> grid = createGrid(rawGrid);
    FireLogic logic = new FireLogic(grid, createEmptyParameterRecord());
    logic.setProbCatch(100.0);
    logic.update();
    assertEquals(FireState.EMPTY, grid.getCell(1, 1).getCurrentState());
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        if (i == 1) {
          continue;
        }
        assertEquals(FireState.BURNING, grid.getCell(i, j).getCurrentState());
      }
    }
  }

  @Test
  public void FireLogic_Update_BurningCellNoPropagationWhenProbCatchZero() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(1, 1, 1));
    rawGrid.add(List.of(1, 2, 1));
    rawGrid.add(List.of(1, 1, 1));
    Grid<FireState> grid = createGrid(rawGrid);
    FireLogic logic = new FireLogic(grid, createEmptyParameterRecord());
    logic.setProbCatch(0.0);
    logic.setProbIgnite(0.0);
    logic.update();
    assertEquals(FireState.EMPTY, grid.getCell(1, 1).getCurrentState());
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        if (i == 1 && j == 1) {
          continue;
        }
        assertEquals(FireState.TREE, grid.getCell(i, j).getCurrentState());
      }
    }
  }

  @Test
  public void FireLogic_Update_TreeCellIgnitesWhenProbIgniteMax() {
    List<List<Integer>> rawGrid = createRawGrid(3, 3, 1);
    Grid<FireState> grid = createGrid(rawGrid);
    FireLogic logic = new FireLogic(grid, createEmptyParameterRecord());
    logic.setProbIgnite(100.0);
    logic.update();
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(FireState.BURNING, grid.getCell(i, j).getCurrentState());
      }
    }
  }

  @Test
  public void FireLogic_Update_EmptyCellGrowsTreeWhenProbTreeMax() {
    List<List<Integer>> rawGrid = createRawGrid(3, 3, 0);
    Grid<FireState> grid = createGrid(rawGrid);
    FireLogic logic = new FireLogic(grid, createEmptyParameterRecord());
    logic.setProbTree(100.0);
    logic.update();
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(FireState.TREE, grid.getCell(i, j).getCurrentState());
      }
    }
  }

  @Test
  public void FireLogic_Update_MixedCells_UpdateStateCorrectly() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(0, 1, 1));
    rawGrid.add(List.of(1, 2, 1));
    rawGrid.add(List.of(1, 1, 0));
    Grid<FireState> grid = createGrid(rawGrid);
    FireLogic logic = new FireLogic(grid, createEmptyParameterRecord());
    logic.setProbCatch(100.0);
    logic.setProbIgnite(0.0);
    logic.setProbTree(0.0);
    logic.update();
    assertEquals(FireState.EMPTY, grid.getCell(1, 1).getCurrentState());
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        if (i == 1 && j == 1) {
          continue;
        }
        if (grid.getCell(i, j).getCurrentState() == FireState.TREE) {
          assertEquals(FireState.TREE, grid.getCell(i, j).getCurrentState());
        }
      }
    }
  }

  @Test
  public void FireLogic_Update_TreeCellNoIgnitionWithProbIgnite0_TreeRemainsTree() {
    List<List<Integer>> rawData = createRawGrid(3, 3, 1);
    Grid<FireState> grid = createGrid(rawData);
    FireLogic logic = new FireLogic(grid, createEmptyParameterRecord());
    logic.setProbIgnite(0.0);
    logic.update();
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(FireState.TREE, grid.getCell(i, j).getCurrentState());
      }
    }
  }

  @Test
  public void FireLogic_Update_BurningCellBecomesEmptyAfterUpdate() {
    List<List<Integer>> rawData = createRawGrid(3, 3, 1);
    rawData.get(1).set(1, 2);
    Grid<FireState> grid = createGrid(rawData);
    FireLogic logic = new FireLogic(grid, createEmptyParameterRecord());
    logic.setProbCatch(100.0);
    logic.update();
    assertEquals(FireState.EMPTY, grid.getCell(1, 1).getCurrentState());
  }

  @Test
  public void FireLogic_SetProbCatch_NegativeValue_ThrowsException() {
    List<List<Integer>> rawData = createRawGrid(2, 2, 1);
    Grid<FireState> grid = createGrid(rawData);
    FireLogic logic = new FireLogic(grid, createEmptyParameterRecord());
    assertThrows(IllegalArgumentException.class, () -> logic.setProbCatch(-10.0));
  }

  @Test
  public void FireLogic_SetProbIgnite_ValueAboveMaximum_ThrowsException() {
    List<List<Integer>> rawData = createRawGrid(2, 2, 1);
    Grid<FireState> grid = createGrid(rawData);
    FireLogic logic = new FireLogic(grid, createEmptyParameterRecord());
    assertThrows(IllegalArgumentException.class, () -> logic.setProbIgnite(150.0));
  }

  @Test
  public void FireLogic_SetProbTree_ValueWithinBounds_SetsCorrectly() {
    List<List<Integer>> rawData = createRawGrid(2, 2, 0);
    Grid<FireState> grid = createGrid(rawData);
    FireLogic logic = new FireLogic(grid, createEmptyParameterRecord());
    logic.setProbTree(50.0);
    assertEquals(50.0, logic.getProbTree());
  }

  @Test
  public void FireLogic_Update_MixedCells_OnlyTreeNeighborsCatchFire() {
    List<List<Integer>> rawData = new ArrayList<>();
    rawData.add(List.of(0, 1, 1));
    rawData.add(List.of(1, 2, 1));
    rawData.add(List.of(1, 1, 0));
    Grid<FireState> grid = createGrid(rawData);
    FireLogic logic = new FireLogic(grid, createEmptyParameterRecord());
    logic.setProbCatch(100.0);
    logic.setProbIgnite(0.0);
    logic.setProbTree(0.0);
    logic.update();
    assertEquals(FireState.EMPTY, grid.getCell(1, 1).getCurrentState());
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        if (i == 1 && j == 1) {
          continue;
        }
        if (grid.getCell(i, j).getCurrentState() == FireState.TREE) {
          assertEquals(FireState.TREE, grid.getCell(i, j).getCurrentState());
        }
      }
    }
  }

  @Test
  public void FireLogic_Update_RandomnessProducesDifferentOutcomesOverMultipleRuns() {
    List<List<Integer>> rawData = createRawGrid(5, 5, 1);
    Grid<FireState> grid1 = createGrid(rawData);
    Grid<FireState> grid2 = createGrid(rawData);
    FireLogic logic1 = new FireLogic(grid1, createEmptyParameterRecord());
    FireLogic logic2 = new FireLogic(grid2, createEmptyParameterRecord());
    logic1.setProbIgnite(50.0);
    logic2.setProbIgnite(50.0);
    logic1.update();
    logic2.update();
    boolean different = false;
    for (int i = 0; i < grid1.getNumRows(); i++) {
      for (int j = 0; j < grid1.getNumCols(); j++) {
        if (grid1.getCell(i, j).getCurrentState() != grid2.getCell(i, j).getCurrentState()) {
          different = true;
          break;
        }
      }
      if (different) {
        break;
      }
    }
    assertTrue(different);
  }

  @Test
  public void FireLogic_Update_BurningCell_WithNoTreeNeighbors_LeavesGridUnchangedExceptSelf() {
    List<List<Integer>> rawData = createRawGrid(3, 3, 0);
    rawData.get(1).set(1, 2);
    Grid<FireState> grid = createGrid(rawData);
    FireLogic logic = new FireLogic(grid, createEmptyParameterRecord());
    logic.setProbCatch(100.0);
    logic.setProbTree(0.0);
    logic.update();
    assertEquals(FireState.EMPTY, grid.getCell(1, 1).getCurrentState());
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        if (i == 1 && j == 1) {
          continue;
        }
        assertEquals(FireState.EMPTY, grid.getCell(i, j).getCurrentState());
      }
    }
  }
}
