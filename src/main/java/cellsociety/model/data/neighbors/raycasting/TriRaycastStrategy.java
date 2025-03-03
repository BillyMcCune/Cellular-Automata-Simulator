package cellsociety.model.data.neighbors.raycasting;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.constants.BoundaryType;
import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.neighbors.raycasting.RaycastStepHelper;
import cellsociety.model.data.neighbors.raycasting.RaycastStrategy;
import cellsociety.model.data.states.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @param <T>
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

  static {
    UP_MAP.put(TriDirection.UP_LEFT,    new Direction(0, -1));
    UP_MAP.put(TriDirection.UP_RIGHT,   new Direction(0, +1));
    UP_MAP.put(TriDirection.LEFT,       new Direction(0, -1));
    UP_MAP.put(TriDirection.RIGHT,      new Direction(0, +1));
    UP_MAP.put(TriDirection.DOWN_LEFT,  new Direction(+1, 0));
    UP_MAP.put(TriDirection.DOWN_RIGHT, new Direction(+1, 0));

    DOWN_MAP.put(TriDirection.UP_LEFT,    new Direction(-1, 0));
    DOWN_MAP.put(TriDirection.UP_RIGHT,   new Direction(-1, 0));
    DOWN_MAP.put(TriDirection.LEFT,       new Direction(0, -1));
    DOWN_MAP.put(TriDirection.RIGHT,      new Direction(0, +1));
    DOWN_MAP.put(TriDirection.DOWN_LEFT,  new Direction(0, -1));
    DOWN_MAP.put(TriDirection.DOWN_RIGHT, new Direction(0, +1));

    for (var e : UP_MAP.entrySet()) {
      UP_REVERSE.put(e.getValue(), e.getKey());
    }
    for (var e : DOWN_MAP.entrySet()) {
      DOWN_REVERSE.put(e.getValue(), e.getKey());
    }
  }

  @Override
  public List<Cell<T>> doRaycast(Grid<T> grid,
      int startRow,
      int startCol,
      Direction rawDir,
      int steps,
      BoundaryType boundary) {
    List<Cell<T>> path = new ArrayList<>();
    int[] pos = new int[] {startRow, startCol};

    boolean startUp = ((startRow + startCol) % 2 == 0);

    TriDirection baseDir = startUp
        ? UP_REVERSE.get(rawDir)
        : DOWN_REVERSE.get(rawDir);

    if (baseDir == null) {
      baseDir = startUp
          ? DOWN_REVERSE.get(rawDir)
          : UP_REVERSE.get(rawDir);
    }

    for (int i = 0; i < steps; i++) {
      boolean isUp = ((pos[0] + pos[1]) % 2 == 0);
      Direction offset = isUp ? UP_MAP.get(baseDir) : DOWN_MAP.get(baseDir);

      boolean ok = RaycastStepHelper.doSingleStep(grid, boundary, pos, offset, path);
      if (!ok) break;
    }

    return path;
  }

  @Override
  public List<Direction> getDefaultRawDirections(int startRow, int startCol) {
    boolean isUp = ((startRow + startCol) % 2 == 0);
    return new ArrayList<>(isUp ? UP_MAP.values() : DOWN_MAP.values());
  }
}
