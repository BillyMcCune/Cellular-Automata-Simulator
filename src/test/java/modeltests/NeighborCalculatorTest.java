package modeltests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cellsociety.model.config.CellRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.constants.EdgeType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.data.states.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class NeighborCalculatorTest {

  private static final CellFactory<DummyState> FACTORY = new CellFactory<>(DummyState.class);

  public enum DummyState implements State {
    DUMMY;

    @Override
    public int getValue() {
      return 0;
    }
  }

  private Grid<DummyState> createGrid(int rows, int cols, GridShape shape, NeighborType type, EdgeType edgeType) {
    List<List<CellRecord>> raw = new ArrayList<>();
    for (int r = 0; r < rows; r++) {
      List<CellRecord> row = new ArrayList<>();
      for (int c = 0; c < cols; c++) {
        row.add(new CellRecord(0, new HashMap<>()));
      }
      raw.add(row);
    }
    return new Grid<>(raw, FACTORY, shape, type, edgeType);
  }

  @Test
  public void givenSquareMoore3x3_whenSteps1_thenCenterHas8() {
    Grid<DummyState> g = createGrid(3, 3, GridShape.SQUARE, NeighborType.MOORE, EdgeType.BASE);
    NeighborCalculator<DummyState> calc = g.getNeighborCalculator();
    calc.setSteps(1);
    Map<Direction, Cell<DummyState>> neighbors = calc.getNeighbors(g, 1, 1);
    assertEquals(8, neighbors.size());
  }

  @Test
  public void givenSquareMoore5x5_whenSteps2_thenTotalCells24() {
    Grid<DummyState> g = createGrid(5, 5, GridShape.SQUARE, NeighborType.MOORE, EdgeType.BASE);
    NeighborCalculator<DummyState> calc = g.getNeighborCalculator();
    calc.setSteps(2);
    Map<Direction, Cell<DummyState>> neighbors = calc.getNeighbors(g, 2, 2);
    assertEquals(24, neighbors.size());
  }

  @Test
  public void givenSquareNeumann3x3_whenSteps1_thenCenterHas4() {
    Grid<DummyState> g = createGrid(3, 3, GridShape.SQUARE, NeighborType.NEUMANN, EdgeType.BASE);
    NeighborCalculator<DummyState> calc = g.getNeighborCalculator();
    calc.setSteps(1);
    Map<Direction, Cell<DummyState>> neighbors = calc.getNeighbors(g, 1, 1);
    assertEquals(4, neighbors.size());
  }

  @Test
  public void givenHexMoore7x7_whenSteps2_then18Neighbors() {
    Grid<DummyState> g = createGrid(7, 7, GridShape.HEX, NeighborType.MOORE, EdgeType.BASE);
    NeighborCalculator<DummyState> calc = g.getNeighborCalculator();
    calc.setSteps(2);
    Map<Direction, Cell<DummyState>> neighbors = calc.getNeighbors(g, 3, 3);
    assertEquals(18, neighbors.size());
  }

  @Test
  public void givenTriMoore6x6_whenSteps1_then12Neighbors() {
    Grid<DummyState> g = createGrid(6, 6, GridShape.TRI, NeighborType.MOORE, EdgeType.BASE);
    NeighborCalculator<DummyState> calc = g.getNeighborCalculator();
    calc.setSteps(1);
    Map<Direction, Cell<DummyState>> neighbors = calc.getNeighbors(g, 1, 3);
    assertEquals(12, neighbors.size());
  }

  @Test
  public void givenSquareMoore3x3_whenSteps1_thenContainsExpectedDirections() {
    Grid<DummyState> g = createGrid(3, 3, GridShape.SQUARE, NeighborType.MOORE, EdgeType.BASE);
    NeighborCalculator<DummyState> calc = g.getNeighborCalculator();
    calc.setSteps(1);
    Map<Direction, Cell<DummyState>> neighbors = calc.getNeighbors(g, 1, 1);

    Direction[] expectedDirections = {
        new Direction(-1, -1), new Direction(-1, 0), new Direction(-1, 1),
        new Direction(0, -1),                      new Direction(0, 1),
        new Direction(1, -1), new Direction(1, 0), new Direction(1, 1)
    };

    for (Direction d : expectedDirections) {
      assertTrue(neighbors.containsKey(d), "Missing direction: " + d);
    }
  }

  @Test
  public void givenHexMoore5x5_whenEvenSteps1_thenContainsExpectedDirections() {
    Grid<DummyState> g = createGrid(5, 5, GridShape.HEX, NeighborType.MOORE, EdgeType.BASE);
    NeighborCalculator<DummyState> calc = g.getNeighborCalculator();
    calc.setSteps(1);
    Map<Direction, Cell<DummyState>> neighbors = calc.getNeighbors(g, 2, 2);

    Direction[] expectedDirections = {
        new Direction(-1, 0), new Direction(0, -1), new Direction(0, 1),
        new Direction(1, -1), new Direction(1, 0), new Direction(1, 1)
    };

    for (Direction d : expectedDirections) {
      assertTrue(neighbors.containsKey(d), "Missing direction: " + d);
    }
  }

  @Test
  public void givenTriMoore6x6_whenSteps1_thenContainsExpectedDirections() {
    Grid<DummyState> g = createGrid(6, 6, GridShape.TRI, NeighborType.MOORE, EdgeType.BASE);
    NeighborCalculator<DummyState> calc = g.getNeighborCalculator();
    calc.setSteps(1);
    Map<Direction, Cell<DummyState>> neighbors = calc.getNeighbors(g, 3, 3);

    Direction[] expectedDirections = {
        new Direction(-1, -2), new Direction(-1, -1), new Direction(-1, 0),
        new Direction(-1, 1), new Direction(-1, 2),
        new Direction(0, -2), new Direction(0, -1), new Direction(0, 1), new Direction(0, 2),
        new Direction(1, -1), new Direction(1, 0), new Direction(1, 1)
    };

    for (Direction d : expectedDirections) {
      assertTrue(neighbors.containsKey(d), "Missing direction: " + d);
    }
  }

  @Test
  public void givenTriNeumann6x6_whenUpSteps1_thenContainsExpectedDirections() {
    Grid<DummyState> g = createGrid(6, 6, GridShape.TRI, NeighborType.NEUMANN, EdgeType.BASE);
    NeighborCalculator<DummyState> calc = g.getNeighborCalculator();
    calc.setSteps(1);
    Map<Direction, Cell<DummyState>> neighbors = calc.getNeighbors(g, 2, 3);

    Direction[] expectedDirections = {
        new Direction(0, -1), new Direction(0, 1), new Direction(1, 0)
    };

    for (Direction d : expectedDirections) {
      assertTrue(neighbors.containsKey(d), "Missing direction: " + d);
    }
  }
}
