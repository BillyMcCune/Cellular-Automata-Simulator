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

  /**
   * Updates the entire game state by one tick following Conway's Game of Life rules.
   */
  @Override
  public void update() {
    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();

    // Determine next state for each cell
    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        updateSingleCell(grid.getCell(row, col));
      }
    }
    grid.updateGrid();
  }

  private void updateSingleCell(Cell<LifeState> cell) {
    LifeState currentState = cell.getCurrentState();
    int liveNeighbors = countLiveNeighbors(cell);

    if (currentState == LifeState.ALIVE) {
      if (liveNeighbors < 2 || liveNeighbors > 3) {
        cell.setNextState(LifeState.DEAD);
      }
    }
    else {
      if (liveNeighbors == 3) {
        cell.setNextState(LifeState.ALIVE);
      }
    }
  }

  private int countLiveNeighbors(Cell<LifeState> cell) {
    int liveCount = 0;
    for (Cell<LifeState> neighbor : grid.getNeighbors(cell)) {
      if (neighbor.getCurrentState() == LifeState.ALIVE) {
        liveCount++;
      }
    }
    return liveCount;
  }
}
