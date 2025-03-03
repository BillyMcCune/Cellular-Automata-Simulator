package cellsociety.model.data.neighbors.raycasting;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.constants.EdgeType;
import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.states.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @param <T>
 * @author Jacob You
 */
public class SquareRaycastStrategy<T extends Enum<T> & State>
    implements RaycastStrategy<T> {

  private enum SquareDirection {UP, DOWN, LEFT, RIGHT}

  private static final Map<SquareDirection, Direction> SQUARE_OFFSETS = Map.of(
      SquareDirection.UP, new Direction(-1, 0),
      SquareDirection.DOWN, new Direction(+1, 0),
      SquareDirection.LEFT, new Direction(0, -1),
      SquareDirection.RIGHT, new Direction(0, +1)
  );

  private static final Map<Direction, SquareDirection> REVERSE_MAP = new HashMap<>();

  static {
    REVERSE_MAP.put(new Direction(-1, 0), SquareDirection.UP);
    REVERSE_MAP.put(new Direction(+1, 0), SquareDirection.DOWN);
    REVERSE_MAP.put(new Direction(0, -1), SquareDirection.LEFT);
    REVERSE_MAP.put(new Direction(0, +1), SquareDirection.RIGHT);
  }

  @Override
  public Map<Direction, Cell<T>> doRaycast(Grid<T> grid, int startRow, int startCol,
      Direction rawDir, int steps, EdgeType boundary) {
    SquareDirection dir = REVERSE_MAP.get(rawDir);
    if (dir == null) {
      throw new IllegalArgumentException("Invalid raw direction for square: " + rawDir);
    }
    Map<Direction, Cell<T>> result = new LinkedHashMap<>();
    int[] pos = new int[]{startRow, startCol};
    for (int i = 0; i < steps; i++) {
      Direction offset = SQUARE_OFFSETS.get(dir);
      boolean ok = RaycastStepHelper.doSingleStep(grid, boundary, pos, startRow, startCol, offset,
          result);
      if (!ok) {
        break;
      }
    }
    return result;
  }

  @Override
  public List<Direction> getDefaultRawDirections(int startRow, int startCol) {
    // Simply return a copy of the offset values.
    return new ArrayList<>(SQUARE_OFFSETS.values());
  }
}
