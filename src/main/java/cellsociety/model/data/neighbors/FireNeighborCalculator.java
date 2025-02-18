package cellsociety.model.data.neighbors;

import cellsociety.model.data.states.State;
import java.util.List;

/**
 * Contains the neighbor calculation/directions for Spreading of Fire
 *
 * @param <T> The State for the simulation
 * @author Jacob You
 */
public class FireNeighborCalculator<T extends Enum<T> & State> extends NeighborCalculator<T> {

  private static final int[][] DIRECTIONS = {
      {-1, 0}, {0, -1}, {0, 1}, {1, 0}
  };

  @Override
  protected List<Direction> getDirections() {
    return intToDirections(DIRECTIONS);
  }
}
