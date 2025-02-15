package cellsociety.model.data.neighbors;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.State;
import java.util.HashMap;
import java.util.Map;

public abstract class NeighborCalculator<T extends Enum<T> & State> {
  protected abstract int[][] getDirections();

  public Map<String, Cell<T>> getNeighbors(Grid<T> grid, int row, int col) {
    Map<String, Cell<T>> neighbors = new HashMap<>();
    for (int[] dir : getDirections()) {
      int numRow = row + dir[0];
      int numCol = col + dir[1];
      if (numRow >= 0 && numRow < grid.getNumRows()
          && numCol >= 0 && numCol < grid.getNumCols()) {
        neighbors.put(numRow + " " + numCol, grid.getCell(numRow, numCol));
      }
    }
    return neighbors;
  }
}

