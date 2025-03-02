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
 *   - Gathers cells up to some BFS distance, either as a cumulative set or ring at exactly distance d
 */
public abstract class NeighborCalculator<T extends Enum<T> & State> {

  private static final String PROPERTY_FILE = "cellsociety/property/CellNeighbor.properties";
  private static final Map<String, List<Direction>> NEIGHBOR_MAP = loadNeighborProperties();

  private final GridShape shape;
  private final NeighborType neighborType;
  private final BoundaryType boundary;

  public NeighborCalculator(GridShape shape, NeighborType neighborType, BoundaryType boundary) {
    this.shape = shape;
    this.neighborType = neighborType;
    this.boundary = boundary;
  }

  public List<Direction> getDirections() {
    String baseKey = shape + "_" + neighborType;
    return NEIGHBOR_MAP.get(baseKey);
  }

  public Set<Cell<T>> getNeighbors(Grid<T> grid, int startRow, int startCol, int steps) {
    List<Set<Cell<T>>> expansionsByDist = bfsExpansions(grid, startRow, startCol, steps);
    Set<Cell<T>> expansions = new HashSet<>();
    for (int dist = 1; dist <= steps && dist < expansionsByDist.size(); dist++) {
      expansions.addAll(expansionsByDist.get(dist));
    }
    return expansions;
  }

  public Set<Cell<T>> getNeighborsAtDistance(Grid<T> grid, int startRow, int startCol, int distTarget) {
    List<Set<Cell<T>>> expansionsByDist = bfsExpansions(grid, startRow, startCol, distTarget);
    if (distTarget < expansionsByDist.size()) {
      return expansionsByDist.get(distTarget);
    }
    return Collections.emptySet();
  }

  private List<Set<Cell<T>>> bfsExpansions(Grid<T> grid, int startRow, int startCol, int maxDist) {
    List<Set<Cell<T>>> expansions = initializeExpansionSets(maxDist);
    Queue<BFSNode> queue = initializeBFSQueue(startRow, startCol);
    Set<String> visited = initializeVisitedSet(startRow, startCol);

    while (!queue.isEmpty()) {
      BFSNode node = queue.poll();
      processNode(grid, node, maxDist, startRow, startCol, expansions);

      if (node.dist < maxDist) {
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
    return expansions;
  }

  private List<Set<Cell<T>>> initializeExpansionSets(int maxDist) {
    List<Set<Cell<T>>> expansions = new ArrayList<>();
    for (int d = 0; d <= maxDist; d++) {
      expansions.add(new HashSet<>());
    }
    return expansions;
  }

  private Queue<BFSNode> initializeBFSQueue(int startRow, int startCol) {
    Queue<BFSNode> queue = new ArrayDeque<>();
    queue.add(new BFSNode(startRow, startCol, 0));
    return queue;
  }

  private Set<String> initializeVisitedSet(int startRow, int startCol) {
    Set<String> visited = new HashSet<>();
    visited.add(startRow + "," + startCol);
    return visited;
  }

  private void processNode(Grid<T> grid, BFSNode node, int maxDist,
      int startRow, int startCol,
      List<Set<Cell<T>>> expansions) {
    int r = node.row;
    int c = node.col;
    int dist = node.dist;
    if (dist <= maxDist && !(r == startRow && c == startCol) && dist > 0) {
      expansions.get(dist).add(grid.getCell(r, c));
    }
  }

  private List<BFSNode> computeNextLayer(Grid<T> grid, BFSNode node) {
    String baseKey = shape + "_" + neighborType;
    List<Direction> baseOffsets = NEIGHBOR_MAP.get(baseKey);
    if (baseOffsets == null) {
      throw new IllegalArgumentException("No offset set found for key: " + baseKey);
    }

    List<BFSNode> result = new ArrayList<>();
    int r = node.row;
    int c = node.col;
    int dist = node.dist;
    boolean flipHex = shape.equals(GridShape.HEX) && (r % 2 != 0);
    boolean flipTri = shape.equals(GridShape.TRI) && ((r + c) % 2 != 0);

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
      result.add(new BFSNode(nr, nc, dist + 1));
    }
    return result;
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
