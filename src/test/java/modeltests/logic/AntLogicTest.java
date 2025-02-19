package modeltests.logic;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.config.CellRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.data.states.AntState;
import cellsociety.model.logic.AntLogic;
import cellsociety.model.data.neighbors.Direction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author Jacob You
 */
public class AntLogicTest {

  private static final int[][] DIRECTIONS = {
      {-1, 0}, {1, 0}, {0, -1}, {0, 1}
  };

  private final NeighborCalculator<AntState> dummyNeighborCalculator = new NeighborCalculator<AntState>(DIRECTIONS) {};

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
        props.put("searchingEntities", 0.0);
        props.put("returningEntities", 0.0);
        // For nest cells, the "antCount" property will be used to initialize searchingEntities.
        props.put("antCount", 1.0);
        props.put("homePheromone", 0.0);
        props.put("foodPheromone", 0.0);
        recordRow.add(new CellRecord(state, props));
      }
      records.add(recordRow);
    }
    return records;
  }

  private Grid<AntState> createGridFromData(List<List<Integer>> rawData) {
    CellFactory<AntState> factory = new CellFactory<>(AntState.class);
    List<List<CellRecord>> records = createCellRecordGrid(rawData);
    return new Grid<>(records, factory, dummyNeighborCalculator);
  }

  private ParameterRecord createDefaultParameterRecord() {
    Map<String, Double> doubleParams = new HashMap<>();
    doubleParams.put("maxAnts", 10.0);
    doubleParams.put("evaporationRate", 1.0);
    doubleParams.put("maxHomePheromone", 100.0);
    doubleParams.put("maxFoodPheromone", 100.0);
    doubleParams.put("basePheromoneWeight", 0.001);
    doubleParams.put("pheromoneSensitivity", 10.0);
    doubleParams.put("pheromoneDiffusionDecay", 2.0);
    return new ParameterRecord(doubleParams, Map.of("rulestring", "B3/S23"));
  }

  @Test
  public void testInitialization_AntMapFromNest() {
    List<List<Integer>> data = createGridData(3, 3, AntState.EMPTY.getValue());
    data.get(1).set(1, AntState.NEST.getValue());
    Grid<AntState> grid = createGridFromData(data);
    grid.getCell(1, 1).setProperty("antCount", 5.0);
    grid.getCell(1, 1).setProperty("searchingEntities", 5.0);
    grid.getCell(1, 1).setProperty("returningEntities", 0.0);
    AntLogic logic = new AntLogic(grid, createDefaultParameterRecord());
    double sc = grid.getCell(1, 1).getProperty("searchingEntities");
    double rc = grid.getCell(1, 1).getProperty("returningEntities");
    assertEquals(5.0, sc, 0.0001);
    assertEquals(0.0, rc, 0.0001);
  }

  @Test
  public void testAntSwitch_SearchingToReturning() {
    List<List<Integer>> data = createGridData(3, 3, AntState.EMPTY.getValue());
    data.get(1).set(1, AntState.FOOD.getValue());
    Grid<AntState> grid = createGridFromData(data);
    grid.getCell(1, 1).setProperty("searchingEntities", 3.0);
    grid.getCell(1, 1).setProperty("returningEntities", 0.0);
    AntLogic logic = new AntLogic(grid, createDefaultParameterRecord());
    logic.updateSingleCell(grid.getCell(1, 1));
    double sc = grid.getCell(1, 1).getProperty("searchingEntities");
    double rc = grid.getCell(1, 1).getProperty("returningEntities");
    assertTrue(sc < 3.0);
    assertTrue(rc > 0.0);
  }

  @Test
  public void testAntMove_UpdatesCellProperties() {
    List<List<Integer>> data = createGridData(2, 2, AntState.EMPTY.getValue());
    data.get(0).set(0, AntState.NEST.getValue());
    data.get(0).set(1, AntState.EMPTY.getValue());
    Grid<AntState> grid = createGridFromData(data);
    grid.getCell(0, 0).setProperty("antCount", 1.0);
    grid.getCell(0, 0).setProperty("searchingEntities", 1.0);
    grid.getCell(0, 0).setProperty("returningEntities", 0.0);
    AntLogic logic = new AntLogic(grid, createDefaultParameterRecord());
    logic.update();
    double srcCount = grid.getCell(0, 0).getProperty("searchingEntities")
        + grid.getCell(0, 0).getProperty("returningEntities");
    double tgtCount = grid.getCell(0, 1).getProperty("searchingEntities")
        + grid.getCell(0, 1).getProperty("returningEntities");
    assertEquals(0.0, srcCount, 0.0001);
    assertTrue(tgtCount > 0);
  }

  @Test
  public void testEvaporation_ReducesPheromones() {
    List<List<Integer>> data = createGridData(1, 1, AntState.EMPTY.getValue());
    Grid<AntState> grid = createGridFromData(data);
    AntLogic logic = new AntLogic(grid, createDefaultParameterRecord());
    Cell<AntState> cell = grid.getCell(0, 0);
    cell.setProperty("homePheromone", 100.0);
    cell.setProperty("foodPheromone", 100.0);
    logic.update();
    double expected = 100.0 * (1 - logic.getEvaporationRate()/100.0);
    assertTrue(cell.getProperty("homePheromone") <= expected);
    assertTrue(cell.getProperty("foodPheromone") <= expected);
  }
}
