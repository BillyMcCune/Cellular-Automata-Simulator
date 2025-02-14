package cellsociety.model.data.neighbors;

import cellsociety.model.data.states.State;

public class LifeNeighborCalculator<T extends Enum<T> & State> extends NeighborCalculator<T> {
  private static final int[][] DIRECTIONS = {
      {-1, -1}, {-1, 0}, {-1, 1},
      {0, -1},           {0, 1},
      {1, -1},  {1, 0},  {1, 1}
  };

  @Override
  protected int[][] getDirections() {
    return DIRECTIONS;
  }
}
