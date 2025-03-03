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
public class HexRaycastStrategy<T extends Enum<T> & State>
    implements RaycastStrategy<T> {

  private enum HexDirection {
    UP, DOWN, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT
  }

  private static final Map<HexDirection, Direction> EVEN_MAP = Map.of(
      HexDirection.UP, new Direction(-1, 0),
      HexDirection.DOWN, new Direction(+1, 0),
      HexDirection.UP_LEFT, new Direction(0, -1),
      HexDirection.UP_RIGHT, new Direction(0, +1),
      HexDirection.DOWN_LEFT, new Direction(+1, -1),
      HexDirection.DOWN_RIGHT, new Direction(+1, +1)
  );

  private static final Map<HexDirection, Direction> ODD_MAP = Map.of(
      HexDirection.UP, new Direction(-1, 0),
      HexDirection.DOWN, new Direction(+1, 0),
      HexDirection.UP_LEFT, new Direction(-1, -1),
      HexDirection.UP_RIGHT, new Direction(-1, +1),
      HexDirection.DOWN_LEFT, new Direction(0, -1),
      HexDirection.DOWN_RIGHT, new Direction(0, +1)
  );

  private static final Map<Direction, HexDirection> EVEN_REVERSE = new HashMap<>();
  private static final Map<Direction, HexDirection> ODD_REVERSE = new HashMap<>();

  static {
    for (var e : EVEN_MAP.entrySet()) {
      EVEN_REVERSE.put(e.getValue(), e.getKey());
    }
    for (var e : ODD_MAP.entrySet()) {
      ODD_REVERSE.put(e.getValue(), e.getKey());
    }
  }

  @Override
  public Map<Direction, Cell<T>> doRaycast(Grid<T> grid, int startRow, int startCol,
      Direction rawDir, int steps, EdgeType boundary) {
    Map<Direction, Cell<T>> result = new LinkedHashMap<>();
    int[] pos = new int[]{startRow, startCol};
    boolean isStartEven = (startCol % 2 == 0);
    HexDirection baseDir = isStartEven ? EVEN_REVERSE.get(rawDir) : ODD_REVERSE.get(rawDir);
    for (int i = 0; i < steps; i++) {
      boolean isEven = (pos[1] % 2 == 0);
      Direction offset = isEven ? EVEN_MAP.get(baseDir) : ODD_MAP.get(baseDir);
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
    boolean isEven = (startCol % 2 == 0);
    return new ArrayList<>(isEven ? EVEN_MAP.values() : ODD_MAP.values());
  }
}
