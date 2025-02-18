package modeltests.logic;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.config.CellRecord;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.data.states.LifeState;
import cellsociety.model.logic.LifeLogic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author Jacob You
 */
public class LifeLogicTest {

  private final NeighborCalculator<LifeState> dummyNeighborCalculator = new NeighborCalculator<LifeState>() {
    @Override
    protected int[][] getDirections() {
      return new int[][]{
          {-1, -1}, {-1, 0}, {-1, 1},
          {0, -1}, {0, 1},
          {1, -1}, {1, 0}, {1, 1}
      };
    }
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

  private Grid<LifeState> createGridFromData(List<List<Integer>> rawData) {
    CellFactory<LifeState> factory = new CellFactory<>(LifeState.class);
    List<List<CellRecord>> records = createCellRecordGrid(rawData);
    return new Grid<>(records, factory, dummyNeighborCalculator);
  }

  private ParameterRecord createDefaultParameterRecord() {
    return new ParameterRecord(Map.of(), Map.of("rulestring", "B3/S23"));
  }

  @Test
  public void LifeLogic_BlinkerOscillator_OneStepVerticalPattern() {
    List<List<Integer>> data = new ArrayList<>();
    data.add(List.of(0, 0, 0));
    data.add(List.of(1, 1, 1));
    data.add(List.of(0, 0, 0));
    Grid<LifeState> grid = createGridFromData(data);
    LifeLogic logic = new LifeLogic(grid, createDefaultParameterRecord());
    logic.update();
    int[][] expected = {
        {0, 1, 0},
        {0, 1, 0},
        {0, 1, 0}
    };
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        LifeState expState = (expected[i][j] == 1) ? LifeState.ALIVE : LifeState.DEAD;
        assertEquals(expState, grid.getCell(i, j).getCurrentState());
      }
    }
  }

  @Test
  public void LifeLogic_BlinkerOscillator_TwoStepsRevertsToHorizontal() {
    List<List<Integer>> data = new ArrayList<>();
    data.add(List.of(0, 0, 0));
    data.add(List.of(1, 1, 1));
    data.add(List.of(0, 0, 0));
    Grid<LifeState> grid = createGridFromData(data);
    LifeLogic logic = new LifeLogic(grid, createDefaultParameterRecord());
    logic.update();
    logic.update();
    int[][] expected = {
        {0, 0, 0},
        {1, 1, 1},
        {0, 0, 0}
    };
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        LifeState expState = (expected[i][j] == 1) ? LifeState.ALIVE : LifeState.DEAD;
        assertEquals(expState, grid.getCell(i, j).getCurrentState());
      }
    }
  }

  @Test
  public void LifeLogic_BlockStillLife_RemainsUnchangedAfterUpdate() {
    List<List<Integer>> data = new ArrayList<>();
    data.add(List.of(0, 0, 0, 0));
    data.add(List.of(0, 1, 1, 0));
    data.add(List.of(0, 1, 1, 0));
    data.add(List.of(0, 0, 0, 0));
    Grid<LifeState> grid = createGridFromData(data);
    LifeLogic logic = new LifeLogic(grid, createDefaultParameterRecord());
    logic.update();
    int[][] expected = {
        {0, 0, 0, 0},
        {0, 1, 1, 0},
        {0, 1, 1, 0},
        {0, 0, 0, 0}
    };
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        LifeState expState = (expected[i][j] == 1) ? LifeState.ALIVE : LifeState.DEAD;
        assertEquals(expState, grid.getCell(i, j).getCurrentState());
      }
    }
  }

  @Test
  public void LifeLogic_AllDeadCells_RemainDeadAfterUpdate() {
    List<List<Integer>> data = createGridData(3, 3, 0);
    Grid<LifeState> grid = createGridFromData(data);
    LifeLogic logic = new LifeLogic(grid, createDefaultParameterRecord());
    logic.update();
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(LifeState.DEAD, grid.getCell(i, j).getCurrentState());
      }
    }
  }

  @Test
  public void LifeLogic_SingleAliveCell_DiesDueToUnderpopulation() {
    List<List<Integer>> data = new ArrayList<>();
    data.add(List.of(1));
    Grid<LifeState> grid = createGridFromData(data);
    LifeLogic logic = new LifeLogic(grid, createDefaultParameterRecord());
    logic.update();
    assertEquals(LifeState.DEAD, grid.getCell(0, 0).getCurrentState());
  }

  @Test
  public void LifeLogic_RulestringInvalidFormat_ThrowsException() {
    List<List<Integer>> data = createGridData(3, 3, 0);
    Grid<LifeState> grid = createGridFromData(data);
    ParameterRecord pr = createDefaultParameterRecord();
    LifeLogic logic = new LifeLogic(grid, pr);
    assertThrows(IllegalArgumentException.class, () -> logic.setRulestring("InvalidRules"));
  }

  @Test
  public void LifeLogic_RulestringBSNotation_ParsesCorrectly() {
    List<List<Integer>> data = createGridData(3, 3, 0);
    Grid<LifeState> grid = createGridFromData(data);
    ParameterRecord pr = createDefaultParameterRecord();
    LifeLogic logic = new LifeLogic(grid, pr);
    logic.setRulestring("B3/S23");
    assertEquals("B3/S23", logic.getRulestring());
  }

  @Test
  public void LifeLogic_RulestringSBNotation_ParsesCorrectly() {
    List<List<Integer>> data = createGridData(3, 3, 0);
    Grid<LifeState> grid = createGridFromData(data);
    ParameterRecord pr = createDefaultParameterRecord();
    LifeLogic logic = new LifeLogic(grid, pr);
    logic.setRulestring("23/3");
    assertEquals("23/3", logic.getRulestring());
  }

  @Test
  public void LifeLogic_RulestringEmptyBirth_ParsesCorrectly() {
    List<List<Integer>> data = createGridData(3, 3, 0);
    Grid<LifeState> grid = createGridFromData(data);
    ParameterRecord pr = createDefaultParameterRecord();
    LifeLogic logic = new LifeLogic(grid, pr);
    logic.setRulestring("B/S23");
    assertEquals("B/S23", logic.getRulestring());
  }

  @Test
  public void LifeLogic_RulestringEmptySurvival_ParsesCorrectly() {
    List<List<Integer>> data = createGridData(3, 3, 0);
    Grid<LifeState> grid = createGridFromData(data);
    ParameterRecord pr = createDefaultParameterRecord();
    LifeLogic logic = new LifeLogic(grid, pr);
    logic.setRulestring("B3/S");
    assertEquals("B3/S", logic.getRulestring());
  }

  @Test
  public void LifeLogic_MixedPattern_MultipleUpdatesProduceValidStates() {
    List<List<Integer>> raw = new ArrayList<>();
    raw.add(List.of(0, 1, 0, 1));
    raw.add(List.of(1, 0, 1, 0));
    raw.add(List.of(0, 1, 0, 1));
    raw.add(List.of(1, 0, 1, 0));
    Grid<LifeState> grid = createGridFromData(raw);
    LifeLogic logic = new LifeLogic(grid, createDefaultParameterRecord());
    for (int i = 0; i < 3; i++) {
      logic.update();
    }
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        LifeState state = grid.getCell(i, j).getCurrentState();
        assertTrue(state == LifeState.ALIVE || state == LifeState.DEAD);
      }
    }
  }

  @Test
  public void LifeLogic_RulestringNonDigit_ThrowsException() {
    List<List<Integer>> data = createGridData(3, 3, 0);
    Grid<LifeState> grid = createGridFromData(data);
    ParameterRecord pr = createDefaultParameterRecord();
    LifeLogic logic = new LifeLogic(grid, pr);
    assertThrows(IllegalArgumentException.class, () -> logic.setRulestring("B3/S2x3"));
  }

  @Test
  public void LifeLogic_RulestringHighBirthThreshold_ResultsInNoNewLife() {
    List<List<Integer>> data = createGridData(3, 3, 0);
    data.get(1).set(1, 1);
    Grid<LifeState> grid = createGridFromData(data);
    ParameterRecord pr = createDefaultParameterRecord();
    LifeLogic logic = new LifeLogic(grid, pr);

    logic.setRulestring("B9/S23");
    logic.update();

    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(LifeState.DEAD, grid.getCell(i, j).getCurrentState(),
            "All cells should be dead because B9 prevents birth.");
      }
    }
  }

  @Test
  public void LifeLogic_RulestringEmptyBirth_AllCellsDie() {
    List<List<Integer>> data = createGridData(3, 3, 1);
    Grid<LifeState> grid = createGridFromData(data);
    ParameterRecord pr = createDefaultParameterRecord();
    LifeLogic logic = new LifeLogic(grid, pr);

    logic.setRulestring("B/S");
    logic.update();

    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(LifeState.DEAD, grid.getCell(i, j).getCurrentState(),
            "All cells should die because no births are allowed.");
      }
    }
  }

  @Test
  public void LifeLogic_RulestringEmptySurvival_AllCellsDie() {
    List<List<Integer>> data = createGridData(3, 3, 1);
    Grid<LifeState> grid = createGridFromData(data);
    ParameterRecord pr = createDefaultParameterRecord();
    LifeLogic logic = new LifeLogic(grid, pr);

    logic.setRulestring("B3/S");
    logic.update();

    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(LifeState.DEAD, grid.getCell(i, j).getCurrentState(),
            "All cells should die because no cells can survive.");
      }
    }
  }

  @Test
  public void LifeLogic_RulestringTotalAnarchy_AllCellsRevive() {
    List<List<Integer>> data = createGridData(3, 3, 0);
    Grid<LifeState> grid = createGridFromData(data);
    ParameterRecord pr = createDefaultParameterRecord();
    LifeLogic logic = new LifeLogic(grid, pr);

    logic.setRulestring("B012345678/S012345678");
    logic.update();

    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(LifeState.ALIVE, grid.getCell(i, j).getCurrentState(),
            "All cells should be alive due to total anarchy rulestring.");
      }
    }
  }

  @Test
  public void LifeLogic_RulestringMinimalSurvival_OnlyOneSurvives() {
    List<List<Integer>> data = new ArrayList<>();
    data.add(List.of(1, 0, 1));
    data.add(List.of(0, 0, 0));
    data.add(List.of(1, 0, 0));
    Grid<LifeState> grid = createGridFromData(data);
    ParameterRecord pr = createDefaultParameterRecord();
    LifeLogic logic = new LifeLogic(grid, pr);

    logic.setRulestring("B3/S2");
    logic.update();

    int[][] expected = {
        {0, 0, 0},
        {0, 1, 0},
        {0, 0, 0}
    };
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        LifeState expectedState = (expected[i][j] == 1) ? LifeState.ALIVE : LifeState.DEAD;
        assertEquals(expectedState, grid.getCell(i, j).getCurrentState(),
            "Minimal survival test: cell at (" + i + "," + j + ") should be " + expectedState);
      }
    }
  }

  @Test
  public void LifeLogic_RulestringSingleNumberOnly_ParsesCorrectly() {
    List<List<Integer>> data = createGridData(3, 3, 0);
    Grid<LifeState> grid = createGridFromData(data);
    ParameterRecord pr = createDefaultParameterRecord();
    LifeLogic logic = new LifeLogic(grid, pr);

    logic.setRulestring("B3/S1");
    assertEquals("B3/S1", logic.getRulestring());
  }
}
