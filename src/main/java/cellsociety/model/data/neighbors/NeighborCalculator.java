package cellsociety.model.data.neighbors;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.constants.BoundaryType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.states.State;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * A BFS-based universal NeighborCalculator that:
 *   - Loads neighbor definitions from a CellNeighbor.properties file
 *   - Derives a single "canonical" set of neighbors for each shape+neighborSet (e.g. "TRI_MOORE")
 *   - For each newly visited cell, we re-check orientation (for hex or tri) to possibly flip offsets
 */
public abstract class NeighborCalculator<T extends Enum<T> & State> {

  private static final String PROPERTY_FILE = "cellsociety/property/CellNeighbor.properties";
  private static final Map<String, List<Direction>> NEIGHBOR_MAP = loadNeighborProperties();

  private GridShape shape;
  private NeighborType neighborType;
  private BoundaryType boundary;
  private int steps;

  public NeighborCalculator(GridShape shape, NeighborType neighborType, BoundaryType boundary) {
    this.shape = shape;
    this.neighborType = neighborType;
    this.boundary = boundary;
    this.steps = 1;
  }
  public NeighborCalculator(GridShape shape, NeighborType neighborType, BoundaryType boundary, int steps) {
    this.shape = shape;
    this.neighborType = neighborType;
    this.boundary = boundary;
    this.steps = steps;
  }

  public List<Direction> getDirections() {
    String baseKey = shape + "_" + neighborType;
    return NEIGHBOR_MAP.get(baseKey);
  }

  public void setShape(GridShape shape) {
    this.shape = shape;
  }

  public GridShape getShape() {
    return shape;
  }

  public BoundaryType getBoundary() {
    return boundary;
  }

  public void setNeighborType(NeighborType neighborType) {
    this.neighborType = neighborType;
  }

  public NeighborType getNeighborType() {
    return neighborType;
  }

  public void setBoundary(BoundaryType boundary) {
    this.boundary = boundary;
  }

  public void setSteps(int steps) {
    this.steps = steps;
  }

  /**
   * Returns all neighbors (accumulated from BFS expansions) up to distance 'steps' from (row,col)
   * as a Map<Direction,Cell<T>>.
   *   - Direction holds the final (dy, dx) offset from the original cell.
   *   - BFS ensures each cell is unique in the result (because we mark visited).
   */
  public Map<Direction, Cell<T>> getNeighbors(Grid<T> grid, int startRow, int startCol) {
    List<Map<Direction, Cell<T>>> expansionsByDist = bfsExpansions(grid, startRow, startCol, steps);
    Map<Direction, Cell<T>> allNeighbors = new HashMap<>();
    for (int dist = 1; dist <= steps && dist < expansionsByDist.size(); dist++) {
      allNeighbors.putAll(expansionsByDist.get(dist));
    }
    return allNeighbors;
  }

  /**
   * Returns the ring (exact BFS distance) of cells at distance 'distTarget' from (row,col),
   * as a Map<Direction,Cell<T>>.
   */
  public Map<Direction, Cell<T>> getNeighborsAtDistance(
      Grid<T> grid, int startRow, int startCol, int distTarget) {
    List<Map<Direction, Cell<T>>> expansionsByDist = bfsExpansions(grid, startRow, startCol, distTarget);
    if (distTarget < expansionsByDist.size()) {
      return expansionsByDist.get(distTarget);
    }
    return Collections.emptyMap();
  }

  /**
   * Perform BFS expansions (up to maxDist) from the (startRow,startCol).
   * Returns a List of Maps (index = distance), each map of Direction => Cell<T>.
   */
  private List<Map<Direction, Cell<T>>> bfsExpansions(Grid<T> grid, int startRow, int startCol, int maxDist) {
    List<Map<Direction, Cell<T>>> expansionsByDist = initializeExpansionMaps(maxDist);

    Queue<BFSNode> queue = new ArrayDeque<>();
    queue.add(new BFSNode(startRow, startCol, 0, new Direction(0, 0)));

    Set<String> visited = new HashSet<>();
    visited.add(startRow + "," + startCol);

    while (!queue.isEmpty()) {
      BFSNode node = queue.poll();
      int r = node.row;
      int c = node.col;
      int dist = node.dist;

      if (dist <= maxDist && dist > 0) {
        expansionsByDist.get(dist).put(node.offsetFromOriginal, grid.getCell(r, c));
      }

      if (dist < maxDist) {
        List<BFSNode> nextLayer = computeNextLayer(grid, node);
        for (BFSNode next : nextLayer) {
          String keyPos = next.row + "," + next.col;
          if (!visited.contains(keyPos)) {
            visited.add(keyPos);
            queue.add(next);
          }
        }
      }
    }
    return expansionsByDist;
  }

  private List<Map<Direction, Cell<T>>> initializeExpansionMaps(int maxDist) {
    List<Map<Direction, Cell<T>>> expansions = new ArrayList<>();
    for (int d = 0; d <= maxDist; d++) {
      expansions.add(new HashMap<>());
    }
    return expansions;
  }

  /**
   * Given a BFSNode, compute all "next" BFSNodes for its neighbors, carrying forward
   * the sum of offsets so that each BFSNode knows how far from original (row,col) it is.
   */
  private List<BFSNode> computeNextLayer(Grid<T> grid, BFSNode node) {
    List<BFSNode> result = new ArrayList<>();
    String baseKey = shape + "_" + neighborType;
    List<Direction> baseOffsets = NEIGHBOR_MAP.get(baseKey);
    if (baseOffsets == null) {
      throw new IllegalArgumentException("No offset set found for key: " + baseKey);
    }

    int r = node.row;
    int c = node.col;
    int dist = node.dist;
    Direction nodeOffset = node.offsetFromOriginal;

    boolean flipHex = (shape == GridShape.HEXAGON && (c % 2 != 0));
    boolean flipTri = (shape == GridShape.TRIANGLE && ((r + c) % 2 != 0));

    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();

    for (Direction off : baseOffsets) {
      int dy = off.dy();
      int dx = off.dx();
      if (flipHex || flipTri) {
        dy = -dy;
      }
      int nr = r + dy;
      int nc = c + dx;

      if (boundary == BoundaryType.TORUS) {
        nr = (nr + numRows) % numRows;
        nc = (nc + numCols) % numCols;
      } else {
        if (nr < 0 || nr >= numRows || nc < 0 || nc >= numCols) {
          continue;
        }
      }

      Direction newOffset = new Direction(nodeOffset.dy() + dy, nodeOffset.dx() + dx);
      result.add(new BFSNode(nr, nc, dist + 1, newOffset));
    }
    return result;
  }

  /**
   * Load neighbor definitions from CellNeighbor.properties into a static map.
   */
  private static Map<String, List<Direction>> loadNeighborProperties() {
    Map<String, List<Direction>> map = new HashMap<>();
    Properties props = new Properties();
    try (InputStream is = NeighborCalculator.class.getClassLoader().getResourceAsStream(PROPERTY_FILE)) {
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
      throw new RuntimeException("Failed to load neighbors from file: " + PROPERTY_FILE, e);
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

  /**
   * Our BFS node now  tracks the cumulative offsetFromOriginal to help
   * identify how far (row/col) the node is from the start cell.
   */
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
}
