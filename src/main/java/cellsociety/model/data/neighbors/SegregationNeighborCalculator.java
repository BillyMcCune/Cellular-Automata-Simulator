package cellsociety.model.data.neighbors;

import cellsociety.model.data.states.State;
import java.util.List;

/**
 * Contains the neighbor calculation/directions for Segregation
 *
 * @param <T> The State for the simulation
 * @author Jacob You
 */

public class SegregationNeighborCalculator<T extends Enum<T> & State> extends
    NeighborCalculator<T> {

  private static final int[][] directions = NeighborCalculator.MOORE;

  public SegregationNeighborCalculator() {
    super(directions);
  }
}
