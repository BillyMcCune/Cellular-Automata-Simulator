package modeltests.logic;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.config.CellRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.data.states.FallingState;
import cellsociety.model.logic.FallingLogic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author Jacob You
 */
public class FallingLogicTest {

  private static final int[][] DIRECTIONS = {
      {1, -1}, {1, 0}, {1, 1}
  };

  private final NeighborCalculator<FallingState> dummyNeighborCalculator = new NeighborCalculator<FallingState>(
      DIRECTIONS) {
  };

  private List<List<Integer>> createGridData(int rows, int cols, int defaultValue) {
    List<List<Integer>> data = new ArrayList<>();
    for (int i = 0; i < rows; i++) {
      List<Integer> row = new ArrayList<>();
      for (int j = 0; j < cols; j++) {
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
        props.put("dummy", 1.0);
        recordRow.add(new CellRecord(state, props));
      }
      records.add(recordRow);
    }
    return records;
  }

  private Grid<FallingState> createGridFromData(List<List<Integer>> rawData) {
    CellFactory<FallingState> factory = new CellFactory<>(FallingState.class);
    List<List<CellRecord>> records = createCellRecordGrid(rawData);
    return new Grid<>(records, factory, dummyNeighborCalculator);
  }

  private ParameterRecord createDefaultParameterRecord() {
    return new ParameterRecord(Map.of(), Map.of());
  }

  @Test
  public void FallingLogic_SandFalls_OneStep() {
    List<List<Integer>> data = createGridData(3, 3, 0);
    data.get(0).set(1, 2);
    Grid<FallingState> grid = createGridFromData(data);
    FallingLogic logic = new FallingLogic(grid, createDefaultParameterRecord());
    logic.update();
    assertEquals(FallingState.EMPTY, grid.getCell(0, 1).getCurrentState());
    assertEquals(FallingState.SAND, grid.getCell(1, 1).getCurrentState());
  }

  @Test
  public void FallingLogic_WaterFalls_OneStepDown() {
    List<List<Integer>> data = createGridData(3, 3, 0);
    data.get(0).set(1, 3);
    data.get(1).set(0, 1);
    data.get(1).set(2, 1);
    Grid<FallingState> grid = createGridFromData(data);
    FallingLogic logic = new FallingLogic(grid, createDefaultParameterRecord());
    logic.update();
    assertEquals(FallingState.EMPTY, grid.getCell(0, 1).getCurrentState());
    assertEquals(FallingState.WATER, grid.getCell(1, 1).getCurrentState());
  }

  @Test
  public void FallingLogic_WaterFalls_OneStepDownRight() {
    List<List<Integer>> data = createGridData(3, 3, 0);
    data.get(0).set(1, 3);
    data.get(1).set(1, 1);
    data.get(1).set(2, 1);
    Grid<FallingState> grid = createGridFromData(data);
    FallingLogic logic = new FallingLogic(grid, createDefaultParameterRecord());
    logic.update();
    assertEquals(FallingState.EMPTY, grid.getCell(0, 1).getCurrentState());
    assertEquals(FallingState.WATER, grid.getCell(1, 0).getCurrentState());
  }

  @Test
  public void FallingLogic_WaterFalls_OneStepDownLeft() {
    List<List<Integer>> data = createGridData(3, 3, 0);
    data.get(0).set(1, 3);
    data.get(1).set(1, 1);
    data.get(1).set(0, 1);
    Grid<FallingState> grid = createGridFromData(data);
    FallingLogic logic = new FallingLogic(grid, createDefaultParameterRecord());
    logic.update();
    assertEquals(FallingState.EMPTY, grid.getCell(0, 1).getCurrentState());
    assertEquals(FallingState.WATER, grid.getCell(1, 2).getCurrentState());
  }

  @Test
  public void FallingLogic_SandDoesNotMove_AtBottom() {
    List<List<Integer>> data = createGridData(3, 3, 0);
    data.get(2).set(1, 2);
    Grid<FallingState> grid = createGridFromData(data);
    FallingLogic logic = new FallingLogic(grid, createDefaultParameterRecord());
    logic.update();
    assertEquals(FallingState.SAND, grid.getCell(2, 1).getCurrentState());
  }

  @Test
  public void FallingLogic_EmptyGrid_DoesNotCrash() {
    List<List<Integer>> data = createGridData(3, 3, 0);
    Grid<FallingState> grid = createGridFromData(data);
    FallingLogic logic = new FallingLogic(grid, createDefaultParameterRecord());
    assertDoesNotThrow(logic::update);
  }

  @Test
  public void FallingLogic_WaterTrapped_DoesNotMove() {
    List<List<Integer>> data = createGridData(3, 3, 1);
    data.get(1).set(1, 3);
    Grid<FallingState> grid = createGridFromData(data);
    FallingLogic logic = new FallingLogic(grid, createDefaultParameterRecord());
    logic.update();
    assertEquals(FallingState.WATER, grid.getCell(1, 1).getCurrentState());
  }

  @Test
  public void FallingLogic_ExtremeCase_AllSandAboveAllWater() {
    List<List<Integer>> data = createGridData(5, 5, 0);
    for (int j = 0; j < 5; j++) {
      data.get(0).set(j, 2);
      data.get(1).set(j, 2);
    }
    for (int j = 0; j < 5; j++) {
      data.get(3).set(j, 3);
      data.get(4).set(j, 3);
    }
    Grid<FallingState> grid = createGridFromData(data);
    FallingLogic logic = new FallingLogic(grid, createDefaultParameterRecord());
    for (int i = 0; i < 30; i++) {
      logic.update(); // It is very improbable something wouldn't happen
    }
    boolean swapOccurred = false;
    for (int row = 0; row < grid.getNumRows(); row++) {
      for (int col = 0; col < grid.getNumCols(); col++) {
        if (grid.getCell(row, col).getCurrentState() == FallingState.SAND && row > 1) {
          swapOccurred = true;
          break;
        }
      }
    }
    assertTrue(swapOccurred);
  }
}
