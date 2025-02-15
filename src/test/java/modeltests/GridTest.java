package modeltests;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.data.Grid;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.states.State;

public class GridTest {

  private enum TestState implements State {
    ZERO(0), ONE(1);
    private final int value;
    TestState(int value) { this.value = value; }
    @Override
    public int getValue() { return value; }
    public static TestState fromInt(Class<TestState> enumClass, int value) {
      for (TestState s : enumClass.getEnumConstants()) {
        if (s.getValue() == value) return s;
      }
      return enumClass.getEnumConstants()[0];
    }
  }

  private static class DummyCellFactory extends CellFactory<TestState> {
    public DummyCellFactory() {
      super(TestState.class);
    }
  }

  private List<List<Integer>> createRawGrid(int rows, int cols, int defaultValue) {
    List<List<Integer>> rawGrid = new ArrayList<>();
    for (int i = 0; i < rows; i++) {
      List<Integer> row = new ArrayList<>();
      for (int j = 0; j < cols; j++) {
        row.add(defaultValue);
      }
      rawGrid.add(row);
    }
    return rawGrid;
  }

  @Test
  public void Grid_Constructor_ValidRawGrid_InitializesCorrectRowsAndColumns() {
    List<List<Integer>> rawGrid = createRawGrid(3, 4, 0);
    DummyCellFactory factory = new DummyCellFactory();
    Grid<TestState> grid = new Grid<>(rawGrid, factory, neighborCalculator);
    assertEquals(3, grid.getNumRows(), "Expected grid to have 3 rows");
    assertEquals(4, grid.getNumCols(), "Expected grid to have 4 columns");
  }

  @Test
  public void Grid_AssignNeighbors_CornerCellHasThreeNeighbors() {
    List<List<Integer>> rawGrid = createRawGrid(3, 3, 0);
    DummyCellFactory factory = new DummyCellFactory();
    Grid<TestState> grid = new Grid<>(rawGrid, factory, neighborCalculator);
    Cell<TestState> cornerCell = grid.getCell(0, 0);
    assertEquals(3, cornerCell.getNeighbors().size(), "Corner cell should have 3 neighbors");
  }

  @Test
  public void Grid_AssignNeighbors_EdgeCellHasFiveNeighbors() {
    List<List<Integer>> rawGrid = createRawGrid(3, 3, 0);
    DummyCellFactory factory = new DummyCellFactory();
    Grid<TestState> grid = new Grid<>(rawGrid, factory, neighborCalculator);
    Cell<TestState> edgeCell = grid.getCell(0, 1);
    assertEquals(5, edgeCell.getNeighbors().size(), "Edge cell should have 5 neighbors");
  }

  @Test
  public void Grid_AssignNeighbors_CenterCellHasEightNeighbors() {
    List<List<Integer>> rawGrid = createRawGrid(3, 3, 0);
    DummyCellFactory factory = new DummyCellFactory();
    Grid<TestState> grid = new Grid<>(rawGrid, factory, neighborCalculator);
    Cell<TestState> centerCell = grid.getCell(1, 1);
    assertEquals(8, centerCell.getNeighbors().size(), "Center cell should have 8 neighbors");
  }

  @Test
  public void Grid_UpdateGrid_CellsCurrentStateUpdatesToNextState() {
    List<List<Integer>> rawGrid = createRawGrid(2, 2, 0);
    DummyCellFactory factory = new DummyCellFactory();
    Grid<TestState> grid = new Grid<>(rawGrid, factory, neighborCalculator);
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        grid.getCell(i, j).setNextState(TestState.ONE);
      }
    }
    grid.updateGrid();
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(TestState.ONE, grid.getCell(i, j).getCurrentState(),
            "After update, each cell's current state should be ONE");
      }
    }
  }

  @Test
  public void Grid_SetGrid_NewRawGrid_ReinitializesGridCorrectly() {
    List<List<Integer>> initialRawGrid = createRawGrid(2, 2, 0);
    DummyCellFactory factory = new DummyCellFactory();
    Grid<TestState> grid = new Grid<>(initialRawGrid, factory, neighborCalculator);
    assertEquals(2, grid.getNumRows(), "Initially, grid should have 2 rows");
    List<List<Integer>> newRawGrid = createRawGrid(3, 3, 1);
    grid.setGrid(newRawGrid, factory);
    assertEquals(3, grid.getNumRows(), "After setGrid, grid should have 3 rows");
    assertEquals(3, grid.getNumCols(), "After setGrid, grid should have 3 columns");
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(TestState.ONE, grid.getCell(i, j).getCurrentState(),
            "After setGrid with raw value 1, cell state should be ONE");
      }
    }
  }

  @Test
  public void Grid_Constructor_NullRawGrid_ReturnsZeroDimensionGrid() {
    DummyCellFactory factory = new DummyCellFactory();
    Grid<TestState> grid = new Grid<>(null, factory, neighborCalculator);
    assertEquals(0, grid.getNumRows());
    assertEquals(0, grid.getNumCols());
  }

  @Test
  public void Grid_Constructor_EmptyRawGrid_ReturnsZeroDimensionGrid() {
    DummyCellFactory factory = new DummyCellFactory();
    List<List<Integer>> rawGrid = new ArrayList<>();
    Grid<TestState> grid = new Grid<>(rawGrid, factory, neighborCalculator);
    assertEquals(0, grid.getNumRows());
    assertEquals(0, grid.getNumCols());
  }

  @Test
  public void Grid_SetGrid_NullRawGrid_ReturnsZeroDimensionGrid() {
    List<List<Integer>> rawGrid = createRawGrid(2, 2, 0);
    DummyCellFactory factory = new DummyCellFactory();
    Grid<TestState> grid = new Grid<>(rawGrid, factory, neighborCalculator);
    grid.setGrid(null, factory);
    assertEquals(0, grid.getNumRows());
    assertEquals(0, grid.getNumCols());
  }

  // Errors that should never be accessible by user input

  @Test
  public void Grid_GetCell_InvalidRowIndex_ThrowsIndexOutOfBoundsException() {
    List<List<Integer>> rawGrid = createRawGrid(2, 2, 0);
    DummyCellFactory factory = new DummyCellFactory();
    Grid<TestState> grid = new Grid<>(rawGrid, factory, neighborCalculator);
    assertThrows(IndexOutOfBoundsException.class, () -> grid.getCell(2, 0));
  }

  @Test
  public void Grid_GetCell_InvalidColumnIndex_ThrowsIndexOutOfBoundsException() {
    List<List<Integer>> rawGrid = createRawGrid(2, 2, 0);
    DummyCellFactory factory = new DummyCellFactory();
    Grid<TestState> grid = new Grid<>(rawGrid, factory, neighborCalculator);
    assertThrows(IndexOutOfBoundsException.class, () -> grid.getCell(0, 2));
  }
}
