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
import cellsociety.model.data.states.PercolationState;
import cellsociety.model.logic.PercolationLogic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author Jacob You
 */
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

  private Grid<PercolationState> createGrid(List<List<Integer>> rawData) {
    CellFactory<PercolationState> factory = new CellFactory<>(PercolationState.class);
    List<List<CellRecord>> records = createCellRecordGrid(rawData);
    return new Grid<>(records, factory, GridShape.SQUARE, NeighborType.MOORE, BoundaryType.BASE);
  }

  private ParameterRecord createEmptyParameterRecord() {
    return new ParameterRecord(Map.of(), Map.of());
  }

  @Test
  public void PercolationLogic_PercolatedCellPropagatesToAllOpenNeighbors() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(1, 1, 1));
    rawGrid.add(List.of(1, 2, 1));
    rawGrid.add(List.of(1, 1, 1));
    Grid<PercolationState> grid = createGrid(rawGrid);
    PercolationLogic logic = new PercolationLogic(grid, createEmptyParameterRecord());
    logic.update();
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(PercolationState.PERCOLATED, grid.getCell(i, j).getCurrentState());
      }
    }
  }

  @Test
  public void PercolationLogic_AllOpenCellsRemainOpenWhenNoPercolationPresent() {
    List<List<Integer>> rawGrid = createRawGrid(3, 3, 1);
    Grid<PercolationState> grid = createGrid(rawGrid);
    PercolationLogic logic = new PercolationLogic(grid, createEmptyParameterRecord());
    logic.update();
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(PercolationState.OPEN, grid.getCell(i, j).getCurrentState());
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
    PercolationLogic logic = new PercolationLogic(grid, createEmptyParameterRecord());
    logic.update();
    assertEquals(PercolationState.BLOCKED, grid.getCell(1, 2).getCurrentState());
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        if (i == 1 && j == 2) {
          continue;
        }
        assertEquals(PercolationState.PERCOLATED, grid.getCell(i, j).getCurrentState());
      }
    }
  }

  @Test
  public void PercolationLogic_1x1OpenCellRemainsOpenAfterUpdate() {
    List<List<Integer>> rawGrid = createRawGrid(1, 1, 1);
    Grid<PercolationState> grid = createGrid(rawGrid);
    PercolationLogic logic = new PercolationLogic(grid, createEmptyParameterRecord());
    logic.update();
    assertEquals(PercolationState.OPEN, grid.getCell(0, 0).getCurrentState());
  }

  @Test
  public void PercolationLogic_1x1PercolatedCellRemainsPercolatedAfterUpdate() {
    List<List<Integer>> rawGrid = createRawGrid(1, 1, 2);
    Grid<PercolationState> grid = createGrid(rawGrid);
    PercolationLogic logic = new PercolationLogic(grid, createEmptyParameterRecord());
    logic.update();
    assertEquals(PercolationState.PERCOLATED, grid.getCell(0, 0).getCurrentState());
  }

  @Test
  public void PercolationLogic_MultipleUpdates_StablePercolationPattern() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(1, 1, 1, 1));
    rawGrid.add(List.of(1, 2, 1, 1));
    rawGrid.add(List.of(1, 1, 1, 1));
    rawGrid.add(List.of(1, 1, 1, 1));
    Grid<PercolationState> grid = createGrid(rawGrid);
    PercolationLogic logic = new PercolationLogic(grid, createEmptyParameterRecord());
    logic.update();
    logic.update();
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(PercolationState.PERCOLATED, grid.getCell(i, j).getCurrentState());
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
    PercolationLogic logic = new PercolationLogic(grid, createEmptyParameterRecord());
    logic.update();
    assertEquals(PercolationState.PERCOLATED, grid.getCell(1, 1).getCurrentState());
    logic.update();
    assertEquals(PercolationState.PERCOLATED, grid.getCell(0, 2).getCurrentState());
    assertEquals(PercolationState.PERCOLATED, grid.getCell(1, 2).getCurrentState());
    assertEquals(PercolationState.PERCOLATED, grid.getCell(2, 0).getCurrentState());
    assertEquals(PercolationState.PERCOLATED, grid.getCell(2, 1).getCurrentState());
    assertEquals(PercolationState.PERCOLATED, grid.getCell(2, 2).getCurrentState());
    assertEquals(PercolationState.BLOCKED, grid.getCell(0, 1).getCurrentState());
    assertEquals(PercolationState.BLOCKED, grid.getCell(1, 0).getCurrentState());
  }

  @Test
  public void PercolationLogic_BarrierBlocksPropagationEvenAfterMultipleUpdates() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(2, 1, 1));
    rawGrid.add(List.of(0, 0, 0));
    rawGrid.add(List.of(1, 1, 1));
    Grid<PercolationState> grid = createGrid(rawGrid);
    PercolationLogic logic = new PercolationLogic(grid, createEmptyParameterRecord());
    logic.update();
    assertEquals(PercolationState.PERCOLATED, grid.getCell(0, 0).getCurrentState());
    assertEquals(PercolationState.PERCOLATED, grid.getCell(0, 1).getCurrentState());
    assertEquals(PercolationState.OPEN, grid.getCell(0, 2).getCurrentState());
    logic.update();
    assertEquals(PercolationState.PERCOLATED, grid.getCell(0, 2).getCurrentState());
    for (int j = 0; j < grid.getNumCols(); j++) {
      assertEquals(PercolationState.BLOCKED, grid.getCell(1, j).getCurrentState());
      assertEquals(PercolationState.OPEN, grid.getCell(2, j).getCurrentState());
    }
    logic.update();
    for (int j = 0; j < grid.getNumCols(); j++) {
      assertEquals(PercolationState.OPEN, grid.getCell(2, j).getCurrentState());
    }
  }

  @Test
  public void PercolationLogic_InvalidRawValueInGrid_DefaultsToBlocked() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(5, 1, 1));
    rawGrid.add(List.of(1, 5, 1));
    rawGrid.add(List.of(1, 1, 5));
    Grid<PercolationState> grid = createGrid(rawGrid);
    assertEquals(PercolationState.BLOCKED, grid.getCell(0, 0).getCurrentState());
    assertEquals(PercolationState.BLOCKED, grid.getCell(1, 1).getCurrentState());
    assertEquals(PercolationState.BLOCKED, grid.getCell(2, 2).getCurrentState());
  }

  @Test
  public void PercolationLogic_MixedInvalidAndValidValues_OnlyPropagatesThroughOpenCells() {
    List<List<Integer>> rawGrid = new ArrayList<>();
    rawGrid.add(List.of(2, 5, 1));
    rawGrid.add(List.of(5, 1, 5));
    rawGrid.add(List.of(1, 5, 1));
    Grid<PercolationState> grid = createGrid(rawGrid);
    PercolationLogic logic = new PercolationLogic(grid, createEmptyParameterRecord());
    logic.update();
    assertEquals(PercolationState.PERCOLATED, grid.getCell(0, 0).getCurrentState());
    assertEquals(PercolationState.BLOCKED, grid.getCell(0, 1).getCurrentState());
    assertEquals(PercolationState.OPEN, grid.getCell(0, 2).getCurrentState());
    assertEquals(PercolationState.BLOCKED, grid.getCell(1, 0).getCurrentState());
    assertEquals(PercolationState.PERCOLATED, grid.getCell(1, 1).getCurrentState());
    assertEquals(PercolationState.BLOCKED, grid.getCell(1, 2).getCurrentState());
    assertEquals(PercolationState.OPEN, grid.getCell(2, 0).getCurrentState());
    assertEquals(PercolationState.BLOCKED, grid.getCell(2, 1).getCurrentState());
    assertEquals(PercolationState.OPEN, grid.getCell(2, 2).getCurrentState());
  }
}
