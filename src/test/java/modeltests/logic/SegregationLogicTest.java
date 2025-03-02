package modeltests.logic;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.config.CellRecord;
import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.data.states.PercolationState;
import cellsociety.model.data.states.SegregationState;
import cellsociety.model.logic.SegregationLogic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author Jacob You
 */
public class SegregationLogicTest {

  private static final int[][] DIRECTIONS = {
      {-1, -1}, {-1, 0}, {-1, 1},
      {0, -1}, {0, 1},
      {1, -1}, {1, 0}, {1, 1}
  };

  private final NeighborCalculator<SegregationState> dummyNeighborCalculator =
      new NeighborCalculator<SegregationState>(GridShape.SQUARE, NeighborType.MOORE, false) {
      };

  private List<List<Integer>> createRawGrid(int rows, int cols, int defaultValue) {
    List<List<Integer>> raw = new ArrayList<>();
    for (int i = 0; i < rows; i++) {
      List<Integer> row = new ArrayList<>();
      for (int j = 0; j < cols; j++) {
        row.add(defaultValue);
      }
      raw.add(row);
    }
    return raw;
  }

  private List<List<CellRecord>> createCellRecordGrid(List<List<Integer>> rawData) {
    List<List<CellRecord>> records = new ArrayList<>();
    for (List<Integer> row : rawData) {
      List<CellRecord> recordRow = new ArrayList<>();
      for (Integer state : row) {
        Map<String, Double> props = new HashMap<>();
        props.put("id", state.doubleValue());
        recordRow.add(new CellRecord(state, props));
      }
      records.add(recordRow);
    }
    return records;
  }

  private Grid<SegregationState> createGrid(List<List<Integer>> rawData) {
    CellFactory<SegregationState> factory = new CellFactory<>(SegregationState.class);
    List<List<CellRecord>> records = createCellRecordGrid(rawData);
    return new Grid<>(records, factory, dummyNeighborCalculator);
  }

  private ParameterRecord createEmptyParameterRecord() {
    return new ParameterRecord(Map.of(), Map.of());
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
    Grid<SegregationState> grid = createGrid(raw);
    SegregationLogic logic = new SegregationLogic(grid, createEmptyParameterRecord());
    logic.setSatisfiedThreshold(80.0);
    logic.update();
    assertEquals(1, countState(grid, SegregationState.RED));
  }

  @Test
  public void SegregationLogic_OneBlueNoNeighbors_StaysPut() {
    List<List<Integer>> raw = createRawGrid(1, 1, 2);
    Grid<SegregationState> grid = createGrid(raw);
    SegregationLogic logic = new SegregationLogic(grid, createEmptyParameterRecord());
    logic.setSatisfiedThreshold(80.0);
    logic.update();
    assertEquals(1, countState(grid, SegregationState.BLUE));
  }

  @Test
  public void SegregationLogic_UnsatisfiedRedCell_MovesToOpen() {
    List<List<Integer>> raw = new ArrayList<>();
    raw.add(List.of(1, 0));
    Grid<SegregationState> grid = createGrid(raw);
    SegregationLogic logic = new SegregationLogic(grid, createEmptyParameterRecord());
    logic.setSatisfiedThreshold(100.0);
    logic.update();
    assertEquals(1, countState(grid, SegregationState.RED));
    assertEquals(SegregationState.OPEN, grid.getCell(0, 0).getCurrentState());
  }

  @Test
  public void SegregationLogic_UnsatisfiedBlueCell_MovesToOpen() {
    List<List<Integer>> raw = new ArrayList<>();
    raw.add(List.of(2, 0));
    Grid<SegregationState> grid = createGrid(raw);
    SegregationLogic logic = new SegregationLogic(grid, createEmptyParameterRecord());
    logic.setSatisfiedThreshold(100.0);
    logic.update();
    assertEquals(1, countState(grid, SegregationState.BLUE));
    assertEquals(SegregationState.OPEN, grid.getCell(0, 0).getCurrentState());
  }

  @Test
  public void SegregationLogic_NoOpenCells_NoMovementOccurs() {
    List<List<Integer>> raw = new ArrayList<>();
    raw.add(List.of(1, 2));
    raw.add(List.of(1, 2));
    Grid<SegregationState> grid = createGrid(raw);
    SegregationLogic logic = new SegregationLogic(grid, createEmptyParameterRecord());
    logic.update();
    assertEquals(2, countState(grid, SegregationState.RED));
    assertEquals(2, countState(grid, SegregationState.BLUE));
  }

  @Test
  public void SegregationLogic_ThresholdZero_NoMovementOccurs() {
    List<List<Integer>> raw = new ArrayList<>();
    raw.add(List.of(1, 0));
    raw.add(List.of(0, 2));
    Grid<SegregationState> grid = createGrid(raw);
    SegregationLogic logic = new SegregationLogic(grid, createEmptyParameterRecord());
    logic.setSatisfiedThreshold(0.0);
    logic.update();
    assertEquals(1, countState(grid, SegregationState.RED));
    assertEquals(1, countState(grid, SegregationState.BLUE));
    assertEquals(2, countState(grid, SegregationState.OPEN));
  }

  @Test
  public void SegregationLogic_ThresholdOutOfBounds_ThrowsException() {
    List<List<Integer>> raw = createRawGrid(2, 2, 1);
    Grid<SegregationState> grid = createGrid(raw);
    SegregationLogic logic = new SegregationLogic(grid, createEmptyParameterRecord());
    assertThrows(IllegalArgumentException.class, () -> logic.setSatisfiedThreshold(-10.0));
    assertThrows(IllegalArgumentException.class, () -> logic.setSatisfiedThreshold(150.0));
  }

  @Test
  public void SegregationLogic_MixedPattern_MultipleUpdatesAdjustStates() {
    List<List<Integer>> raw = new ArrayList<>();
    raw.add(List.of(1, 2, 0));
    raw.add(List.of(2, 1, 0));
    raw.add(List.of(0, 0, 0));
    Grid<SegregationState> grid = createGrid(raw);
    SegregationLogic logic = new SegregationLogic(grid, createEmptyParameterRecord());
    logic.setSatisfiedThreshold(100.0);
    logic.update();
    int redCount = countState(grid, SegregationState.RED);
    int blueCount = countState(grid, SegregationState.BLUE);
    int openCount = countState(grid, SegregationState.OPEN);
    assertTrue(redCount + blueCount + openCount == grid.getNumRows() * grid.getNumCols());
    assertTrue(openCount > 0);
  }

  @Test
  public void SegregationLogic_CellWithNoNeighbors_RemainsSatisfied() {
    List<List<Integer>> raw = createRawGrid(1, 1, 1);
    Grid<SegregationState> grid = createGrid(raw);
    SegregationLogic logic = new SegregationLogic(grid, createEmptyParameterRecord());
    logic.setSatisfiedThreshold(100.0);
    logic.update();
    assertEquals(1, countState(grid, SegregationState.RED));
  }
}
