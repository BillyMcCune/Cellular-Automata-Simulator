package modeltests.logic;

import static org.junit.jupiter.api.Assertions.*;
import cellsociety.model.config.ParameterRecord;
import cellsociety.model.config.CellRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.data.states.SegregationState;
import cellsociety.model.data.states.WatorState;
import cellsociety.model.logic.WatorLogic;
import cellsociety.model.data.neighbors.Direction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author Jacob You
 */
public class WatorLogicTest {


  private final NeighborCalculator<WatorState> dummyNeighborCalculator =
      new NeighborCalculator<WatorState>("square", "moore", true) {
      };

  private List<List<Integer>> createGridData(int rows, int cols, int defaultState) {
    List<List<Integer>> data = new ArrayList<>();
    for (int i = 0; i < rows; i++) {
      List<Integer> row = new ArrayList<>();
      for (int j = 0; j < cols; j++) {
        row.add(defaultState);
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
        props.put("time", 0.0);
        props.put("energy", 0.0);
        recordRow.add(new CellRecord(state, props));
      }
      records.add(recordRow);
    }
    return records;
  }

  private Grid<WatorState> createGridFromData(List<List<Integer>> rawData) {
    CellFactory<WatorState> factory = new CellFactory<>(WatorState.class);
    List<List<CellRecord>> records = createCellRecordGrid(rawData);
    return new Grid<>(records, factory, dummyNeighborCalculator);
  }

  private ParameterRecord createDefaultParameterRecord() {
    Map<String, Double> doubles = new HashMap<>();
    doubles.put("fishEnergyGain", 2.0);
    doubles.put("fishReproductionTime", 2.0);
    doubles.put("sharkReproductionTime", 8.0);
    doubles.put("sharkBaseEnergy", 5.0);
    return new ParameterRecord(doubles, Map.of());
  }

  @Test
  public void WatorLogic_SharkNextToFish_SharkEatsFish() {
    List<List<Integer>> data = createGridData(3, 3, WatorState.OPEN.getValue());
    data.get(1).set(1, WatorState.SHARK.getValue());
    data.get(0).set(1, WatorState.FISH.getValue());
    Grid<WatorState> grid = createGridFromData(data);
    Cell<WatorState> sharkCell = grid.getCell(1, 1);
    sharkCell.setProperty("energy", 5.0);
    sharkCell.setProperty("time", 0.0);
    ParameterRecord pr = createDefaultParameterRecord();
    WatorLogic logic = new WatorLogic(grid, pr);
    logic.update();
    Cell<WatorState> fishCell = grid.getCell(0, 1);
    assertEquals(WatorState.SHARK, fishCell.getCurrentState());
    double newEnergy = (double) fishCell.getProperty("energy");
    assertEquals(5.0 + pr.myDoubleParameters().get("fishEnergyGain"), newEnergy, 0.0001);
  }

  @Test
  public void WatorLogic_SharkEnergyDepletes_SharkDies() {
    List<List<Integer>> data = createGridData(3, 3, WatorState.OPEN.getValue());
    data.get(1).set(1, WatorState.SHARK.getValue());
    Grid<WatorState> grid = createGridFromData(data);
    Cell<WatorState> sharkCell = grid.getCell(1, 1);
    sharkCell.setProperty("energy", 1.0);
    sharkCell.setProperty("time", 0.0);
    ParameterRecord pr = createDefaultParameterRecord();
    WatorLogic logic = new WatorLogic(grid, pr);
    logic.update();
    assertEquals(WatorState.OPEN, grid.getCell(1, 1).getCurrentState());
  }

  @Test
  public void WatorLogic_FishWithOpenNeighbor_Moves() {
    List<List<Integer>> data = createGridData(3, 3, WatorState.OPEN.getValue());
    data.get(1).set(1, WatorState.FISH.getValue());
    // Surround fish with OPEN cells in orthogonal directions only.
    Grid<WatorState> grid = createGridFromData(data);
    Cell<WatorState> fishCell = grid.getCell(1, 1);
    fishCell.setProperty("time", 0.0);
    ParameterRecord pr = createDefaultParameterRecord();
    WatorLogic logic = new WatorLogic(grid, pr);
    logic.update();
    assertNotEquals(WatorState.FISH, fishCell.getCurrentState());
  }

  @Test
  public void WatorLogic_NoOpenNeighbor_NoMovement() {
    List<List<Integer>> data = createGridData(3, 3, WatorState.SHARK.getValue());
    // Entire grid is full of sharks, so no open neighbor exists.
    Grid<WatorState> grid = createGridFromData(data);
    ParameterRecord pr = createDefaultParameterRecord();
    WatorLogic logic = new WatorLogic(grid, pr);
    Cell<WatorState> sharkCell = grid.getCell(1, 1);
    double initialEnergy = (double) sharkCell.getProperty("energy");
    sharkCell.setProperty("time", 0.0);
    logic.update();
    assertEquals(WatorState.SHARK, sharkCell.getCurrentState());
    assertEquals(initialEnergy - 1, (double) sharkCell.getProperty("energy"), 0.0001);
  }
}
