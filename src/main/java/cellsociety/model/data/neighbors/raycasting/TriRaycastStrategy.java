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
 * Implements the raycasting strategy for triangular cells.
 *
 * @param <T> the enum type representing the cell state.
 * @author Jacob You
 */
public class TriRaycastStrategy<T extends Enum<T> & State>
    implements RaycastStrategy<T> {

  private enum TriDirection {
    UP_LEFT, UP_RIGHT, LEFT, RIGHT, DOWN_LEFT, DOWN_RIGHT
  }

  private static final Map<TriDirection, Direction> UP_MAP = new HashMap<>();
  private static final Map<TriDirection, Direction> DOWN_MAP = new HashMap<>();

  private static final Map<Direction, TriDirection> UP_REVERSE = new HashMap<>();
  private static final Map<Direction, TriDirection> DOWN_REVERSE = new HashMap<>();

  /**
   * Initializes the mapping for upward and downward directions and reverse mappings for triangular cells.
   */
  static {
    UP_MAP.put(TriDirection.UP_LEFT, new Direction(0, -1));
    UP_MAP.put(TriDirection.UP_RIGHT, new Direction(0, +1));
    UP_MAP.put(TriDirection.LEFT, new Direction(0, -1));
    UP_MAP.put(TriDirection.RIGHT, new Direction(0, +1));
    UP_MAP.put(TriDirection.DOWN_LEFT, new Direction(+1, 0));
    UP_MAP.put(TriDirection.DOWN_RIGHT, new Direction(+1, 0));

    DOWN_MAP.put(TriDirection.UP_LEFT, new Direction(-1, 0));
    DOWN_MAP.put(TriDirection.UP_RIGHT, new Direction(-1, 0));
    DOWN_MAP.put(TriDirection.LEFT, new Direction(0, -1));
    DOWN_MAP.put(TriDirection.RIGHT, new Direction(0, +1));
    DOWN_MAP.put(TriDirection.DOWN_LEFT, new Direction(0, -1));
    DOWN_MAP.put(TriDirection.DOWN_RIGHT, new Direction(0, +1));

    for (var entry : UP_MAP.entrySet()) {
      if (entry.getKey() != TriDirection.UP_LEFT && entry.getKey() != TriDirection.UP_RIGHT) {
        UP_REVERSE.putIfAbsent(entry.getValue(), entry.getKey());
      }
    }
    for (var entry : DOWN_MAP.entrySet()) {
      if (entry.getKey() != TriDirection.DOWN_LEFT && entry.getKey() != TriDirection.DOWN_RIGHT) {
        DOWN_REVERSE.putIfAbsent(entry.getValue(), entry.getKey());
      }
    }
  }

  @Override
  public Map<Direction, Cell<T>> doRaycast(Grid<T> grid, int startRow, int startCol,
      Direction rawDir, int steps, EdgeType boundary) {
    Map<Direction, Cell<T>> result = new LinkedHashMap<>();
    int[] pos = new int[]{startRow, startCol};
    boolean startUp = ((startRow + startCol) % 2 == 0);
    TriDirection baseDir = startUp ? UP_REVERSE.get(rawDir) : DOWN_REVERSE.get(rawDir);
    for (int i = 0; i < steps; i++) {
      boolean isUp = ((pos[0] + pos[1]) % 2 == 0);
      Direction offset = isUp ? UP_MAP.get(baseDir) : DOWN_MAP.get(baseDir);
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
    boolean isUp = ((startRow + startCol) % 2 == 0);
    return new ArrayList<>(isUp ? UP_MAP.values() : DOWN_MAP.values());
  }
}