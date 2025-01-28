package cellsociety.model.logic;

import cellsociety.model.data.Cell;
import cellsociety.model.data.Grid;

/**
 * Concrete implementation of {@link Logic} for Conway's Game of Life.
 */
public class LifeLogic extends Logic {

  /**
   * Constructs a {@code LifeLogic} instance with the specified grid.
   *
   * @param grid the grid representing the current state of grid
   */
  public LifeLogic(Grid grid) {
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

  private void updateSingleCell(Cell cell) {
    int currentState = cell.getCurrState();
    int liveNeighbors = countLiveNeighbors(cell.getCoordinates()[0], cell.getCoordinates()[1]);

    if (currentState == 1) {
      if (liveNeighbors < 2 || liveNeighbors > 3) {
        cell.setNextState(0);
      } else {
        cell.setNextState(1);
      }
    } else {
      if (liveNeighbors == 3) {
        cell.setNextState(1);
      } else {
        cell.setNextState(0);
      }
    }
  }

  private int countLiveNeighbors(int row, int col) {
    int liveCount = 0;
    for (Cell neighbor : grid.getNeighbors(row, col)) {
      if (neighbor.getCurrState() == 1) {
        liveCount++;
      }
    }
    return liveCount;
  }
}
