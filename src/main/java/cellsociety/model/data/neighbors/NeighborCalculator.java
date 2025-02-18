package cellsociety.model.data.neighbors;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.State;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class that calculates the neighbors for a (row, col) in a Grid. It uses simulation
 * specific directions to calculate these neighbors
 *
 * @author Jacob You
 */
public abstract class NeighborCalculator<T extends Enum<T> & State> {

  protected abstract int[][] getDirections();

  /**
   * Returns a map from a Coord record (row,col) to the neighbor Cell<T>.
   */
  public Map<Coord, Cell<T>> getNeighbors(Grid<T> grid, int row, int col) {
    Map<Coord, Cell<T>> neighbors = new HashMap<>();
    for (int[] dir : getDirections()) {
      int nr = row + dir[0];
      int nc = col + dir[1];
      if (nr >= 0 && nr < grid.getNumRows()
          && nc >= 0 && nc < grid.getNumCols()) {
        Coord coord = new Coord(nr, nc);
        neighbors.put(coord, grid.getCell(nr, nc));
      }
    }
    return neighbors;
  }
}
