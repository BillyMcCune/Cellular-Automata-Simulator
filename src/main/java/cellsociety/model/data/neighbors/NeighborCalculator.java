package cellsociety.model.data.neighbors;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.constants.EdgeType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.neighbors.raycasting.RaycastImplementor;
import cellsociety.model.data.states.State;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * A unified NeighborCalculator that supports both BFS-based neighbor expansion and raycasting. Its
 * behavior is configured via a ConfigInfo record.
 */
public class NeighborCalculator<T extends Enum<T> & State> {

  private static final String PROPERTY_FILE = "cellsociety/property/CellNeighbor.properties";
  private static final Map<String, List<Direction>> NEIGHBOR_MAP = loadNeighborProperties();

  private GridShape shape;
  private NeighborType neighborType;
  private EdgeType edgeType;
  private int steps;
  private RaycastImplementor<T> raycastImplementor;

  public NeighborCalculator(GridShape shape, NeighborType neighborType, EdgeType edgeType) {
    this(shape, neighborType, edgeType, 1);
  }

  public NeighborCalculator(GridShape shape, NeighborType neighborType, EdgeType edgeType,
      int steps) {
    this.shape = shape;
    this.neighborType = neighborType;
    this.edgeType = edgeType;
    this.steps = steps;
    this.raycastImplementor = new RaycastImplementor<>(this.shape, this.edgeType);
  }


  /**
   * Retrieves the list of available directions for a cell at the specified row and column,
   * adjusting the direction offsets for hexagonal and triangular grids by flipping the dy value as
   * needed.
   *
   * @param row the row index of the cell
   * @param col the column index of the cell
   * @return a list of directions adjusted based on the cell's position and grid shape
   */
  public List<Direction> getDirections(int row, int col) {
    String baseKey = shape + "_" + neighborType;
    List<Direction> baseOffsets = NEIGHBOR_MAP.get(baseKey);
    boolean flip = false;
    if (shape == GridShape.HEX) {
      flip = (col % 2 != 0);
    } else if (shape == GridShape.TRI) {
      flip = ((row + col) % 2 != 0);
    }
    List<Direction> adjustedDirections = new ArrayList<>();
    for (Direction offset : baseOffsets) {
      int dy = offset.dy();
      int dx = offset.dx();
      if (flip) {
        dy = -dy;
      }
      adjustedDirections.add(new Direction(dy, dx));
    }
    return adjustedDirections;
  }

  public void setSteps(int steps) {
    this.steps = steps;
  }

  public void setShape(GridShape shape) {
    this.shape = shape;
  }

  public void setNeighborType(NeighborType neighborType) {
    this.neighborType = neighborType;
  }

  public void setEdgeType(EdgeType edgeType) {
    this.edgeType = edgeType;
  }

  public int getSteps() {
    return steps;
  }

  public GridShape getShape() {
    return shape;
  }

  public EdgeType getEdgeType() {
    return edgeType;
  }

  public NeighborType getNeighborType() {
    return neighborType;
  }

  public Map<Direction, Cell<T>> getNeighbors(Grid<T> grid, int startRow, int startCol) {
    List<Map<Direction, Cell<T>>> expansionsByDist = bfsExpansions(grid, startRow, startCol, steps);
    Map<Direction, Cell<T>> allNeighbors = new HashMap<>();
    for (int dist = 1; dist <= steps && dist < expansionsByDist.size(); dist++) {
      allNeighbors.putAll(expansionsByDist.get(dist));
    }
    return allNeighbors;
  }

  /**
   * Returns the ring (exact BFS distance) of cells at distance 'distTarget' from (row,col), as a
   * Map<Direction,Cell<T>>.
   */
  public Map<Direction, Cell<T>> getNeighborsAtDistance(
      Grid<T> grid, int startRow, int startCol, int distTarget) {
    List<Map<Direction, Cell<T>>> expansionsByDist = bfsExpansions(grid, startRow, startCol,
        distTarget);
    if (distTarget < expansionsByDist.size()) {
      return expansionsByDist.get(distTarget);
    }
    return Collections.emptyMap();
  }

  private List<Map<Direction, Cell<T>>> bfsExpansions(Grid<T> grid, int startRow, int startCol,
      int maxDist) {
    List<Map<Direction, Cell<T>>> expansions = new ArrayList<>();
    for (int d = 0; d <= maxDist; d++) {
      expansions.add(new HashMap<>());
    }
    Queue<BFSNode> queue = new ArrayDeque<>();
    queue.add(new BFSNode(startRow, startCol, 0, new Direction(0, 0)));
    Set<String> visited = new HashSet<>();
    visited.add(startRow + "," + startCol);

    while (!queue.isEmpty()) {
      BFSNode node = queue.poll();
      int r = node.row, c = node.col, d = node.dist;
      if (d > 0 && d <= maxDist) {
        expansions.get(d).put(node.offsetFromOriginal, grid.getCell(r, c));
      }
      if (d < maxDist) {
        List<BFSNode> nextLayer = computeNextLayer(grid, node);
        for (BFSNode next : nextLayer) {
          String key = next.row + "," + next.col;
          if (!visited.contains(key)) {
            visited.add(key);
            queue.add(next);
          }
        }
      }
    }
    return expansions;
  }

  private List<BFSNode> computeNextLayer(Grid<T> grid, BFSNode node) {
    List<BFSNode> result = new ArrayList<>();
    String baseKey = shape + "_" + neighborType;
    List<Direction> baseOffsets = NEIGHBOR_MAP.get(baseKey);
    if (baseOffsets == null) {
      throw new IllegalArgumentException("No offsets found for key: " + baseKey);
    }
    int r = node.row, c = node.col, d = node.dist;
    Direction prevOffset = node.offsetFromOriginal;
    int numRows = grid.getNumRows(), numCols = grid.getNumCols();

    boolean flip = false;
    if (shape == GridShape.HEX) {
      flip = (c % 2 != 0);
    } else if (shape == GridShape.TRI) {
      flip = ((r + c) % 2 != 0);
    }
    for (Direction off : baseOffsets) {
      int dy = off.dy(), dx = off.dx();
      if (flip) {
        dy = -dy;
      }
      int nr = r + dy, nc = c + dx;
      if (edgeType == EdgeType.TORUS) {
        nr = (nr + numRows) % numRows;
        nc = (nc + numCols) % numCols;
      } else {
        if (nr < 0 || nr >= numRows || nc < 0 || nc >= numCols) {
          continue;
        }
      }
      Direction newOffset = new Direction(prevOffset.dy() + dy, prevOffset.dx() + dx);
      result.add(new BFSNode(nr, nc, d + 1, newOffset));
    }
    return result;
  }

  protected static class BFSNode {

    int row, col, dist;
    Direction offsetFromOriginal;

    BFSNode(int r, int c, int d, Direction offset) {
      row = r;
      col = c;
      dist = d;
      offsetFromOriginal = offset;
    }
  }

  public Map<Direction, Cell<T>> raycastDirection(Grid<T> grid, int startRow, int startCol,
      Direction rawDir, int steps) {
    return raycastImplementor.raycast(grid, startRow, startCol, rawDir, steps);
  }

  public List<Direction> getAllRaycastDirections(int startRow, int startCol) {
    return raycastImplementor.getDefaultRawDirections(startRow, startCol);
  }

  public Map<Direction, Map<Direction, Cell<T>>> raycastAllDirections(Grid<T> grid, int startRow,
      int startCol, int steps) {
    Map<Direction, Map<Direction, Cell<T>>> results = new HashMap<>();
    for (Direction d : getAllRaycastDirections(startRow, startCol)) {
      results.put(d, raycastImplementor.raycast(grid, startRow, startCol, d, steps));
    }
    return results;
  }

  private static Map<String, List<Direction>> loadNeighborProperties() {
    Map<String, List<Direction>> map = new HashMap<>();
    Properties props = new Properties();
    try (InputStream is = NeighborCalculator.class.getClassLoader()
        .getResourceAsStream(PROPERTY_FILE)) {
      if (is == null) {
        throw new IOException("Could not find " + PROPERTY_FILE + " in resources");
      }
      props.load(is);
      for (String key : props.stringPropertyNames()) {
        String line = props.getProperty(key);
        List<Direction> dirList = parseNeighborProperties(line);
        map.put(key.toUpperCase().trim(), dirList);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to load neighbors from " + PROPERTY_FILE, e);
    }
    return map;
  }

  private static List<Direction> parseNeighborProperties(String line) {
    List<Direction> dirs = new ArrayList<>();
    String[] pairs = line.split(";");
    for (String p : pairs) {
      String[] xy = p.split(",");
      int dy = Integer.parseInt(xy[0].trim());
      int dx = Integer.parseInt(xy[1].trim());
      dirs.add(new Direction(dy, dx));
    }
    return dirs;
  }
}
