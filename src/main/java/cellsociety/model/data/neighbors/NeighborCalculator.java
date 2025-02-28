package cellsociety.model.data.neighbors;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class that calculates the neighbors for a (dx, dy) in a Grid. It uses simulation
 * specific directions to calculate these neighbors.
 *
 * @author Jacob You
 */
public abstract class NeighborCalculator<T extends Enum<T> & State> {

  private final List<Direction> directions;
  public static final int[][] MOORE = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
  public static final int[][] VONNEUMANN = {{-1, 0}, {0, -1}, {0, 1}, {1, 0}};

  /**
   * Creates a new neighbor calculator, assigning given integers to new direction objects.
   *
   * @param directions an array of length-2 arrays indicating coordinates
   */
  public NeighborCalculator(int[][] directions) {
    this.directions = intToDirections(directions);
  }

  /**
   * Returns all the directions of the current NeighborCalculator.
   *
   * @return the directions of the NeighborCalculator
   */
  public List<Direction> getDirections() {
    return directions;
  }

  /**
   * Base class to be overwritten. By default, selects standard neighbor assignment.
   *
   * @param grid   The Grid to query
   * @param row    The cell row
   * @param col    The cell column
   *
   * @return the cell neighbors
   */
  public Map<Direction, Cell<T>> getNeighbors(Grid<T> grid, int row, int col) {
    return calculateStandardNeighbors(grid, row, col);
  }

  /**
   * Returns a map from a Direction record (dx,dy) to the neighbor Cell<T>.
   *
   * @return the map of neighbors
   */
  public Map<Direction, Cell<T>> calculateStandardNeighbors(Grid<T> grid, int row, int col) {
    Map<Direction, Cell<T>> neighbors = new HashMap<>();
    for (Direction direction : getDirections()) {
      int nr = row + direction.dy();
      int nc = col + direction.dx();
      if (nr >= 0 && nr < grid.getNumRows() && nc >= 0 && nc < grid.getNumCols()) {
        neighbors.put(direction, grid.getCell(nr, nc));
      }
    }
    return neighbors;
  }

  /**
   * Returns a map from a Direction record (dx,dy) to the neighbor Cell, assuming that
   * the grid loops around.
   */
  public Map<Direction, Cell<T>> calculateTorusNeighbors(Grid<T> grid, int row, int col) {
    Map<Direction, Cell<T>> neighbors = new HashMap<>();
    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();
    for (Direction direction : getDirections()) {
      int nr = (row + direction.dx() + numRows) % numRows;
      int nc = (col + direction.dy() + numCols) % numCols;
      neighbors.put(direction, grid.getCell(nr, nc));
    }
    return neighbors;
  }

  private static List<Direction> intToDirections(int[][] directions) {
    List<Direction> directionList = new ArrayList<>();
    for (int[] dir : directions) {
      directionList.add(new Direction(dir[0], dir[1]));
    }
    return directionList;
  }
}
