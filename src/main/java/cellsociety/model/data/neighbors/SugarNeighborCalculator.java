package cellsociety.model.data.neighbors;

import cellsociety.model.data.states.State;

/**
 * Contains the neighbor calculation/directions for Segregation
 *
 * @param <T> The State for the simulation
 * @author Jacob You
 */

public class SugarNeighborCalculator<T extends Enum<T> & State> extends
    NeighborCalculator<T> {

  private static final int[][] DIRECTIONS = {
      {-1, 0}, {0, -1}, {0, 1}, {1, 0}
  };

  public SugarNeighborCalculator() {
    super(DIRECTIONS);
  }
}