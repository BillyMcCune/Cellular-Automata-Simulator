package cellsociety.model.data.neighbors;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.State;
import java.util.List;
import java.util.Map;

/**
 * Contains the neighbor calculation/directions for Falling Sand
 *
 * @param <T> The State for the simulation
 * @author Jacob You
 */
public class FallingNeighborCalculator<T extends Enum<T> & State> extends NeighborCalculator<T> {

  public static final int[][] FALLING_DIRECTIONS = {
      {1, -1}, {1, 0}, {1, 1}
  };

  /**
   * Creates a specific NeighborCalculator with the specified directions.
   */
  public FallingNeighborCalculator() {
    super(FALLING_DIRECTIONS);
  }
}
