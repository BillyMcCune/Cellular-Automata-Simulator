package cellsociety.model.data.neighbors;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.State;
import java.util.Map;

/**
 * Contains the neighbor calculation/directions for ForagingAnts
 *
 * @param <T> The State for the simulation
 * @author Jacob You
 */
public class AntNeighborCalculator<T extends Enum<T> & State> extends NeighborCalculator<T> {

  /**
   * Creates a specific NeighborCalculator with the specified directions
   */
  public AntNeighborCalculator() {
    super("square", "moore", true);
  }
}
