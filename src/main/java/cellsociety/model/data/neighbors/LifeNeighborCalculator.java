package cellsociety.model.data.neighbors;

import cellsociety.model.data.states.State;
import java.util.List;

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

  public LifeNeighborCalculator(int[][] directions) {
    super(directions);
  }
}
