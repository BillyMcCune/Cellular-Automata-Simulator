package cellsociety.model.data;

import cellsociety.model.config.CellRecord;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.data.states.State;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a grid of cells for cellular automata models.
 *
 * @param <T> the enum type representing the cell state
 * @author Jacob You
 */
public class Grid<T extends Enum<T> & State> {

  private Class<?> simulationType;
  private NeighborCalculator<T> neighborCalculator;

  /**
   * The two-dimensional grid of {@link Cell} objects.
   */
  private List<List<Cell<T>>> grid = new ArrayList<>();

  /**
   * Constructs a {@code Grid} from a two-dimensional list of states and a cell factory. Each state
   * in the list represents the initial state of a corresponding {@link Cell}.
   *
   * @param rawGrid            a two-dimensional list of {@link CellRecord} representing the initial
   *                           states and properties of the cells
   * @param factory            the factory to create cells
   * @param neighborCalculator the calculator method to assign neighbors
   */
  public Grid(List<List<CellRecord>> rawGrid, CellFactory<T> factory,
      NeighborCalculator<?> neighborCalculator) {
    this.neighborCalculator = (NeighborCalculator<T>) neighborCalculator;
    setGrid(rawGrid, factory);
  }

  /**
   * Sets a new grid from a two-dimensional list of {@link CellRecord} and a cell factory.
   * Initializes the grid and assigns neighbors.
   *
   * @param rawGrid a two-dimensional list of {@link CellRecord} representing the new states and
   *                properties of the cells
   * @param factory the factory to create cells
   */
  public void setGrid(List<List<CellRecord>> rawGrid, CellFactory<T> factory) {
    grid.clear();
    if (rawGrid != null && !rawGrid.isEmpty()) {
      initializeGrid(rawGrid, factory);
      reassignNeighbors();
    }
  }

  /**
   * Retrieves a specific cell from the grid based on its dx and column indices.
   *
   * @param row the dx index of the desired cell
   * @param col the column index of the desired cell
   * @return the cell at the specified position
   */
  public Cell<T> getCell(int row, int col) {
    return grid.get(row).get(col);
  }

  /**
   * Updates all cells in the grid by setting each cell's current state to its next state. This
   * should be called after all cells have had their next states computed based on the automaton's
   * rules.
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

  public NeighborCalculator<T> getNeighborCalculator() {
    return neighborCalculator;
  }

  private void initializeGrid(List<List<CellRecord>> rawGrid, CellFactory<T> factory) {
    for (List<CellRecord> rowStates : rawGrid) {
      List<Cell<T>> newRow = new ArrayList<>();
      for (CellRecord record : rowStates) {
        Cell<T> cell = factory.createCell(record.state());
        cell.setAllProperties(record.properties());
        if (simulationType == null) {
          simulationType = cell.getCurrentState().getClass();
        }
        newRow.add(cell);
      }
      grid.add(newRow);
    }
  }

  /**
   * Reinitialize all neighbors according to any new changes that might have happened
   * in the NeighborCalculator class
   */
  public void reassignNeighbors() {
    if (grid.isEmpty()) {
      return;
    }
    for (int row = 0; row < getNumRows(); row++) {
      for (int col = 0; col < getNumCols(); col++) {
        Cell<T> cell = getCell(row, col);
        Map<Direction, Cell<T>> neighbors = neighborCalculator.getNeighbors(this, row, col);
        cell.setNeighbors(neighbors);
      }
    }
  }
}
