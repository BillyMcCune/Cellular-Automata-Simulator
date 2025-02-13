package cellsociety.model.data.neighbors;

import cellsociety.model.data.states.State;

public class SegregationNeighbors<T extends Enum<T> & State> extends Neighbors<T> {

  private static final int[][] DIRECTIONS = {
      {-1, -1}, {-1, 0}, {-1, 1},
      {0, -1},         {0, 1},
      {1, -1}, {1, 0}, {1, 1}
  };

  @Override
  protected int[][] getDirections() {
    return DIRECTIONS;
  }
}
