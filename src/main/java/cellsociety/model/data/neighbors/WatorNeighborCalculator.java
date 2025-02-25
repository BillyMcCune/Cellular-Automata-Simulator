package cellsociety.model.data.neighbors;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.State;
import java.util.Map;

/**
 * Contains the neighbor calculation/directions for Wator Uses unique logic to specify a torus
 * looping world
 *
 * @param <T> The State for the simulation
 * @author Jacob You
 */

public class WatorNeighborCalculator<T extends Enum<T> & State> extends NeighborCalculator<T> {

  private static final int[][] directions = NeighborCalculator.VONNEUMANN;

  public WatorNeighborCalculator() {
    super(directions);
  }

  @Override
  public Map<Direction, Cell<T>> getNeighbors(Grid<T> grid, int row, int col) {
    return getTorusNeighbors(grid, row, col);
  }
}
