package modeltests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cellsociety.model.config.CellRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.states.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

  private static class DummyNeighborCalculator extends NeighborCalculator<DummyState> {

    public DummyNeighborCalculator(String shape, String neighborSet, boolean isTorus) {
      super(shape, neighborSet, isTorus);
    }
  }

  private Grid<DummyState> createGrid(int rows, int cols) {
    List<List<CellRecord>> raw = new ArrayList<>();
    for (int r = 0; r < rows; r++) {
      List<CellRecord> row = new ArrayList<>();
      for (int c = 0; c < cols; c++) {
        row.add(new CellRecord(0, new HashMap<>()));
      }
      raw.add(row);
    }
    return new Grid<>(raw, FACTORY, null);
  }

  @Test
  public void givenSquareMoore3x3_whenSteps1_thenCenterHas8() {
    Grid<DummyState> g = createGrid(3, 3);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("square", "moore", false);
    Set<Cell<DummyState>> s = calc.getNeighbors(g, 1, 1, 1);
    assertEquals(8, s.size());
  }

  @Test
  public void givenSquareMoore5x5_whenSteps2_thenTotalCells24() {
    Grid<DummyState> g = createGrid(5, 5);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("square", "moore", false);
    Set<Cell<DummyState>> s = calc.getNeighbors(g, 2, 2, 2);
    assertEquals(24, s.size());
  }

  @Test
  public void givenSquareMoore5x5_whenSteps2InCorner_thenTotalCells8() {
    Grid<DummyState> g = createGrid(5, 5);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("square", "moore", false);
    Set<Cell<DummyState>> s = calc.getNeighbors(g, 0, 0, 2);
    assertEquals(8, s.size());
  }

  @Test
  public void givenSquareMoore3x3_whenRingDistance1_then8NeighborsCenter() {
    Grid<DummyState> g = createGrid(3, 3);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("square", "moore", false);
    Set<Cell<DummyState>> s = calc.getNeighborsAtDistance(g, 1, 1, 1);
    assertEquals(8, s.size());
  }

  @Test
  public void givenSquareMoore5x5_whenRingDistance2_thenTotalCells16() {
    Grid<DummyState> g = createGrid(5, 5);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("square", "moore", false);
    Set<Cell<DummyState>> s = calc.getNeighborsAtDistance(g, 2, 2, 2);
    assertEquals(16, s.size());
  }

  @Test
  public void givenSquareMoore3x3_whenRingDistance2_thenNoCells() {
    Grid<DummyState> g = createGrid(3, 3);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("square", "moore", false);
    Set<Cell<DummyState>> s = calc.getNeighborsAtDistance(g, 1, 1, 2);
    assertEquals(0, s.size());
  }

  @Test
  public void givenSquareNeumann3x3_whenSteps1_thenCenterHas4() {
    Grid<DummyState> g = createGrid(3, 3);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("square", "neumann", false);
    Set<Cell<DummyState>> s = calc.getNeighbors(g, 1, 1, 1);
    assertEquals(4, s.size());
  }

  @Test
  public void givenSquareNeumann3x3_whenRingDistance1_then4NeighborsCenter() {
    Grid<DummyState> g = createGrid(3, 3);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("square", "neumann", false);
    Set<Cell<DummyState>> s = calc.getNeighborsAtDistance(g, 1, 1, 1);
    assertEquals(4, s.size());
  }

  @Test
  public void givenSquareNeumann3x3_whenRingDistance2InCorner_then4Neighbors() {
    Grid<DummyState> g = createGrid(3, 3);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("square", "neumann", false);
    Set<Cell<DummyState>> s = calc.getNeighborsAtDistance(g, 1, 1, 2);
    assertEquals(4, s.size());
  }

  @Test
  public void givenSquareNeumann5x5_whenRingDistance2_then8Neighbors() {
    Grid<DummyState> g = createGrid(5, 5);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("square", "neumann", false);
    Set<Cell<DummyState>> s = calc.getNeighborsAtDistance(g, 2, 2, 2);
    assertEquals(8, s.size());
  }

  @Test
  public void givenSquareNeumann7x7_whenRingDistance3_then12Neighbors() {
    Grid<DummyState> g = createGrid(7, 7);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("square", "neumann", false);
    Set<Cell<DummyState>> s = calc.getNeighborsAtDistance(g, 3, 3, 3);
    assertEquals(12, s.size());
  }

  @Test
  public void givenSquareMoore3x3Torus_whenTopLeftSteps1_thenWrap8() {
    Grid<DummyState> g = createGrid(3, 3);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("square", "moore", true);
    Set<Cell<DummyState>> s = calc.getNeighbors(g, 0, 0, 1);
    assertEquals(8, s.size());
  }

  @Test
  public void givenHexMoore5x5_whenSteps1_then6Neighbors() {
    Grid<DummyState> g = createGrid(5, 5);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("hex", "moore", false);
    Set<Cell<DummyState>> s = calc.getNeighbors(g, 2, 2, 1);
    assertEquals(6, s.size());
  }

  @Test
  public void givenHexMoore7x7_whenSteps2_then18Neighbors() {
    Grid<DummyState> g = createGrid(5, 5);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("hex", "moore", false);
    Set<Cell<DummyState>> s = calc.getNeighbors(g, 2, 2, 2);
    assertEquals(18, s.size());
  }

  @Test
  public void givenHexNeumann5x5_whenSteps1_then2Neighbors() {
    Grid<DummyState> g = createGrid(5, 5);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("hex", "neumann", true);
    Set<Cell<DummyState>> s = calc.getNeighbors(g, 0, 0, 1);
    assertEquals(2, s.size());
  }

  @Test
  public void givenTriMoore6x6_whenSteps1Triangle_then12Neighbors() {
    Grid<DummyState> g = createGrid(6, 6);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("tri", "moore", false);
    Set<Cell<DummyState>> s = calc.getNeighbors(g, 1, 3, 1);
    assertEquals(12, s.size());
  }

  @Test
  public void givenTriMoore9x9_whenSteps2Triangle_then36Neighbors() {
    Grid<DummyState> g = createGrid(9, 9);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("tri", "moore", false);
    Set<Cell<DummyState>> s = calc.getNeighbors(g, 4, 4, 2);
    assertEquals(36, s.size());
  }

  @Test
  public void givenTriMoore6x6_whenRingDist1Triangle_thenRing6Neighbors() {
    Grid<DummyState> g = createGrid(6, 6);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("tri", "moore", false);
    Set<Cell<DummyState>> s = calc.getNeighborsAtDistance(g, 3, 3, 1);
    assertEquals(12, s.size());
  }

  @Test
  public void givenTriMoore9x9_whenRingDist2Triangle_thenRing24Neighbors() {
    Grid<DummyState> g = createGrid(9, 9);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("tri", "moore", false);
    Set<Cell<DummyState>> s = calc.getNeighborsAtDistance(g, 4, 4, 2);
    assertEquals(24, s.size());
  }

  @Test
  public void givenTriNeumann6x6Torus_whenSteps1Triangle_then3Neighbors() {
    Grid<DummyState> g = createGrid(6, 6);
    DummyNeighborCalculator calc = new DummyNeighborCalculator("tri", "neumann", true);
    Set<Cell<DummyState>> s = calc.getNeighbors(g, 2, 2, 1);
    assertEquals(3, s.size());
  }
}