package cellsociety.model.data.neighbors;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.State;
import java.util.Map;

/**
 * Contains the neighbor calculation/directions for Bacteria Rock Paper Scissors
 *
 * @param <T> The State for the simulation
 * @author Jacob You
 */
public class BacteriaNeighborCalculator<T extends Enum<T> & State> extends NeighborCalculator<T> {

  /**
   * Creates a specific NeighborCalculator with the specified directions.
   */
  public BacteriaNeighborCalculator() {
    super(NeighborCalculator.MOORE);
  }

  /**
   * Creates neighbors based off standard grid neighbor assignment.
   *
   * @param grid   The Grid to query
   * @param row    The cell row
   * @param col    The cell column
   */
  @Override
  public Map<Direction, Cell<T>> getNeighbors(Grid<T> grid, int row, int col) {
    return calculateStandardNeighbors(grid, row, col);
  }
}
