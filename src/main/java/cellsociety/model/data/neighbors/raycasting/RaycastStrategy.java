package cellsociety.model.data.neighbors.raycasting;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.constants.EdgeType;
import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.states.State;
import java.util.List;
import java.util.Map;

/**
 * Generic interface for "raycasting" in a particular shape (square, hex, triangle).
 *
 * @author Jacob You
 */
public interface RaycastStrategy<T extends Enum<T> & State> {

  /**
   * Performs a raycast starting at (startRow, startCol) using the supplied raw direction, for up to
   * 'steps'. BoundaryType controls whether we wrap around.
   */
  Map<Direction, Cell<T>> doRaycast(Grid<T> grid, int startRow, int startCol,
      Direction rawDir, int steps, EdgeType boundary);

  /**
   * Returns the default raw directions for this strategy given the starting cell. For hex and
   * triangle strategies, the set of default raw directions may depend on the cell’s orientation
   * (row parity for hex; up/down for triangles).
   */
  List<Direction> getDefaultRawDirections(int startRow, int startCol);
}

