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

  private int vision;

  public SugarNeighborCalculator() {
    super(GridShape.SQUARE, NeighborType.NEUMANN, BoundaryType.STANDARD);
  }

  public void setVision(int vision) {
    this.vision = vision;
  }

  /**
   * Returns orthogonal neighbors of a cell within a specified vision range, clamping to grid boundaries.
   * Diagonals are excluded.
   *
   * @param grid   The Grid to query
   * @param row    The cell row
   * @param col    The cell column
   * @return A map from a relative Direction to the neighbor Cell
   */
  public Map<Direction, Cell<T>> getNeighbors(Grid<T> grid, int row, int col) {
    return calculateSugarNeighbors(grid, row, col);
  }

  public Map<Direction, Cell<T>> calculateSugarNeighbors(Grid<T> grid, int row, int col) {
    Map<Direction, Cell<T>> neighbors = new HashMap<>();
    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();
    for (int dist = 1; dist <= vision; dist++) {
      int upRow = row - dist;
      int downRow = row + dist;
      int leftCol = col - dist;
      int rightCol = col + dist;

      if (upRow >= 0) {
        neighbors.put(new Direction(-dist, 0), grid.getCell(upRow, col));
      }
      if (downRow < numRows) {
        neighbors.put(new Direction(dist, 0), grid.getCell(downRow, col));
      }
      if (leftCol >= 0) {
        neighbors.put(new Direction(0, -dist), grid.getCell(row, leftCol));
      }
      if (rightCol < numCols) {
        neighbors.put(new Direction(0, dist), grid.getCell(row, rightCol));
      }
    }
    return neighbors;
  }
}