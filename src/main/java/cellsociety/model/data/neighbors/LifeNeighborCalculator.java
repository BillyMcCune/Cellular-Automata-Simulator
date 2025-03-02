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

  /**
   * Creates a specific NeighborCalculator with the specified directions.
   */
  public LifeNeighborCalculator() {
    super("square", "moore", false);
  }
}
