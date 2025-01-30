package cellsociety.model.logic;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.PercolationState;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of {@link Logic} for Princeton's Percolation Automata.
 */
public class PercolationLogic extends Logic<PercolationState> {

  /**
   * Constructs a {@code PercolationLogic} instance with the specified grid.
   *
   * @param grid the grid representing the current state of the grid
   */
  public PercolationLogic(Grid<PercolationState> grid) {
    super(grid);
  }

  /**
   * Updates the entire game state by one tick following Princeton's Percolation rules.
   */
  @Override
  public void update() {
    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();

    // Determine the next state for each cell
    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        Cell<PercolationState> cell = grid.getCell(row, col);
        updateSingleCell(cell);
      }
    }

    // Update all cells to their next state
    grid.updateGrid();
  }

  /**
   * Updates the next state of a single cell based on its current state and neighbors.
   *
   * @param cell the cell to update
   */
  private void updateSingleCell(Cell<PercolationState> cell) {
    PercolationState currentState = cell.getCurrentState();
    List<Cell<PercolationState>> openNeighbors = getOpenNeighbors(cell);

    if (currentState == PercolationState.PERCOLATED) {
      for (Cell<PercolationState> neighbor : openNeighbors) {
        neighbor.setNextState(PercolationState.PERCOLATED);
      }
    }
  }

  private List<Cell<PercolationState>> getOpenNeighbors(Cell<PercolationState> cell) {
    List<Cell<PercolationState>> neighbors = cell.getNeighbors();
    List<Cell<PercolationState>> openNeighbors = new ArrayList<>();

    for (Cell<PercolationState> neighbor : neighbors) {
      PercolationState neighborState = neighbor.getCurrentState();
      if (neighborState == PercolationState.OPEN) {
        openNeighbors.add(neighbor);
      }
    }
    return openNeighbors;
  }
}
