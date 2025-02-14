package cellsociety.model.data;

import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.neighbors.Coord;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.data.states.State;
import cellsociety.model.data.states.WatorState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a grid of cells for cellular automata models.
 *
 * @param <T> the enum type representing the cell state
 */
public class Grid<T extends Enum<T> & State> {

  /**
   * Represents all directions
   */
  private static final int[][] ORTHOGONAL_DIAGONAL_DIRECTIONS = {
      {-1, -1}, {-1,  0}, {-1,  1}, { 0, -1}, { 0,  1}, { 1, -1}, { 1,  0}, { 1,  1}
  };
  private static final int[][] ORTHOGONAL_NEIGHBORS = {
      {-1, 0}, {1, 0}, {0, -1}, {0, 1}
  };
  private Class<?> simulationType;
  private NeighborCalculator<T> neighborCalculator;

  /**
   * The two-dimensional grid of {@link Cell} objects.
   */
  private final List<List<Cell<T>>> grid = new ArrayList<>();

  /**
   * Constructs a {@code Grid} from a two-dimensional list of states and a cell factory. Each state
   * in the list represents the initial state of a corresponding {@link Cell}.
   *
   * @param rawGrid            a two-dimensional list of integer states representing the initial
   *                           states of the cells
   * @param factory            the factory to create cells
   * @param neighborCalculator the calculator method to assign neighbors
   */
  public Grid(List<List<Integer>> rawGrid, CellFactory<T> factory, NeighborCalculator<?> neighborCalculator) {
    this.neighborCalculator = (NeighborCalculator<T>) neighborCalculator;
    setGrid(rawGrid, factory);
  }

  /**
   * Initializes the grid by creating cell instances based on the provided states and factory.
   *
   * @param rawGrid a two-dimensional list of states
   * @param factory the factory to create cells
   */
  private void initializeGrid(List<List<Integer>> rawGrid, CellFactory<T> factory) {
    for (List<Integer> rowStates : rawGrid) {
      List<Cell<T>> newRow = new ArrayList<>();
      for (Integer state : rowStates) {
        Cell<T> cell = factory.createCell(state);
        if (simulationType == null) {
          simulationType = cell.getCurrentState().getClass();
        }
        newRow.add(cell);
      }
      grid.add(newRow);
    }
  }

  private void assignNeighbors() {
    if (grid.isEmpty()) return;
    for (int row = 0; row < getNumRows(); row++) {
      for (int col = 0; col < getNumCols(); col++) {
        Cell<T> cell = getCell(row, col);
        Map<Coord, Cell<T>> neighbors = neighborCalculator.getNeighbors(this, row, col);
        cell.setNeighbors(neighbors);
      }
    }
  }

  /**
   * Sets a new grid from a two-dimensional list of states and a cell factory.
   *
   * @param rawGrid a two-dimensional list of integer states representing the new states of the cells
   * @param factory the factory to create cells
   */
  public void setGrid(List<List<Integer>> rawGrid, CellFactory<T> factory) {
    grid.clear();
    if (rawGrid != null && !rawGrid.isEmpty()) {
      initializeGrid(rawGrid, factory);
      assignNeighbors();
    }
  }

  /**
   * Retrieves a specific cell from the grid based on its row and column indices.
   *
   * @param row the row index of the desired cell
   * @param col the column index of the desired cell
   * @return the {@link Cell<T>} at the specified position
   */
  public Cell<T> getCell(int row, int col) {
    return grid.get(row).get(col);
  }

  /**
   * Updates all cells in the grid by setting each cell's current state to its next state.
   * This should be called after all cells have had their next states computed based on the automaton's rules.
   */
  public void updateGrid() {
    for (List<Cell<T>> row : grid) {
      for (Cell<T> cell : row) {
        cell.update();
      }
    }
  }

  /**
   * Returns the number of rows in the grid.
   *
   * @return the number of rows in the grid
   */
  public int getNumRows() {
    return grid.size();
  }

  /**
   * Returns the number of columns in the grid.
   *
   * @return the number of columns in the grid
   */
  public int getNumCols() {
    if (!grid.isEmpty()) {
      return grid.getFirst().size();
    }
    return 0;
  }
}
