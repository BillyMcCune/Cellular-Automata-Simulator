package cellsociety.model.data.neighbors;

import cellsociety.model.data.states.State;
import java.util.List;

/**
 * Contains the neighbor calculation/directions for Percolation
 *
 * @param <T> The State for the simulation
 * @author Jacob You
 */

public class PercolationNeighborCalculator<T extends Enum<T> & State> extends NeighborCalculator<T> {

  private static final int[][] directions = NeighborCalculator.MOORE;

  public PercolationNeighborCalculator() {
    super(directions);
  }
}
