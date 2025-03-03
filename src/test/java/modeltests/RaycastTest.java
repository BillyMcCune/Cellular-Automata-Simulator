package modeltests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cellsociety.model.config.CellRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.constants.EdgeType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.data.neighbors.raycasting.RaycastImplementor;
import cellsociety.model.data.states.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RaycastTest {

  private static final CellFactory<DummyState> FACTORY = new CellFactory<>(DummyState.class);

  public enum DummyState implements State {
    DUMMY;
    @Override
    public int getValue() { return 0; }
  }

  private static class DummyNeighborCalculator extends NeighborCalculator<DummyState> {
    public DummyNeighborCalculator(GridShape shape, NeighborType neighborType, EdgeType edgeType) {
      super(shape, neighborType, edgeType);
    }
  }

  private Grid<DummyState> createGrid(int rows, int cols, GridShape shape, EdgeType boundary) {
    List<List<CellRecord>> raw = new ArrayList<>();
    for (int r = 0; r < rows; r++) {
      List<CellRecord> row = new ArrayList<>();
      for (int c = 0; c < cols; c++) {
        row.add(new CellRecord(0, new HashMap<>()));
      }
      raw.add(row);
    }
    return new Grid<>(raw, FACTORY, shape, NeighborType.NEUMANN, boundary);
  }

  @BeforeEach
  public void setup() {}

  @Test
  public void givenSquareGrid_WhenRaycastingSingleDirection_ThenReturnsExpectedCells() throws Exception {
    Grid<DummyState> grid = createGrid(5, 5, GridShape.SQUARE, EdgeType.BASE);
    RaycastImplementor<DummyState> impl = new RaycastImplementor<>(GridShape.SQUARE, EdgeType.BASE);
    Map<Direction, Cell<DummyState>> result = impl.raycast(grid, 2, 2, new Direction(-1, 0), 2);

    assertEquals(2, result.size());
    assertEquals(grid.getCell(1, 2), result.get(new Direction(-1, 0)));
    assertEquals(grid.getCell(0, 2), result.get(new Direction(-2, 0)));
  }

  @Test
  public void givenSquareGrid_WhenRaycastingAllDirectionsTorus_ThenEachDirectionHasExpectedSteps() throws Exception {
    Grid<DummyState> grid = createGrid(7, 7, GridShape.SQUARE, EdgeType.TORUS);
    RaycastImplementor<DummyState> impl = new RaycastImplementor<>(GridShape.SQUARE, EdgeType.TORUS);
    List<Direction> dirs = impl.getDefaultRawDirections(3, 3);
    assertEquals(4, dirs.size());

    for (Direction d : dirs) {
      Map<Direction, Cell<DummyState>> result = impl.raycast(grid, 3, 3, d, 3);
      assertEquals(3, result.size());
    }
  }

  @Test
  public void givenHexGridEvenRow_WhenRaycastingSingleDirection_ThenReturnsExpectedCells() throws Exception {
    Grid<DummyState> grid = createGrid(6, 6, GridShape.HEX, EdgeType.BASE);
    RaycastImplementor<DummyState> impl = new RaycastImplementor<>(GridShape.HEX, EdgeType.BASE);
    Map<Direction, Cell<DummyState>> result = impl.raycast(grid, 2, 2, new Direction(1, 1), 3);

    assertEquals(3, result.size());
    assertEquals(grid.getCell(3, 3), result.get(new Direction(1, 1)));
    assertEquals(grid.getCell(3, 4), result.get(new Direction(1, 2)));
    assertEquals(grid.getCell(4, 5), result.get(new Direction(2, 3)));
  }

  @Test
  public void givenHexGrid_WhenRaycastingAllDirectionsTorus_ThenEachDirectionHasExpectedSteps() throws Exception {
    Grid<DummyState> grid = createGrid(6, 6, GridShape.HEX, EdgeType.TORUS);
    RaycastImplementor<DummyState> impl = new RaycastImplementor<>(GridShape.HEX, EdgeType.TORUS);
    List<Direction> dirs = impl.getDefaultRawDirections(5, 4);
    assertEquals(6, dirs.size());

    for (Direction d : dirs) {
      Map<Direction, Cell<DummyState>> result = impl.raycast(grid, 4, 4, d, 4);
      assertEquals(4, result.size());
    }
  }

  @Test
  public void givenTriangleGridUpFacing_WhenRaycastingSingleDirection_ThenReturnsExpectedCells() throws Exception {
    Grid<DummyState> grid = createGrid(6, 6, GridShape.TRI, EdgeType.BASE);
    RaycastImplementor<DummyState> impl = new RaycastImplementor<>(GridShape.TRI, EdgeType.BASE);
    Map<Direction, Cell<DummyState>> result = impl.raycast(grid, 2, 2, new Direction(0, -1), 2);

    assertEquals(2, result.size());
    assertEquals(grid.getCell(2, 1), result.get(new Direction(0, -1)));
    assertEquals(grid.getCell(2, 0), result.get(new Direction(0, -2)));
  }

  @Test
  public void givenTriangleGridDownFacing_WhenRaycastingSingleDirection_ThenReturnsExpectedCells() throws Exception {
    Grid<DummyState> grid = createGrid(6, 6, GridShape.TRI, EdgeType.BASE);
    RaycastImplementor<DummyState> impl = new RaycastImplementor<>(GridShape.TRI, EdgeType.BASE);
    Map<Direction, Cell<DummyState>> result = impl.raycast(grid, 3, 2, new Direction(0, -1), 2);

    assertEquals(2, result.size());
    assertEquals(grid.getCell(3, 1), result.get(new Direction(0, -1)));
    assertEquals(grid.getCell(3, 0), result.get(new Direction(0, -2)));
  }

  @Test
  public void givenTriangleGrid_WhenRaycastingAllDirectionsTorus_ThenEachDirectionHasExpectedSteps() throws Exception {
    Grid<DummyState> grid = createGrid(10, 10, GridShape.TRI, EdgeType.TORUS);
    RaycastImplementor<DummyState> impl = new RaycastImplementor<>(GridShape.TRI, EdgeType.TORUS);
    List<Direction> dirs = impl.getDefaultRawDirections(5, 5);
    assertEquals(6, dirs.size());

    for (Direction d : dirs) {
      Map<Direction, Cell<DummyState>> result = impl.raycast(grid, 5, 5, d, 3);
      assertEquals(3, result.size());
    }
  }
}
