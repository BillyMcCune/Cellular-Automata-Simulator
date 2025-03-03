package modeltests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cellsociety.model.config.CellRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.constants.BoundaryType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.data.neighbors.raycasting.RaycastImplementor;
import cellsociety.model.data.states.State;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RaycastTest {

  private static final CellFactory<DummyState> FACTORY = new CellFactory<>(DummyState.class);

  public enum DummyState implements State {
    DUMMY;
    @Override
    public int getValue() { return 0; }
  }

  private static class DummyNeighborCalculator extends
      NeighborCalculator<NeighborCalculatorTest.DummyState> {
    public DummyNeighborCalculator(GridShape shape, NeighborType neighborType, BoundaryType boundaryType) {
      super(shape, neighborType, boundaryType);
    }
  }

  private Grid<DummyState> createGrid(int rows, int cols, GridShape shape, BoundaryType boundary) {
    List<List<CellRecord>> raw = new ArrayList<>();
    for (int r = 0; r < rows; r++) {
      List<CellRecord> row = new ArrayList<>();
      for (int c = 0; c < cols; c++) {
        row.add(new CellRecord(0, new HashMap<>()));
      }
      raw.add(row);
    }
    DummyNeighborCalculator neighborCalculator = new DummyNeighborCalculator(shape, NeighborType.NEUMANN, boundary);
    return new Grid<>(raw, FACTORY, neighborCalculator);
  }

  @BeforeEach
  public void setup() {}

  @Test
  public void testSquareSingleDirectionStandard() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Grid<DummyState> grid = createGrid(5, 5, GridShape.SQUARE, BoundaryType.STANDARD);
    RaycastImplementor<DummyState> impl = new RaycastImplementor<>(GridShape.SQUARE, BoundaryType.STANDARD);
    List<Cell<DummyState>> path = impl.raycast(grid, 2, 2, new Direction(-1, 0), 2);
    assertEquals(2, path.size());
    assertEquals(grid.getCell(1, 2), path.get(0));
    assertEquals(grid.getCell(0, 2), path.get(1));
  }

  @Test
  public void testSquareAllDirectionsTorus() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Grid<DummyState> grid = createGrid(7, 7, GridShape.SQUARE, BoundaryType.TORUS);
    RaycastImplementor<DummyState> impl = new RaycastImplementor<>(GridShape.SQUARE, BoundaryType.TORUS);
    List<Direction> dirs = impl.getDefaultRawDirections(3, 3);
    assertEquals(4, dirs.size());
    for (Direction d : dirs) {
      List<Cell<DummyState>> path = impl.raycast(grid, 3, 3, d, 3);
      assertEquals(3, path.size());
    }
  }

  @Test
  public void testHexSingleDirectionStandardEvenRow() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Grid<DummyState> grid = createGrid(6, 6, GridShape.HEX, BoundaryType.STANDARD);
    RaycastImplementor<DummyState> impl = new RaycastImplementor<>(GridShape.HEX, BoundaryType.STANDARD);
    List<Cell<DummyState>> path = impl.raycast(grid, 2, 2, new Direction(1, 1), 3);
    assertEquals(3, path.size());
    assertEquals(grid.getCell(3, 3), path.get(0));
    assertEquals(grid.getCell(3, 4), path.get(1));
    assertEquals(grid.getCell(4, 5), path.get(2));
  }

  @Test
  public void testHexSingleDirectionStandardOddRow() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Grid<DummyState> grid = createGrid(7, 7, GridShape.HEX, BoundaryType.STANDARD);
    RaycastImplementor<DummyState> impl = new RaycastImplementor<>(GridShape.HEX, BoundaryType.STANDARD);
    List<Cell<DummyState>> path = impl.raycast(grid, 3, 3, new Direction(-1, 1), 3);
    assertEquals(3, path.size());
    assertEquals(grid.getCell(2, 4), path.get(0));
    assertEquals(grid.getCell(2, 5), path.get(1));
    assertEquals(grid.getCell(2, 6), path.get(2));
  }

  @Test
  public void testHexAllDirectionsTorus() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Grid<DummyState> grid = createGrid(6, 6, GridShape.HEX, BoundaryType.TORUS);
    RaycastImplementor<DummyState> impl = new RaycastImplementor<>(GridShape.HEX, BoundaryType.TORUS);
    List<Direction> dirs = impl.getDefaultRawDirections(5, 4);
    assertEquals(6, dirs.size());
    for (Direction d : dirs) {
      List<Cell<DummyState>> path = impl.raycast(grid, 4, 4, d, 4);
      assertEquals(4, path.size());
    }
  }

  @Test
  public void testTriSingleDirectionStandardUpFacing() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Grid<DummyState> grid = createGrid(6, 6, GridShape.TRI, BoundaryType.STANDARD);
    RaycastImplementor<DummyState> impl = new RaycastImplementor<>(GridShape.TRI, BoundaryType.STANDARD);
    List<Cell<DummyState>> path = impl.raycast(grid, 2, 2, new Direction(0, -1), 2);
    assertEquals(2, path.size());
    assertEquals(grid.getCell(2, 1), path.get(0));
    assertEquals(grid.getCell(2, 0), path.get(1));
  }

  @Test
  public void testTriSingleDirectionStandardDownFacing() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Grid<DummyState> grid = createGrid(6, 6, GridShape.TRI, BoundaryType.STANDARD);
    RaycastImplementor<DummyState> impl = new RaycastImplementor<>(GridShape.TRI, BoundaryType.STANDARD);
    List<Cell<DummyState>> path = impl.raycast(grid, 3, 2, new Direction(0, -1), 2);
    assertEquals(2, path.size());
    assertEquals(grid.getCell(3, 1), path.get(0));
    assertEquals(grid.getCell(3, 0), path.get(1));
  }

  @Test
  public void testTriAllDirectionsTorus() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Grid<DummyState> grid = createGrid(10, 10, GridShape.TRI, BoundaryType.TORUS);
    RaycastImplementor<DummyState> impl = new RaycastImplementor<>(GridShape.TRI, BoundaryType.TORUS);
    List<Direction> dirs = impl.getDefaultRawDirections(5, 5);
    assertEquals(6, dirs.size());
    for (Direction d : dirs) {
      List<Cell<DummyState>> path = impl.raycast(grid, 5, 5, d, 3);
      assertEquals(3, path.size());
    }
  }
}