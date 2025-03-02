package cellsociety.model.data.neighbors;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.constants.BoundaryType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.states.State;
import java.util.Map;

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
    super(GridShape.SQUARE, NeighborType.MOORE, BoundaryType.TORUS);
  }
}
