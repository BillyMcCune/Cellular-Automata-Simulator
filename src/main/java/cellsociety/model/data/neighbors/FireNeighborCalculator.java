package cellsociety.model.data.neighbors;

import cellsociety.model.data.constants.BoundaryType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.states.State;

/**
 * Contains the neighbor calculation/directions for Spreading of Fire
 *
 * @param <T> The State for the simulation
 * @author Jacob You
 */
public class FireNeighborCalculator<T extends Enum<T> & State> extends NeighborCalculator<T> {

  /**
   * Creates a specific NeighborCalculator with the specified directions.
   */
  public FireNeighborCalculator() {
    super(GridShape.SQUARE, NeighborType.NEUMANN, BoundaryType.STANDARD);
  }
}
