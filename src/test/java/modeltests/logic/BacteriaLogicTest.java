package modeltests.logic;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.config.CellRecord;
import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.constants.EdgeType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.states.BacteriaState;
import cellsociety.model.logic.BacteriaLogic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class BacteriaLogicTest {

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
    return new Grid<>(recordGrid, factory, GridShape.SQUARE, NeighborType.MOORE, EdgeType.TORUS);
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
  public void updateSingleCell_OneNeighbor_BelowThreshold_CellChanges() {
    List<List<Integer>> data = new ArrayList<>();
    data.add(List.of(0, 1)); // (0,0) => 0, (0,1) => 1
    Grid<BacteriaState> grid = createGridFromData(data);
    ParameterRecord params = createParams(100.0, 3.0);
    BacteriaLogic logic = new BacteriaLogic(grid, params);

    logic.update();
    assertEquals(1.0, grid.getCell(0, 0).getProperty("coloredId"));
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
