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

  /**
   * Creates a specific NeighborCalculator with the specified directions.
   */
  public BacteriaNeighborCalculator() {
    super(NeighborCalculator.MOORE);
  }
}
