package cellsociety.model.data.neighbors;

import cellsociety.model.data.states.State;
import java.util.List;

/**
 * Contains the neighbor calculation/directions for Bacteria Rock Paper Scissors
 *
 * @param <T> The State for the simulation
 * @author Jacob You
 */
public class BacteriaNeighborCalculator<T extends Enum<T> & State> extends NeighborCalculator<T> {

  private static final int[][] DIRECTIONS = {
      {-1, -1}, {-1, 0}, {-1, 1},
      {0, -1}, {0, 1},
      {1, -1}, {1, 0}, {1, 1}
  };

  public BacteriaNeighborCalculator(int[][] directions) {
    super(directions);
  }
}
