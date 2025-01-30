package cellsociety.model.logic;

import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.Grid;
import cellsociety.model.data.states.LifeState;

/**
 * Concrete implementation of {@link Logic} for Conway's Game of Life.
 */
public class LifeLogic extends Logic<LifeState> {

  /**
   * Constructs a {@code LifeLogic} instance with the specified grid.
   *
   * @param grid the grid representing the current state of grid
   */
  public LifeLogic(Grid<LifeState> grid) {
    super(grid);
  }


  public void update() {
    super.update();
  }

  protected void updateSingleCell(Cell<LifeState> cell) {
    LifeState currentState = cell.getCurrentState();
    int liveNeighbors = countLiveNeighbors(cell);

    if (currentState == LifeState.ALIVE) {
      if (liveNeighbors < 2 || liveNeighbors > 3) {
        cell.setNextState(LifeState.DEAD);
      }
    } else {
      if (liveNeighbors == 3) {
        cell.setNextState(LifeState.ALIVE);
      }
    }
  }

  private int countLiveNeighbors(Cell<LifeState> cell) {
    int liveCount = 0;
    for (Cell<LifeState> neighbor : cell.getNeighbors()) {
      if (neighbor.getCurrentState() == LifeState.ALIVE) {
        liveCount++;
      }
    }
    return liveCount;
  }
}
