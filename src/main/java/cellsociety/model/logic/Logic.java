package cellsociety.model.logic;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.PercolationState;
import cellsociety.model.data.states.State;

/**
 * Abstract superclass responsible for managing the logic of a cellular automaton. Subclasses should
 * implement specific rules.
 *
 * @param <T> The enum type representing the cell state
 */
public abstract class Logic<T extends Enum<T> & State> {

  protected final Grid<T> grid;

  public Logic(Grid<T> grid) {
    this.grid = grid;
  }

  /**
   * Updates the entire game state by one tick. Subclasses should provide an implementation for this
   * method.
   */
  public void update() {
    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();

    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        Cell<T> cell = grid.getCell(row, col);
        updateSingleCell(cell);
      }
    }
    grid.updateGrid();
  }

  protected abstract void updateSingleCell(Cell<T> cell);
}
