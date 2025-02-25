package cellsociety.model.data.neighbors;

import cellsociety.model.data.states.State;
import java.util.List;

/**
 * Contains the neighbor calculation/directions for Falling Sand
 *
 * @param <T> The State for the simulation
 * @author Jacob You
 */
public class FallingNeighborCalculator<T extends Enum<T> & State> extends NeighborCalculator<T> {

  private static final int[][] directions = {
      {1, -1}, {1, 0}, {1, 1}
  };

  public FallingNeighborCalculator() {
    super(directions);
  }
}
