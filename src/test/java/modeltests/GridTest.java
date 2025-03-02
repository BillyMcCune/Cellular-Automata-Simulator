package modeltests;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.config.CellRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.constants.BoundaryType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.data.states.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author Jacob You
 */
public class GridTest {

  private enum TestState implements State {
    ZERO(0), ONE(1);
    private final int value;

    TestState(int value) {
      this.value = value;
    }

    @Override
    public int getValue() {
      return value;
    }

    public static TestState fromInt(Class<TestState> enumClass, int value) {
      for (TestState s : enumClass.getEnumConstants()) {
        if (s.getValue() == value) {
          return s;
        }
      }
      return enumClass.getEnumConstants()[0];
    }
  }

  private static class DummyCellFactory extends CellFactory<TestState> {

    public DummyCellFactory() {
      super(TestState.class);
    }
  }

  private static class DummyNeighborCalculator extends NeighborCalculator<TestState> {
    public DummyNeighborCalculator() {
      super(GridShape.SQUARE, NeighborType.MOORE, BoundaryType.STANDARD);
    }
  }



  private List<List<CellRecord>> createRawGrid(int rows, int cols, int defaultState) {
    List<List<CellRecord>> raw = new ArrayList<>();
    for (int i = 0; i < rows; i++) {
      List<CellRecord> row = new ArrayList<>();
      for (int j = 0; j < cols; j++) {
        Map<String, Double> props = new HashMap<>();
        props.put("dummy", 1.0);
        row.add(new CellRecord(defaultState, props));
      }
      raw.add(row);
    }
    return raw;
  }

  private Grid<TestState> createGrid(List<List<CellRecord>> raw) {
    DummyCellFactory factory = new DummyCellFactory();
    DummyNeighborCalculator neighborCalc = new DummyNeighborCalculator();
    return new Grid<>(raw, factory, neighborCalc);
  }

  @Test
  public void Grid_Constructor_ValidRawGrid_CorrectDimensions() {
    List<List<CellRecord>> raw = createRawGrid(3, 4, 0);
    Grid<TestState> grid = createGrid(raw);
    assertEquals(3, grid.getNumRows());
    assertEquals(4, grid.getNumCols());
  }

  @Test
  public void Grid_AssignNeighbors_CornerCell_HasThreeNeighbors() {
    List<List<CellRecord>> raw = createRawGrid(3, 3, 0);
    Grid<TestState> grid = createGrid(raw);
    Cell<TestState> cell = grid.getCell(0, 0);
    assertEquals(3, cell.getNeighbors().size());
  }

  @Test
  public void Grid_AssignNeighbors_EdgeCell_HasFiveNeighbors() {
    List<List<CellRecord>> raw = createRawGrid(3, 3, 0);
    Grid<TestState> grid = createGrid(raw);
    Cell<TestState> cell = grid.getCell(0, 1);
    assertEquals(5, cell.getNeighbors().size());
  }

  @Test
  public void Grid_AssignNeighbors_CenterCell_HasEightNeighbors() {
    List<List<CellRecord>> raw = createRawGrid(3, 3, 0);
    Grid<TestState> grid = createGrid(raw);
    Cell<TestState> cell = grid.getCell(1, 1);
    assertEquals(8, cell.getNeighbors().size());
  }

  @Test
  public void Grid_UpdateGrid_ChangesCurrentState() {
    List<List<CellRecord>> raw = createRawGrid(2, 2, 0);
    Grid<TestState> grid = createGrid(raw);
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        grid.getCell(i, j).setNextState(TestState.ONE);
      }
    }
    grid.updateGrid();
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(TestState.ONE, grid.getCell(i, j).getCurrentState());
      }
    }
  }

  @Test
  public void Grid_SetGrid_ReinitializesGrid() {
    List<List<CellRecord>> raw1 = createRawGrid(2, 2, 0);
    Grid<TestState> grid = createGrid(raw1);
    List<List<CellRecord>> raw2 = createRawGrid(3, 3, 1);
    grid.setGrid(raw2, new DummyCellFactory());
    assertEquals(3, grid.getNumRows());
    assertEquals(3, grid.getNumCols());
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        assertEquals(TestState.ONE, grid.getCell(i, j).getCurrentState());
      }
    }
  }

  @Test
  public void Grid_Constructor_NullRawGrid_ReturnsZeroDimensions() {
    DummyCellFactory factory = new DummyCellFactory();
    Grid<TestState> grid = new Grid<>(null, factory, new DummyNeighborCalculator());
    assertEquals(0, grid.getNumRows());
    assertEquals(0, grid.getNumCols());
  }

  @Test
  public void Grid_Constructor_EmptyRawGrid_ReturnsZeroDimensions() {
    DummyCellFactory factory = new DummyCellFactory();
    List<List<CellRecord>> raw = new ArrayList<>();
    Grid<TestState> grid = new Grid<>(raw, factory, new DummyNeighborCalculator());
    assertEquals(0, grid.getNumRows());
    assertEquals(0, grid.getNumCols());
  }

  @Test
  public void Grid_SetGrid_NullRawGrid_ReturnsZeroDimensions() {
    List<List<CellRecord>> raw = createRawGrid(2, 2, 0);
    Grid<TestState> grid = createGrid(raw);
    grid.setGrid(null, new DummyCellFactory());
    assertEquals(0, grid.getNumRows());
    assertEquals(0, grid.getNumCols());
  }

  @Test
  public void Grid_GetCell_InvalidRowIndex_ThrowsException() {
    List<List<CellRecord>> raw = createRawGrid(2, 2, 0);
    Grid<TestState> grid = createGrid(raw);
    assertThrows(IndexOutOfBoundsException.class, () -> grid.getCell(2, 0));
  }

  @Test
  public void Grid_GetCell_InvalidColumnIndex_ThrowsException() {
    List<List<CellRecord>> raw = createRawGrid(2, 2, 0);
    Grid<TestState> grid = createGrid(raw);
    assertThrows(IndexOutOfBoundsException.class, () -> grid.getCell(0, 2));
  }

  @Test
  public void Grid_CopyPropertiesAndClearProperties_WorksCorrectly() {
    List<List<CellRecord>> raw = createRawGrid(2, 2, 0);
    Grid<TestState> grid = createGrid(raw);
    Cell<TestState> cell = grid.getCell(0, 0);
    cell.setProperty("energy", 5.0);
    Map<String, Double> props = cell.getAllProperties();
    Cell<TestState> cell2 = grid.getCell(0, 1);
    cell.copyAllPropertiesTo(cell2);
    assertEquals(props, cell2.getAllProperties());
    cell.clearAllProperties();
    assertNull(cell.getAllProperties());
  }

  @Test
  public void Grid_AssignNeighbors_PropertiesOfNeighbors_NotNull() {
    List<List<CellRecord>> raw = createRawGrid(3, 3, 0);
    Grid<TestState> grid = createGrid(raw);
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        Cell<TestState> cell = grid.getCell(i, j);
        for (Map.Entry<?, ?> entry : cell.getNeighbors().entrySet()) {
          assertNotNull(entry.getValue());
        }
      }
    }
  }

  @Test
  public void Grid_UpdateGrid_DoesNotAlterNeighborMapping() {
    List<List<CellRecord>> raw = createRawGrid(3, 3, 0);
    Grid<TestState> grid = createGrid(raw);
    Map<?, ?> originalNeighbors = new HashMap<>(grid.getCell(1, 1).getNeighbors());
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        grid.getCell(i, j).setNextState(TestState.ONE);
      }
    }
    grid.updateGrid();
    Map<?, ?> updatedNeighbors = grid.getCell(1, 1).getNeighbors();
    assertEquals(originalNeighbors.keySet(), updatedNeighbors.keySet());
  }
}
