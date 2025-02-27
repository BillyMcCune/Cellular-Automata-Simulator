package modeltests.logic;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.config.CellRecord;
import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.data.states.BacteriaState;
import cellsociety.model.logic.BacteriaLogic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class BacteriaLogicTest {

  private static final int[][] EIGHT_DIRECTIONS = {
      {-1, -1}, {-1, 0}, {-1, 1},
      {0, -1}, {0, 1},
      {1, -1}, {1, 0}, {1, 1}
  };

  private final NeighborCalculator<BacteriaState> dummyNeighborCalculator = new NeighborCalculator<BacteriaState>(
      EIGHT_DIRECTIONS) {
    @Override
    public Map<Direction, Cell<BacteriaState>> calculateStandardNeighbors(Grid<BacteriaState> grid, int row,
        int col) {
      Map<Direction, Cell<BacteriaState>> neighbors = new HashMap<>();
      int numRows = grid.getNumRows();
      int numCols = grid.getNumCols();
      for (int[] d : EIGHT_DIRECTIONS) {
        int newRow = (row + d[0] + numRows) % numRows;
        int newCol = (col + d[1] + numCols) % numCols;
        if (newRow >= 0 && newRow < numRows && newCol >= 0 && newCol < numCols) {
          neighbors.put(new Direction(d[0], d[1]), grid.getCell(newRow, newCol));
        }
      }
      return neighbors;
    }
  };

  private List<List<Integer>> createGridData(int rows, int cols, int defaultValue) {
    List<List<Integer>> data = new ArrayList<>();
    for (int r = 0; r < rows; r++) {
      List<Integer> row = new ArrayList<>();
      for (int c = 0; c < cols; c++) {
        row.add(defaultValue);
      }
      data.add(row);
    }
    return data;
  }

  private List<List<CellRecord>> createCellRecordGrid(List<List<Integer>> rawData) {
    List<List<CellRecord>> records = new ArrayList<>();
    for (List<Integer> row : rawData) {
      List<CellRecord> recordRow = new ArrayList<>();
      for (Integer state : row) {
        Map<String, Double> props = new HashMap<>();
        props.put("coloredId", state.doubleValue());
        recordRow.add(new CellRecord(0, props));
      }
      records.add(recordRow);
    }
    return records;
  }

  private Grid<BacteriaState> createGridFromData(List<List<Integer>> rawData) {
    CellFactory<BacteriaState> factory = new CellFactory<>(BacteriaState.class);
    List<List<CellRecord>> recordGrid = createCellRecordGrid(rawData);
    return new Grid<>(recordGrid, factory, dummyNeighborCalculator);
  }

  private ParameterRecord createParams(double beatingThreshold, double numStates) {
    Map<String, Double> doubles = new HashMap<>();
    doubles.put("beatingThreshold", beatingThreshold);
    doubles.put("numStates", numStates);
    return new ParameterRecord(doubles, Map.of());
  }

  @Test
  public void updateSingleCell_NoNeighbors_CellRemainsSame() {
    List<List<Integer>> data = createGridData(1, 1, 0);
    Grid<BacteriaState> grid = createGridFromData(data);
    ParameterRecord params = createParams(50.0, 3.0);
    BacteriaLogic logic = new BacteriaLogic(grid, params);

    logic.update();
    assertEquals(0.0, grid.getCell(0, 0).getProperty("coloredId"));
  }

  @Test
  public void updateSingleCell_OneNeighbor_BelowThreshold_NoChange() {
    List<List<Integer>> data = new ArrayList<>();
    data.add(List.of(0, 1)); // (0,0) => 0, (0,1) => 1
    Grid<BacteriaState> grid = createGridFromData(data);
    ParameterRecord params = createParams(100.0, 3.0);
    BacteriaLogic logic = new BacteriaLogic(grid, params);

    logic.update();
    assertEquals(0.0, grid.getCell(0, 0).getProperty("coloredId"));
    assertEquals(1.0, grid.getCell(0, 1).getProperty("coloredId"));
  }

  @Test
  public void updateSingleCell_ManyNeighbors_AboveThreshold_CellChanges() {
    List<List<Integer>> data = new ArrayList<>();
    data.add(List.of(0, 1, 1));
    data.add(List.of(1, 1, 1));
    data.add(List.of(1, 1, 1));
    Grid<BacteriaState> grid = createGridFromData(data);
    ParameterRecord params = createParams(50.0, 2.0);
    BacteriaLogic logic = new BacteriaLogic(grid, params);

    logic.update();
    assertEquals(1.0, grid.getCell(0, 0).getProperty("coloredId"));
  }

  @Test
  public void updateSingleCell_ThresholdZero_CellAlwaysChangesWhenOutnumbered() {
    List<List<Integer>> data = new ArrayList<>();
    data.add(List.of(1, 1, 1));
    data.add(List.of(1, 0, 1));
    data.add(List.of(1, 1, 1));
    Grid<BacteriaState> grid = createGridFromData(data);
    ParameterRecord params = createParams(0.0, 2.0);
    BacteriaLogic logic = new BacteriaLogic(grid, params);

    logic.update();
    assertEquals(1.0, grid.getCell(1, 1).getProperty("coloredId"));
  }

  @Test
  public void BacteriaLogic_Setters_Getters_WorkProperly() {
    List<List<Integer>> data = createGridData(2, 2, 0);
    Grid<BacteriaState> grid = createGridFromData(data);
    ParameterRecord params = createParams(30.0, 3.0);
    BacteriaLogic logic = new BacteriaLogic(grid, params);

    assertEquals(30.0, logic.getBeatingThreshold(), 0.0001);
    assertEquals(3.0, logic.getNumStates(), 0.0001);

    logic.setBeatingThreshold(90.0);
    assertEquals(90.0, logic.getBeatingThreshold(), 0.0001);

    logic.setNumStates(5.0);
    assertEquals(5.0, logic.getNumStates(), 0.0001);
  }

  @Test
  public void BacteriaLogic_Setters_NegativeThreshold_Throws() {
    List<List<Integer>> data = createGridData(2, 2, 0);
    Grid<BacteriaState> grid = createGridFromData(data);
    ParameterRecord params = createParams(30.0, 3.0);
    BacteriaLogic logic = new BacteriaLogic(grid, params);
    assertThrows(IllegalArgumentException.class, () -> logic.setBeatingThreshold(-10.0));
  }

  @Test
  public void update_MultipleTimes_BeatsRepeatedly() {
    // 3x3, center=0, everything else=1
    List<List<Integer>> data = new ArrayList<>();
    data.add(List.of(1, 1, 1));
    data.add(List.of(1, 0, 1));
    data.add(List.of(1, 1, 1));
    Grid<BacteriaState> grid = createGridFromData(data);
    ParameterRecord params = createParams(10.0, 2.0);
    BacteriaLogic logic = new BacteriaLogic(grid, params);

    logic.update();
    assertEquals(1.0, grid.getCell(1, 1).getProperty("coloredId"));

    logic.update();
    assertDoesNotThrow(logic::update);
  }

  @Test
  public void update_LargeNumStates_NoErrors() {
    List<List<Integer>> data = createGridData(2, 2, 6);
    Grid<BacteriaState> grid = createGridFromData(data);
    ParameterRecord params = createParams(10.0, 7.0);
    BacteriaLogic logic = new BacteriaLogic(grid, params);
    assertDoesNotThrow(logic::update);
  }

  @Test
  public void update_CellAlreadyMatchesBeatingId_RemainsUnchanged() {
    List<List<Integer>> data = new ArrayList<>();
    data.add(List.of(2, 2, 2));
    data.add(List.of(2, 2, 2));
    data.add(List.of(2, 2, 2));
    Grid<BacteriaState> grid = createGridFromData(data);
    ParameterRecord params = createParams(50.0, 3.0);
    BacteriaLogic logic = new BacteriaLogic(grid, params);

    logic.update();
    for (int r = 0; r < 3; r++) {
      for (int c = 0; c < 3; c++) {
        assertEquals(2.0, grid.getCell(r, c).getProperty("coloredId"));
      }
    }
  }

  @Test
  public void update_TiesInNeighbors_ExactlyHalfNeighbors() {
    List<List<Integer>> data = new ArrayList<>();
    data.add(List.of(0, 1, 0));
    data.add(List.of(1, 0, 1));
    data.add(List.of(0, 1, 0));
    Grid<BacteriaState> grid = createGridFromData(data);
    ParameterRecord params = createParams(50.0, 2.0);
    BacteriaLogic logic = new BacteriaLogic(grid, params);

    logic.update();
    assertEquals(1.0, grid.getCell(1, 1).getProperty("coloredId"));
  }

  @Test
  public void update_MultipleSteps_GradualSpread() {
    List<List<Integer>> data = new ArrayList<>();
    data.add(List.of(0,1,0));
    data.add(List.of(1,1,1));
    data.add(List.of(0,1,0));
    Grid<BacteriaState> grid = createGridFromData(data);
    ParameterRecord params = createParams(40.0, 2.0);
    BacteriaLogic logic = new BacteriaLogic(grid, params);

    logic.update();
    logic.update();
    assertDoesNotThrow(logic::update);
  }
}
