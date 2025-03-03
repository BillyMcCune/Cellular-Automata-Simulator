package modeltests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cellsociety.model.config.CellRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.constants.BoundaryType;
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

  private Grid<DummyState> createGrid(int rows, int cols, NeighborCalculator<DummyState> neighborCalculator) {
    List<List<CellRecord>> raw = new ArrayList<>();
    for (int r = 0; r < rows; r++) {
      List<CellRecord> row = new ArrayList<>();
      for (int c = 0; c < cols; c++) {
        row.add(new CellRecord(0, new HashMap<>()));
      }
      raw.add(row);
    }
    return new Grid<>(raw, FACTORY, neighborCalculator);
  }

  @Test
  public void givenSquareMoore3x3_whenSteps1_thenCenterHas8() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.SQUARE, NeighborType.MOORE, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(3, 3, calc);
    calc.setSteps(1);
    Map<Direction, Cell<DummyState>> s = calc.getNeighbors(g, 1, 1);
    assertEquals(8, s.size());
  }

  @Test
  public void givenSquareMoore5x5_whenSteps2_thenTotalCells24() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.SQUARE, NeighborType.MOORE, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(5, 5, calc);
    calc.setSteps(2);
    Map<Direction, Cell<DummyState>> s = calc.getNeighbors(g, 2, 2);
    assertEquals(24, s.size());
  }

  @Test
  public void givenSquareMoore5x5_whenSteps2InCorner_thenTotalCells8() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.SQUARE, NeighborType.MOORE, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(5, 5, calc);
    calc.setSteps(2);
    Map<Direction, Cell<DummyState>> s = calc.getNeighbors(g, 0, 0);
    assertEquals(8, s.size());
  }

  @Test
  public void givenSquareNeumann3x3_whenSteps1_thenCenterHas4() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.SQUARE, NeighborType.NEUMANN, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(3, 3, calc);
    calc.setSteps(1);
    Map<Direction, Cell<DummyState>> s = calc.getNeighbors(g, 1, 1);
    assertEquals(4, s.size());
  }

  @Test
  public void givenSquareNeumann5x5_whenRingDistance2_then8Neighbors() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.SQUARE, NeighborType.NEUMANN, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(5, 5, calc);
    Map<Direction, Cell<DummyState>> s = calc.getNeighborsAtDistance(g, 2, 2, 2);
    assertEquals(8, s.size());
  }

  @Test
  public void givenSquareMoore3x3Torus_whenTopLeftSteps1_thenWrap8() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.SQUARE, NeighborType.MOORE, BoundaryType.TORUS);
    Grid<DummyState> g = createGrid(3, 3, calc);
    calc.setSteps(1);
    Map<Direction, Cell<DummyState>> s = calc.getNeighbors(g, 0, 0);
    assertEquals(8, s.size());
  }

  @Test
  public void givenHexMoore5x5_whenSteps1_then6Neighbors() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.HEX, NeighborType.MOORE, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(5, 5, calc);
    calc.setSteps(1);
    Map<Direction, Cell<DummyState>> s = calc.getNeighbors(g, 2, 2);
    assertEquals(6, s.size());
  }

  @Test
  public void givenHexMoore7x7_whenSteps2_then18Neighbors() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.HEX, NeighborType.MOORE, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(5, 5, calc);
    calc.setSteps(2);
    Map<Direction, Cell<DummyState>> s = calc.getNeighbors(g, 2, 2);
    assertEquals(18, s.size());
  }

  @Test
  public void givenHexNeumann5x5_whenSteps1_then2Neighbors() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.HEX, NeighborType.NEUMANN, BoundaryType.TORUS);
    Grid<DummyState> g = createGrid(5, 5, calc);
    calc.setSteps(1);
    Map<Direction, Cell<DummyState>> s = calc.getNeighbors(g, 0, 0);
    assertEquals(2, s.size());
  }

  @Test
  public void givenTriMoore6x6_whenSteps1Triangle_then12Neighbors() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.TRI, NeighborType.MOORE, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(6, 6, calc);
    calc.setSteps(1);
    Map<Direction, Cell<DummyState>> s = calc.getNeighbors(g, 1, 3);
    assertEquals(12, s.size());
  }

  @Test
  public void givenTriMoore9x9_whenSteps2Triangle_then36Neighbors() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.TRI, NeighborType.MOORE, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(9, 9, calc);
    calc.setSteps(2);
    Map<Direction, Cell<DummyState>> s = calc.getNeighbors(g, 4, 4);
    assertEquals(36, s.size());
  }

  @Test
  public void givenTriMoore6x6_whenRingDist1Triangle_thenRing6Neighbors() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.TRI, NeighborType.MOORE, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(6, 6, calc);
    Map<Direction, Cell<DummyState>> s = calc.getNeighborsAtDistance(g, 3, 3, 1);
    assertEquals(12, s.size());
  }

  @Test
  public void givenTriMoore9x9_whenRingDist2Triangle_thenRing24Neighbors() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.TRI, NeighborType.MOORE, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(9, 9, calc);
    Map<Direction, Cell<DummyState>> s = calc.getNeighborsAtDistance(g, 4, 4, 2);
    assertEquals(24, s.size());
  }

  @Test
  public void givenTriNeumann6x6Torus_whenSteps1Triangle_then3Neighbors() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.TRI, NeighborType.NEUMANN, BoundaryType.TORUS);
    Grid<DummyState> g = createGrid(6, 6, calc);
    calc.setSteps(1);
    Map<Direction, Cell<DummyState>> s = calc.getNeighbors(g, 2, 2);
    assertEquals(3, s.size());
  }

  @Test
  public void givenSquareMoore3x3_whenSteps1_thenContainsExpectedDirections() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.SQUARE, NeighborType.MOORE, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(3, 3, calc);
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
  public void givenSquareNeumann3x3_whenSteps1_thenContainsExpectedDirections() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.SQUARE, NeighborType.NEUMANN, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(3, 3, calc);
    calc.setSteps(1);
    Map<Direction, Cell<DummyState>> neighbors = calc.getNeighbors(g, 1, 1);

    Direction[] expectedDirections = {
        new Direction(-1, 0), new Direction(0, -1),
        new Direction(0, 1), new Direction(1, 0)
    };

    for (Direction d : expectedDirections) {
      assertTrue(neighbors.containsKey(d), "Missing direction: " + d);
    }
  }

  @Test
  public void givenHexMoore5x5_whenEvenSteps1_thenContainsExpectedDirections() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.HEX, NeighborType.MOORE, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(5, 5, calc);
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
  public void givenHexMoore6x6_whenOddSteps1_thenContainsExpectedDirections() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.HEX, NeighborType.MOORE, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(6, 6, calc);
    calc.setSteps(1);
    Map<Direction, Cell<DummyState>> neighbors = calc.getNeighbors(g, 2, 3);
    System.out.println(neighbors.keySet());

    Direction[] expectedDirections = {
        new Direction(-1, -1), new Direction(-1, 0), new Direction(-1, 1),
        new Direction(0, -1), new Direction(1, 0), new Direction(0, 1)
    };

    for (Direction d : expectedDirections) {
      assertTrue(neighbors.containsKey(d), "Missing direction: " + d);
    }
  }

  @Test
  public void givenHexNeumann5x5_whenSteps1_thenContainsExpectedDirections() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.HEX, NeighborType.NEUMANN, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(5, 5, calc);
    calc.setSteps(1);
    Map<Direction, Cell<DummyState>> neighbors = calc.getNeighbors(g, 2, 2);

    Direction[] expectedDirections = {
        new Direction(-1, 0), new Direction(1, 0)
    };

    for (Direction d : expectedDirections) {
      assertTrue(neighbors.containsKey(d), "Missing direction: " + d);
    }
  }

  @Test
  public void givenTriMoore6x6_whenSteps1_thenContainsExpectedDirections() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.TRI, NeighborType.MOORE, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(6, 6, calc);
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
  public void givenTriNeumann6x6_whenDownSteps1_thenContainsExpectedDirections() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.TRI, NeighborType.NEUMANN, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(6, 6, calc);
    calc.setSteps(1);
    Map<Direction, Cell<DummyState>> neighbors = calc.getNeighbors(g, 3, 3);

    Direction[] expectedDirections = {
        new Direction(0, -1), new Direction(0, 1), new Direction(-1, 0)
    };

    for (Direction d : expectedDirections) {
      assertTrue(neighbors.containsKey(d), "Missing direction: " + d);
    }
  }

  @Test
  public void givenTriNeumann6x6_whenUpSteps1_thenContainsExpectedDirections() {
    NeighborCalculator<DummyState> calc = new NeighborCalculator<DummyState>(GridShape.TRI, NeighborType.NEUMANN, BoundaryType.BASE);
    Grid<DummyState> g = createGrid(6, 6, calc);
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
