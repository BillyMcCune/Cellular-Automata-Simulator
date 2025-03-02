package cellsociety.model.data.neighbors;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.State;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * A BFS-based universal NeighborCalculator that:
 *   - Loads neighbor definitions from a CellDirection.properties file
 *   - Derives a single "canonical" set of neighbors for each shape+neighborSet (e.g. "TRI_MOORE")
 *   - For each newly visited cell, we re-check orientation and possibly flip the neighbors
 *     (for hex or tri).
 *   - We can gather all cells up to 'steps' distance away in BFS order.
 *   - We store expansions by distance, i.e. expansions[d] = set of all cells at distance d.
 *     (Or you can flatten them all into one set/list.)
 */
public abstract class NeighborCalculator<T extends Enum<T> & State> {

  private static final String PROPERTY_FILE = "cellsociety/property/CellNeighbor.properties";
  private static final Map<String, List<Direction>> NEIGHBOR_MAP = loadNeighborProperties();

  private final String shape;
  private final String neighborSet;
  private final boolean isTorus;

  /**
   * Constructs a BFS-based neighbor calculator.
   *
   * @param shape e.g. "SQUARE","HEX","TRI"
   * @param neighborSet e.g. "MOORE","NEUMANN"
   * @param isTorus true if the grid is toroidal
   */
  public NeighborCalculator(String shape, String neighborSet, boolean isTorus) {
    this.shape = shape.toUpperCase().trim();
    this.neighborSet = neighborSet.toUpperCase().trim();
    this.isTorus = isTorus;
  }

  public List<Direction> getDirections() {
    String baseKey = shape + "_" + neighborSet;
    return NEIGHBOR_MAP.get(baseKey);
  }

  /**
   * Returns all cells up to distance 'steps' from (row,col), inclusive.
   * Distance is measured in BFS steps. For each newly visited cell, we re-check orientation
   * (for hex or tri) to get the correct neighbor set. The BFS ensures we collect the correct ring
   * of neighbors even if orientation changes from cell to cell.
   *
   * @return The cells in the area within steps
   */
  public Set<Cell<T>> getNeighbors(Grid<T> grid, int startRow, int startCol, int steps) {
    Set<Cell<T>> expansions = new HashSet<>();
    Queue<BFSNode> queue = new ArrayDeque<>();
    Set<String> visited = new HashSet<>();
    queue.add(new BFSNode(startRow, startCol, 0));
    visited.add(startRow + "," + startCol);

    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();

    while (!queue.isEmpty()) {
      BFSNode node = queue.poll();
      int r = node.row;
      int c = node.col;
      int dist = node.dist;
      if (dist > 0) {
        expansions.add(grid.getCell(r, c));
      }
      if (dist == steps) {
        continue;
      }

      String baseKey = shape + "_" + neighborSet;
      List<Direction> baseOffsets = NEIGHBOR_MAP.get(baseKey);
      if (baseOffsets == null) {
        throw new IllegalArgumentException("No offset set found for key: " + baseKey);
      }

      boolean flipHex = shape.equals("HEX") && (r % 2 != 0);
      boolean flipTri = shape.equals("TRI") && ((r + c) % 2 != 0);

      for (Direction off : baseOffsets) {
        int dy = off.dy();
        int dx = off.dx();
        if (flipHex || flipTri) {
          dy = -dy;
        }
        int nr = r + dy;
        int nc = c + dx;

        if (isTorus) {
          nr = (nr + numRows) % numRows;
          nc = (nc + numCols) % numCols;
        } else {
          if (nr < 0 || nr >= numRows || nc < 0 || nc >= numCols) {
            continue;
          }
        }
        String keyPos = nr + "," + nc;
        if (!visited.contains(keyPos)) {
          visited.add(keyPos);
          queue.add(new BFSNode(nr, nc, dist + 1));
        }
      }
    }
    return expansions;
  }

  /**
   * Returns the set of cells exactly at distance 'dist' from (row,col). This is the BFS ring.
   */
  public Set<Cell<T>> getNeighborsAtDistance(Grid<T> grid, int startRow, int startCol, int distTarget) {
    Set<Cell<T>> ring = new HashSet<>();
    Queue<BFSNode> queue = new ArrayDeque<>();
    Set<String> visited = new HashSet<>();
    queue.add(new BFSNode(startRow, startCol, 0));
    visited.add(startRow + "," + startCol);

    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();

    while (!queue.isEmpty()) {
      BFSNode node = queue.poll();
      int r = node.row;
      int c = node.col;
      int dist = node.dist;

      if (dist == distTarget && !(r==startRow && c==startCol)) {
        ring.add(grid.getCell(r, c));
      }

      if (dist == distTarget) {
        continue;
      }

      // same orientation logic
      String baseKey = shape + "_" + neighborSet;
      List<Direction> baseNeighbors = NEIGHBOR_MAP.get(baseKey);
      if (baseNeighbors == null) {
        throw new IllegalArgumentException("No neighbors set found for key: " + baseKey);
      }
      boolean flipHex = shape.equals("HEX") && (r % 2 != 0);
      boolean flipTri = shape.equals("TRI") && ((r + c) % 2 != 0);

      for (Direction off : baseNeighbors) {
        int dy = off.dy();
        int dx = off.dx();
        if (flipHex || flipTri) {
          dy = -dy;
        }
        int nr = r + dy;
        int nc = c + dx;

        if (isTorus) {
          nr = (nr + numRows) % numRows;
          nc = (nc + numCols) % numCols;
        } else {
          if (nr < 0 || nr >= numRows || nc < 0 || nc >= numCols) {
            continue;
          }
        }
        String keyPos = nr + "," + nc;
        if (!visited.contains(keyPos)) {
          visited.add(keyPos);
          queue.add(new BFSNode(nr, nc, dist + 1));
        }
      }
    }
    return ring;
  }

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

  protected static class BFSNode {
    int row, col, dist;
    BFSNode(int r, int c, int d) {
      row = r; col = c; dist = d;
    }
  }
}
