package cellsociety.model.data.neighbors.raycasting;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.constants.BoundaryType;
import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.states.State;
import java.util.List;

public final class RaycastStepHelper {

  private RaycastStepHelper() {
    // no instances
  }

  /**
   * Moves the position [posRow, posCol] by (offset.dy, offset.dx).
   * If boundary=TORUS, wraps around. If STANDARD and out-of-bounds,
   * returns false (meaning "stop"). Otherwise, updates posRow/posCol
   * and adds the new cell to 'path'.
   *
   * @param grid    the grid
   * @param boundary boundary type
   * @param pos     int[0] = current row, int[1] = current col (mutable array)
   * @param offset  the offset to apply
   * @param path    the list of visited cells to append to
   * @return true if the move succeeded, false if we went out of bounds (STANDARD)
   */
  public static <T extends Enum<T> & State> boolean doSingleStep(Grid<T> grid,
      BoundaryType boundary,
      int[] pos,
      Direction offset,
      List<Cell<T>> path) {
    int nextRow = pos[0] + offset.dy();
    int nextCol = pos[1] + offset.dx();

    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();

    if (boundary == BoundaryType.TORUS) {
      nextRow = (nextRow + numRows) % numRows;
      nextCol = (nextCol + numCols) % numCols;
    } else {
      if (nextRow < 0 || nextRow >= numRows || nextCol < 0 || nextCol >= numCols) {
        return false;
      }
    }
    pos[0] = nextRow;
    pos[1] = nextCol;
    path.add(grid.getCell(nextRow, nextCol));
    return true;
  }
}
