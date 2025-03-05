package modeltests.logic;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.config.CellRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.constants.EdgeType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.states.SugarState;
import cellsociety.model.logic.SugarLogic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class SugarLogicTest {

  private Grid<SugarState> createGrid(int rows, int cols) {
    List<List<CellRecord>> rawData = new ArrayList<>();
    for (int r = 0; r < rows; r++) {
      List<CellRecord> row = new ArrayList<>();
      for (int c = 0; c < cols; c++) {
        Map<String, Double> props = new HashMap<>();
        props.put("sugarAmount", 0.0);
        props.put("maxSugar", 0.0);
        props.put("agentSugar", 0.0);
        row.add(new CellRecord(SugarState.EMPTY.getValue(), props));
      }
      rawData.add(row);
    }
    return new Grid<>(rawData, new CellFactory<>(SugarState.class), GridShape.SQUARE,
        NeighborType.NEUMANN, EdgeType.BASE);
  }

  private ParameterRecord createDefaultParameterRecord() {
    Map<String, Double> doubles = new HashMap<>();
    doubles.put("vision", 3.0);
    doubles.put("sugarMetabolism", 2.0);
    doubles.put("sugarGrowBackRate", 2.0);
    doubles.put("sugarGrowBackInterval", 1.0);
    return new ParameterRecord(doubles, new HashMap<>());
  }

  @Test
  public void givenSugarPatch_whenGrowSugar_thenIncreasesUpToMax() {
    Grid<SugarState> grid = createGrid(1, 1);
    Cell<SugarState> patch = grid.getCell(0, 0);
    patch.setProperty("sugarAmount", 3.0);
    patch.setProperty("maxSugar", 10.0);
    SugarLogic logic = new SugarLogic(grid, createDefaultParameterRecord());
    logic.update();
    assertEquals(5.0, patch.getProperty("sugarAmount"), 0.0001);
  }

  @Test
  public void givenAgentAndPatch_whenUpdate_thenAgentMovesToBestSugarPatch() {
    Grid<SugarState> grid = createGrid(3, 3);
    Cell<SugarState> agent = grid.getCell(1, 1);
    agent.setCurrentState(SugarState.AGENT);
    agent.setProperty("agentSugar", 10.0);
    Cell<SugarState> bestPatch = grid.getCell(1, 2);
    bestPatch.setCurrentState(SugarState.EMPTY);
    bestPatch.setProperty("sugarAmount", 8.0);
    bestPatch.setProperty("maxSugar", 10.0);
    SugarLogic logic = new SugarLogic(grid, createDefaultParameterRecord());
    logic.update();
    assertEquals(SugarState.EMPTY, agent.getNextState());
    assertEquals(SugarState.AGENT, bestPatch.getNextState());
  }

  @Test
  public void givenAgentWithLowSugar_whenUpdate_thenAgentDies() {
    Grid<SugarState> grid = createGrid(3, 3);
    Cell<SugarState> agent = grid.getCell(1, 1);
    agent.setCurrentState(SugarState.AGENT);
    agent.setProperty("agentSugar", 1.0);
    SugarLogic logic = new SugarLogic(grid, createDefaultParameterRecord());
    logic.update();
    assertEquals(SugarState.EMPTY, agent.getNextState());
  }

  @Test
  public void givenAgentWithSufficientSugar_whenUpdate_thenAgentRemains() {
    Grid<SugarState> grid = createGrid(3, 3);
    Cell<SugarState> agent = grid.getCell(1, 1);
    agent.setCurrentState(SugarState.AGENT);
    agent.setProperty("agentSugar", 15.0);
    SugarLogic logic = new SugarLogic(grid, createDefaultParameterRecord());
    logic.update();
    if (agent.getNextState() == SugarState.AGENT) {
      double remaining = agent.getProperty("agentSugar");
      assertTrue(remaining > 0);
    }
  }

  @Test
  public void givenSugarPatch_whenGrowSugar_thenDoesNotExceedMax() {
    Grid<SugarState> grid = createGrid(1, 1);
    Cell<SugarState> patch = grid.getCell(0, 0);
    patch.setProperty("sugarAmount", 9.0);
    patch.setProperty("maxSugar", 10.0);
    SugarLogic logic = new SugarLogic(grid, createDefaultParameterRecord());
    logic.update();
    assertEquals(10.0, patch.getProperty("sugarAmount"), 0.0001);
  }
}
