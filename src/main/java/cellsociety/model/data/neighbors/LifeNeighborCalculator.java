package cellsociety.model.data.neighbors;

import cellsociety.model.data.states.State;

/**
 * Contains the neighbor calculation/directions for Game of Life
 *
 * @param <T> The State for the simulation
 * @author Jacob You
 */
public class LifeNeighborCalculator<T extends Enum<T> & State> extends NeighborCalculator<T> {

  private static final int[][] DIRECTIONS = {
      {-1, -1}, {-1, 0}, {-1, 1},
      {0, -1}, {0, 1},
      {1, -1}, {1, 0}, {1, 1}
  };

  @Override
  protected int[][] getDirections() {
    return DIRECTIONS;
  }
}
