package cellsociety.model.logic;

import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.Grid;
import cellsociety.model.data.states.LifeState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Concrete implementation of {@link Logic} for Conway's Game of Life.
 */
public class LifeLogic extends Logic<LifeState> {
  private List<Integer> birthRequirement;
  private List<Integer> survivalRequirement;
  private String rulestring;

  /**
   * Constructs a {@code LifeLogic} instance with the specified grid.
   *
   * @param grid the grid representing the current state of grid
   */
  public LifeLogic(Grid<LifeState> grid) {
    super(grid);
    birthRequirement = List.of(3);
    survivalRequirement = List.of(2,3);
  }

  public void setRulestring(String rulestring) {
    rulestring = rulestring;
  }

  public String getRulestring() {
    return rulestring;
  }

  @Override
  protected void updateSingleCell(Cell<LifeState> cell) {
    LifeState currentState = cell.getCurrentState();
    int liveNeighbors = countLiveNeighbors(cell);

    if (currentState == LifeState.ALIVE) {
      if (!survivalRequirement.contains(liveNeighbors)) {
        cell.setNextState(LifeState.DEAD);
      }
    } else {
      if (birthRequirement.contains(liveNeighbors)) {
        cell.setNextState(LifeState.ALIVE);
      }
    }
  }

  private int countLiveNeighbors(Cell<LifeState> cell) {
    int liveCount = 0;
    for (Cell<LifeState> neighbor : cell.getNeighbors().values()) {
      if (neighbor.getCurrentState() == LifeState.ALIVE) {
        liveCount++;
      }
    }
    return liveCount;
  }
}
