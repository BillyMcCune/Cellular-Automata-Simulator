package cellsociety.model.data.neighbors;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.constants.BoundaryType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.states.State;
import java.util.HashMap;
import java.util.Map;

/**
 * A neighbor calculator that finds only orthogonal neighbors (up, right, down, left)
 * up to a specified vision distance. Diagonal directions are excluded.
 *
 * @param <T> the state for the simulation
 */
public class SugarNeighborCalculator<T extends Enum<T> & State> extends NeighborCalculator<T> {

  /**
   * Creates a specific NeighborCalculator with the specified directions.
   */
  public SugarNeighborCalculator() {
    super(GridShape.SQUARE, NeighborType.NEUMANN, BoundaryType.STANDARD);
  }
}