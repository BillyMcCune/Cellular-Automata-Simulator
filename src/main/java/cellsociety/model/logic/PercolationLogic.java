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

  public void update() {
    super.update();
  }

  /**
   * Updates the next state of a single cell based on its current state and neighbors.
   *
   * @param cell the cell to update
   */
  protected void updateSingleCell(Cell<PercolationState> cell) {
    PercolationState currentState = cell.getCurrentState();

    if (currentState == PercolationState.PERCOLATED) {
      List<Cell<PercolationState>> openNeighbors = getOpenNeighbors(cell);
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
