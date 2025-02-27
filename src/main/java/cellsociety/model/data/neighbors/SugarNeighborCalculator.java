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

  /**
   * Creates a specific NeighborCalculator with the specified directions.
   */
  public SugarNeighborCalculator() {
    super(NeighborCalculator.VONNEUMANN);
  }
}