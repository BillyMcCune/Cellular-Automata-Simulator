package cellsociety.model.data.neighbors;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.State;
import java.util.HashMap;
import java.util.Map;

public class WatorNeighborCalculator<T extends Enum<T> & State> extends NeighborCalculator<T> {

  private static final int[][] DIRECTIONS = {
      {-1, 0}, {1, 0}, {0, -1}, {0, 1}
  };

  @Override
  protected int[][] getDirections() {
    return DIRECTIONS;
  }

  @Override
  public Map<String, Cell<T>> getNeighbors(Grid<T> grid, int row, int col) {
    Map<String, Cell<T>> neighbors = new HashMap<>();
    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();

    for (int[] dir : getDirections()) {
      int numRow = (row + dir[0] + numRows) % numRows;
      int numCol = (col + dir[1] + numCols) % numCols;
      neighbors.put(numRow + " " + numCol, grid.getCell(numRow, numCol));
    }
    return neighbors;
  }
}
