package cellsociety.model.data.neighbors.raycasting;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.constants.BoundaryType;
import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.states.State;
import java.util.List;
import java.util.Map;

public final class RaycastStepHelper {

  private RaycastStepHelper() {
    // no instances
  }

  /**
   * Moves the position [posRow, posCol] by (offset.dy, offset.dx). If boundary=TORUS, wraps around.
   * If STANDARD and out-of-bounds, returns false (meaning "stop"). Otherwise, return true.
   *
   * @param grid     the grid
   * @param boundary boundary type
   * @param pos      int[0] = current row, int[1] = current col (mutable array)
   * @param offset   the offset to apply
   * @return true if the move succeeded, false if we went out of bounds (STANDARD)
   */
  public static <T extends Enum<T> & State> boolean doSingleStep(Grid<T> grid,
      BoundaryType boundary, int[] pos, int startRow, int startCol, Direction offset,
      Map<Direction, Cell<T>> result) {
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
    int dy = pos[0] - startRow;
    int dx = pos[1] - startCol;
    result.put(new Direction(dy, dx), grid.getCell(pos[0], pos[1]));
    return true;
  }
}