package modeltests.logic;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.logic.PercolationLogic;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.states.PercolationState;

public class PercolationLogicTest {

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

  private Grid<PercolationState> createGrid(List<List<Integer>> rawGrid) {
    CellFactory<PercolationState> factory = new CellFactory<>(PercolationState.class);
    return new Grid<>(rawGrid, factory);
  }

  @Test
  public void PercolationLogic_PercolatedCellPropagatesToAllOpenNeighbors() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(1, 1, 1));
    rawGrid.add(List.of(1, 2, 1));
    rawGrid.add(List.of(1, 1, 1));
    Grid<PercolationState> grid = createGrid(rawGrid);
    PercolationLogic logic = new PercolationLogic(grid);
    logic.update();
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(PercolationState.PERCOLATED, grid.getCell(i, j).getCurrentState(),
            "Cell (" + i + "," + j + ") should be PERCOLATED after update");
      }
    }
  }

  @Test
  public void PercolationLogic_AllOpenCellsRemainOpenWhenNoPercolationPresent() {
    List<List<Integer>> rawGrid = createRawGrid(3, 3, 1);
    Grid<PercolationState> grid = createGrid(rawGrid);
    PercolationLogic logic = new PercolationLogic(grid);
    logic.update();
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(PercolationState.OPEN, grid.getCell(i, j).getCurrentState(),
            "Cell (" + i + "," + j + ") should remain OPEN when no percolation is present");
      }
    }
  }

  @Test
  public void PercolationLogic_BlockedCellsUnaffectedByAdjacentPercolation() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(1, 1, 1));
    rawGrid.add(List.of(1, 2, 0));
    rawGrid.add(List.of(1, 1, 1));
    Grid<PercolationState> grid = createGrid(rawGrid);
    PercolationLogic logic = new PercolationLogic(grid);
    logic.update();
    assertEquals(PercolationState.BLOCKED, grid.getCell(1, 2).getCurrentState(),
        "Blocked cell at (1,2) should remain BLOCKED after update");
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        if (i == 1 && j == 2) continue;
        assertEquals(PercolationState.PERCOLATED, grid.getCell(i, j).getCurrentState(),
            "Cell (" + i + "," + j + ") should be PERCOLATED after update");
      }
    }
  }

  @Test
  public void PercolationLogic_1x1OpenCellRemainsOpenAfterUpdate() {
    List<List<Integer>> rawGrid = createRawGrid(1, 1, 1);
    Grid<PercolationState> grid = createGrid(rawGrid);
    PercolationLogic logic = new PercolationLogic(grid);
    logic.update();
    assertEquals(PercolationState.OPEN, grid.getCell(0, 0).getCurrentState(),
        "1x1 open cell should remain OPEN after update");
  }

  @Test
  public void PercolationLogic_1x1PercolatedCellRemainsPercolatedAfterUpdate() {
    List<List<Integer>> rawGrid = createRawGrid(1, 1, 2);
    Grid<PercolationState> grid = createGrid(rawGrid);
    PercolationLogic logic = new PercolationLogic(grid);
    logic.update();
    assertEquals(PercolationState.PERCOLATED, grid.getCell(0, 0).getCurrentState(),
        "1x1 percolated cell should remain PERCOLATED after update");
  }

  @Test
  public void PercolationLogic_MultipleUpdates_StablePercolationPattern() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(1, 1, 1, 1));
    rawGrid.add(List.of(1, 2, 1, 1));
    rawGrid.add(List.of(1, 1, 1, 1));
    rawGrid.add(List.of(1, 1, 1, 1));
    Grid<PercolationState> grid = createGrid(rawGrid);
    PercolationLogic logic = new PercolationLogic(grid);
    logic.update();
    logic.update();
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(PercolationState.PERCOLATED, grid.getCell(i, j).getCurrentState(),
            "After multiple updates, cell (" + i + "," + j + ") should be PERCOLATED");
      }
    }
  }

  @Test
  public void PercolationLogic_DiagonalPropagationOverMultipleUpdates() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(2, 0, 1));
    rawGrid.add(List.of(0, 1, 1));
    rawGrid.add(List.of(1, 1, 1));
    Grid<PercolationState> grid = createGrid(rawGrid);
    PercolationLogic logic = new PercolationLogic(grid);
    logic.update();
    assertEquals(PercolationState.PERCOLATED, grid.getCell(1, 1).getCurrentState(), "After first update, open diagonal cell (1,1) should become PERCOLATED.");
    logic.update();
    assertEquals(PercolationState.PERCOLATED, grid.getCell(0, 2).getCurrentState(), "After second update, cell (0,2) should be PERCOLATED.");
    assertEquals(PercolationState.PERCOLATED, grid.getCell(1, 2).getCurrentState(), "After second update, cell (1,2) should be PERCOLATED.");
    assertEquals(PercolationState.PERCOLATED, grid.getCell(2, 0).getCurrentState(), "After second update, cell (2,0) should be PERCOLATED.");
    assertEquals(PercolationState.PERCOLATED, grid.getCell(2, 1).getCurrentState(), "After second update, cell (2,1) should be PERCOLATED.");
    assertEquals(PercolationState.PERCOLATED, grid.getCell(2, 2).getCurrentState(), "After second update, cell (2,2) should be PERCOLATED.");
    assertEquals(PercolationState.BLOCKED, grid.getCell(0, 1).getCurrentState(), "Blocked cell (0,1) should remain BLOCKED.");
    assertEquals(PercolationState.BLOCKED, grid.getCell(1, 0).getCurrentState(), "Blocked cell (1,0) should remain BLOCKED.");
  }

  @Test
  public void PercolationLogic_BarrierBlocksPropagationEvenAfterMultipleUpdates() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(2, 1, 1));
    rawGrid.add(List.of(0, 0, 0));
    rawGrid.add(List.of(1, 1, 1));
    Grid<PercolationState> grid = createGrid(rawGrid);
    PercolationLogic logic = new PercolationLogic(grid);
    logic.update();
    assertEquals(PercolationState.PERCOLATED, grid.getCell(0, 0).getCurrentState(), "Cell (0,0) should remain PERCOLATED.");
    assertEquals(PercolationState.PERCOLATED, grid.getCell(0, 1).getCurrentState(), "Cell (0,1) should become PERCOLATED.");
    assertEquals(PercolationState.OPEN, grid.getCell(0, 2).getCurrentState(), "Cell (0,2) should remain OPEN.");
    logic.update();
    assertEquals(PercolationState.PERCOLATED, grid.getCell(0, 2).getCurrentState(), "Cell (0,2) should become PERCOLATED.");
    for (int j = 0; j < grid.getNumCols(); j++) {
      assertEquals(PercolationState.BLOCKED, grid.getCell(1, j).getCurrentState(), "Row1 cell (" + j + ") should remain BLOCKED.");
      assertEquals(PercolationState.OPEN, grid.getCell(2, j).getCurrentState(), "Row2 cell (" + j + ") should remain OPEN.");
    }
    logic.update();
    for (int j = 0; j < grid.getNumCols(); j++) {
      assertEquals(PercolationState.OPEN, grid.getCell(2, j).getCurrentState(), "Row2 cell (" + j + ") should remain OPEN after multiple updates.");
    }
  }

  @Test
  public void PercolationLogic_InvalidRawValueInGridDefaultsToBlocked() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(5, 1, 1));
    rawGrid.add(List.of(1, 5, 1));
    rawGrid.add(List.of(1, 1, 5));
    Grid<PercolationState> grid = createGrid(rawGrid);
    assertEquals(PercolationState.BLOCKED, grid.getCell(0, 0).getCurrentState(), "Invalid raw value should default to BLOCKED.");
    assertEquals(PercolationState.BLOCKED, grid.getCell(1, 1).getCurrentState(), "Invalid raw value should default to BLOCKED.");
    assertEquals(PercolationState.BLOCKED, grid.getCell(2, 2).getCurrentState(), "Invalid raw value should default to BLOCKED.");
  }

  @Test
  public void PercolationLogic_MixedInvalidAndValidValues_OnlyPropagatesThroughOpenCells() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(2, 5, 1));
    rawGrid.add(List.of(5, 1, 5));
    rawGrid.add(List.of(1, 5, 1));
    Grid<PercolationState> grid = createGrid(rawGrid);
    PercolationLogic logic = new PercolationLogic(grid);
    logic.update();
    assertEquals(PercolationState.PERCOLATED, grid.getCell(0, 0).getCurrentState(), "Cell (0,0) should be PERCOLATED.");
    assertEquals(PercolationState.BLOCKED, grid.getCell(0, 1).getCurrentState(), "Cell (0,1) should be BLOCKED.");
    assertEquals(PercolationState.OPEN, grid.getCell(0, 2).getCurrentState(), "Cell (0,2) should be OPEN.");
    assertEquals(PercolationState.BLOCKED, grid.getCell(1, 0).getCurrentState(), "Cell (1,0) should be BLOCKED.");
    assertEquals(PercolationState.PERCOLATED, grid.getCell(1, 1).getCurrentState(), "Cell (1,1) should be PERCOLATED.");
    assertEquals(PercolationState.BLOCKED, grid.getCell(1, 2).getCurrentState(), "Cell (1,2) should be BLOCKED.");
    assertEquals(PercolationState.OPEN, grid.getCell(2, 0).getCurrentState(), "Cell (2,0) should be OPEN.");
    assertEquals(PercolationState.BLOCKED, grid.getCell(2, 1).getCurrentState(), "Cell (2,1) should be BLOCKED.");
    assertEquals(PercolationState.OPEN, grid.getCell(2, 2).getCurrentState(), "Cell (2,2) should be OPEN.");
  }
}
