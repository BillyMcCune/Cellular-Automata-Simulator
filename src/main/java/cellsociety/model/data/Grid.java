package cellsociety.model.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jacob You
 * Purpose: Represents a grid of cells for a cellular automaton.
 * The grid is a two-dimensional list where each element is a {@link Cell}.
 * Assumptions: Assumes that cells is not empty and that all rows have the same number of columns
 * Dependecies: Cell (classes or packages):
 * How to Use: Call methods to set the next stage, then update to switch the cell to the next step.
 */

public class Grid {

  /**
   * Utility class responsible for loading and creating the grid of cells.
   */
  private static class GridLoader {

    /**
     * Creates a two-dimensional grid of {@link Cell} objects from a two-dimensional list of integer
     * states.
     *
     * @param cells a two-dimensional list of integers representing the initial states of the cells
     * @return a two-dimensional list of {@link Cell} objects initialized with the given states
     */
    private static List<List<Cell>> createGrid(List<List<Integer>> cells) {
      List<List<Cell>> grid = new ArrayList<>();
      for (int row = 0; row < cells.size(); row++) {
        List<Cell> newCells = new ArrayList<>();
        for (int col = 0; col < cells.get(0).size(); col++) {
          newCells.add(new Cell(row, col, cells.get(row).get(col)));
        }
        grid.add(newCells);
      }
      return grid;
    }
  }

  private List<List<Cell>> grid = new ArrayList<>();
  private static final int[][] DIRECTIONS = {
      {-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}
  };

  /**
   * Constructs a {@code Grid} from a two-dimensional list of integer states. Each integer in the
   * list represents the initial state of a corresponding {@link Cell}.
   *
   * @param cells a two-dimensional list of integers representing the initial states of the cells
   */
  public Grid(List<List<Integer>> cells) {
    grid = GridLoader.createGrid(cells);
  }

  /**
   * Retrieves a specific cell from the grid based on its row and column indices.
   *
   * @param row the row index of the desired cell
   * @param col the column index of the desired cell
   * @return the {@link Cell} at the specified position
   */
  public Cell getCell(int row, int col) {
    return grid.get(row).get(col);
  }

  /**
   * Retrieves all neighboring {@link Cell} objects surrounding the cell at the specified row and
   * column. Neighbors are considered in all eight possible directions.
   *
   * @param row the row index of the target cell
   * @param col the column index of the target cell
   * @return a list of neighboring {@link Cell} objects
   */
  public List<Cell> getNeighbors(int row, int col) {
    List<Cell> neighbors = new ArrayList<>();
    int numRows = grid.size();
    int numCols = grid.get(0).size();

    for (int[] direction : DIRECTIONS) {
      int neighborRow = row + direction[0];
      int neighborCol = col + direction[1];

      if (neighborRow >= 0 && neighborRow < numRows &&
          neighborCol >= 0 && neighborCol < numCols) {
        neighbors.add(getCell(neighborRow, neighborCol));
      }
    }
    return neighbors;
  }

  /**
   * Updates all cells in the grid by setting each cell's current state to its next state. This
   * should be called after all cells have had their next states computed based on the automaton's
   * rules.
   */
  public void updateGrid() {
    for (List<Cell> row : grid) {
      for (Cell cell : row) {
        cell.update();
      }
    }
  }
}
