package cellsociety.model.data.neighbors;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class that calculates the neighbors for a (row, col) in a Grid. It uses simulation
 * specific directions to calculate these neighbors
 *
 * @author Jacob You
 */
public abstract class NeighborCalculator<T extends Enum<T> & State> {

  private static List<Direction> DIRECTIONS;

  public NeighborCalculator(int[][] directions) {
    DIRECTIONS = intToDirections(directions);
  }

  public static List<Direction> getDirections() {
    return DIRECTIONS;
  }

  /**
   * Returns a map from a Direction record (row,col) to the neighbor Cell<T>.
   */
  public Map<Direction, Cell<T>> getNeighbors(Grid<T> grid, int row, int col) {
    Map<Direction, Cell<T>> neighbors = new HashMap<>();
    for (Direction direction : getDirections()) {
      int nr = row + direction.col();
      int nc = col + direction.row();
      if (nr >= 0 && nr < grid.getNumRows() && nc >= 0 && nc < grid.getNumCols()) {
        neighbors.put(direction, grid.getCell(nr, nc));
      }
    }
    return neighbors;
  }

  public Map<Direction, Cell<T>> getTorusNeighbors(Grid<T> grid, int row, int col) {
    Map<Direction, Cell<T>> neighbors = new HashMap<>();
    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();
    for (Direction direction : getDirections()) {
      int nr = (row + direction.row() + numRows) % numRows;
      int nc = (col + direction.col() + numCols) % numCols;
      neighbors.put(direction, grid.getCell(nr, nc));
    }
    return neighbors;
  }

  protected static List<Direction> intToDirections(int[][] directions) {
    List<Direction> directionList = new ArrayList<>();
    for (int[] dir : directions) {
      directionList.add(new Direction(dir[0], dir[1]));
    }
    return directionList;
  }
}
