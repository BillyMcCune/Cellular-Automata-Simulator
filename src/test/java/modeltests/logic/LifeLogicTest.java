package modeltests.logic;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.logic.LifeLogic;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.states.LifeState;

public class LifeLogicTest {

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

  private Grid<LifeState> createGridFromData(List<List<Integer>> data) {
    CellFactory<LifeState> factory = new CellFactory<>(LifeState.class);
    return new Grid<>(data, factory);
  }

  @Test
  public void LifeLogic_BlinkerOscillator_OneStepVerticalPattern() {
    List<List<Integer>> data = new ArrayList<>();
    data.add(List.of(0, 0, 0));
    data.add(List.of(1, 1, 1));
    data.add(List.of(0, 0, 0));
    Grid<LifeState> grid = createGridFromData(data);
    LifeLogic logic = new LifeLogic(grid);
    logic.update();
    int[][] expected = {
        {0, 1, 0},
        {0, 1, 0},
        {0, 1, 0}
    };
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        LifeState expState = (expected[i][j] == 1) ? LifeState.ALIVE : LifeState.DEAD;
        assertEquals(expState, grid.getCell(i, j).getCurrentState(),
            "After one update, cell at (" + i + "," + j + ") should be " + expState);
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
    LifeLogic logic = new LifeLogic(grid);
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
        assertEquals(expState, grid.getCell(i, j).getCurrentState(),
            "After two updates, cell at (" + i + "," + j + ") should be " + expState);
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
    LifeLogic logic = new LifeLogic(grid);
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
        assertEquals(expState, grid.getCell(i, j).getCurrentState(),
            "Block pattern: cell at (" + i + "," + j + ") should remain " + expState);
      }
    }
  }

  @Test
  public void LifeLogic_AllDeadCells_RemainDeadAfterUpdate() {
    List<List<Integer>> data = createGridData(3, 3, 0);
    Grid<LifeState> grid = createGridFromData(data);
    LifeLogic logic = new LifeLogic(grid);
    logic.update();
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(LifeState.DEAD, grid.getCell(i, j).getCurrentState(),
            "All dead grid: cell at (" + i + "," + j + ") should remain DEAD");
      }
    }
  }

  @Test
  public void LifeLogic_SingleAliveCell_BecomesDeadDueToUnderpopulation() {
    List<List<Integer>> data = new ArrayList<>();
    data.add(List.of(1));
    Grid<LifeState> grid = createGridFromData(data);
    LifeLogic logic = new LifeLogic(grid);
    logic.update();
    assertEquals(LifeState.DEAD, grid.getCell(0, 0).getCurrentState(),
        "A single alive cell should die due to underpopulation");
  }

  @Test
  public void LifeLogic_1x1Dead_RemainsDead() {
    List<List<Integer>> raw = createGridData(1, 1, 0);
    Grid<LifeState> grid = createGridFromData(raw);
    LifeLogic logic = new LifeLogic(grid);
    logic.update();
    assertEquals(LifeState.DEAD, grid.getCell(0, 0).getCurrentState(), "1x1 dead cell should remain dead");
  }

  @Test
  public void LifeLogic_1x3AllAlive_CellsBecomeEdgeCellsDieAndCenterSurvives() {
    List<List<Integer>> raw = new ArrayList<>();
    List<Integer> row = new ArrayList<>();
    row.add(1); row.add(1); row.add(1);
    raw.add(row);
    Grid<LifeState> grid = createGridFromData(raw);
    LifeLogic logic = new LifeLogic(grid);
    logic.update();
    assertEquals(LifeState.DEAD, grid.getCell(0, 0).getCurrentState(), "Left cell in 1x3 all-alive grid should die");
    assertEquals(LifeState.ALIVE, grid.getCell(0, 1).getCurrentState(), "Center cell in 1x3 all-alive grid should survive");
    assertEquals(LifeState.DEAD, grid.getCell(0, 2).getCurrentState(), "Right cell in 1x3 all-alive grid should die");
  }

  @Test
  public void LifeLogic_2x2AllAlive_RemainsAlive() {
    List<List<Integer>> raw = createGridData(2, 2, 1);
    Grid<LifeState> grid = createGridFromData(raw);
    LifeLogic logic = new LifeLogic(grid);
    logic.update();
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        assertEquals(LifeState.ALIVE, grid.getCell(i, j).getCurrentState(), "In 2x2 all-alive grid, cell (" + i + "," + j + ") should remain alive");
      }
    }
  }

  @Test
  public void LifeLogic_MixedPattern_MultipleUpdatesProduceValidStates() {
    List<List<Integer>> raw = new ArrayList<>();
    raw.add(List.of(0, 1, 0, 1));
    raw.add(List.of(1, 0, 1, 0));
    raw.add(List.of(0, 1, 0, 1));
    raw.add(List.of(1, 0, 1, 0));
    Grid<LifeState> grid = createGridFromData(raw);
    LifeLogic logic = new LifeLogic(grid);
    for (int i = 0; i < 3; i++) {
      logic.update();
    }
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        LifeState state = grid.getCell(i, j).getCurrentState();
        assertTrue(state == LifeState.ALIVE || state == LifeState.DEAD, "Cell (" + i + "," + j + ") should have a valid state after updates");
      }
    }
  }
}
