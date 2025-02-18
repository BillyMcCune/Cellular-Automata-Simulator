package cellsociety.model.data.neighbors;

import cellsociety.model.data.states.State;

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
  protected int[][] getDirections() {
    return DIRECTIONS;
  }
}
