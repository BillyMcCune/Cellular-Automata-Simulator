package cellsociety.model.data;

import cellsociety.model.config.CellRecord;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
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
   * @param rawGrid      a two-dimensional list of {@link CellRecord} representing the initial
   *                     states and properties of the cells
   * @param factory      the factory to create cells
   * @param shape        the grid shape for the grid
   * @param neighborType the type of neighbor assignment
   * @param boundary     the type of edge implementation
   */
  public Grid(List<List<CellRecord>> rawGrid, CellFactory<T> factory,
      GridShape shape,
      NeighborType neighborType,
      EdgeType boundary) {
    this.neighborCalculator = new NeighborCalculator<T>(shape, neighborType, boundary);
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
      assignNeighbors();
    }
  }

  /**
   * Retrieves a specific cell from the grid based on its row and column indices.
   *
   * @param row the row index of the desired cell
   * @param col the column index of the desired cell
   * @return the cell at the specified position
   */
  public Cell<T> getCell(int row, int col) {
    return grid.get(row).get(col);
  }

  /**
   * Sets the number of steps used in neighbor calculations.
   *
   * @param steps the number of steps to use
   */
  public void setSteps(int steps) {
    this.neighborCalculator.setSteps(steps);
  }

  /**
   * Sets the grid shape for neighbor calculations.
   *
   * @param shape the new grid shape
   */
  public void setGridShape(GridShape shape) {
    this.neighborCalculator.setShape(shape);
  }

  /**
   * Sets the neighbor type for neighbor calculations.
   *
   * @param neighborType the new neighbor type
   */
  public void setNeighborType(NeighborType neighborType) {
    this.neighborCalculator.setNeighborType(neighborType);
  }

  /**
   * Sets the edge type for neighbor calculations.
   *
   * @param edgeType the new edge type
   */
  public void setEdgeType(EdgeType edgeType) {
    this.neighborCalculator.setEdgeType(edgeType);
  }

  /**
   * Returns the current number of steps used for neighbor calculations.
   *
   * @return the number of steps
   */
  public int getSteps() {
    return this.neighborCalculator.getSteps();
  }

  /**
   * Returns the current grid shape used for neighbor calculations.
   *
   * @return the grid shape
   */
  public GridShape getShape() {
    return this.neighborCalculator.getShape();
  }

  /**
   * Returns the current edge type used for neighbor calculations.
   *
   * @return the edge type
   */
  public EdgeType getEdgeType() {
    return this.neighborCalculator.getEdgeType();
  }

  /**
   * Returns the current neighbor type used for neighbor calculations.
   *
   * @return the neighbor type
   */
  public NeighborType getNeighborType() {
    return this.neighborCalculator.getNeighborType();
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
   * @return the number of columns in the grid, or 0 if the grid is empty
   */
  public int getNumCols() {
    if (!grid.isEmpty()) {
      return grid.get(0).size();
    }
    return 0;
  }

  /**
   * Returns the {@link NeighborCalculator} associated with this grid.
   *
   * @return the neighbor calculator
   */
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
   * Assigns neighbors for each {@link Cell} in the grid using the current
   * {@link NeighborCalculator}.
   */
  public void assignNeighbors() {
    for (int row = 0; row < getNumRows(); row++) {
      for (int col = 0; col < getNumCols(); col++) {
        getCell(row, col).setNeighbors(neighborCalculator.getNeighbors(this, row, col));
      }
    }
  }

  /**
   * Assigns raycast neighbors for a given cell in a specific direction and for a specified number
   * of steps.
   *
   * @param cell      the cell to assign raycast neighbors to
   * @param direction the direction in which to perform the raycast
   * @param steps     the number of steps to raycast
   */
  public void assignRaycastNeighbor(Cell<T> cell, Direction direction, int steps) {
    int[] coordinates = getCellCoordinates(cell);
    Map<Direction, Cell<T>> neighbors = neighborCalculator.raycastDirection(this, coordinates[0],
        coordinates[1], direction, steps);
    cell.setNeighbors(neighbors);
  }

  /**
   * Assigns raycast neighbors for all cells in the grid for a specified number of steps.
   *
   * @param steps the number of steps to raycast in all directions for each cell
   */
  public void assignAllRaycastNeighbors(int steps) {
    for (int row = 0; row < getNumRows(); row++) {
      for (int col = 0; col < getNumCols(); col++) {
        Map<Direction, Map<Direction, Cell<T>>> allRays = neighborCalculator.raycastAllDirections(
            this, row, col, steps);
        Map<Direction, Cell<T>> merged = new HashMap<>();
        for (Map<Direction, Cell<T>> ray : allRays.values()) {
          merged.putAll(ray);
        }
        getCell(row, col).setNeighbors(merged);
      }
    }
  }

  private int[] getCellCoordinates(Cell<T> cell) {
    for (int row = 0; row < getNumRows(); row++) {
      for (int col = 0; col < getNumCols(); col++) {
        if (getCell(row, col).equals(cell)) {
          return new int[]{row, col};
        }
      }
    }
    return new int[]{0, 0};
  }

  /**
   * Retrieves all possible raycast directions for a given cell.
   *
   * @param cell the cell for which to obtain the raycast directions
   * @return a list of all possible raycast directions based on the cell's position
   */
  public List<Direction> getAllRaycastDirections(Cell<T> cell) {
    int[] coordinates = getCellCoordinates(cell);
    return neighborCalculator.getAllRaycastDirections(coordinates[0], coordinates[1]);
  }

  /**
   * Retrieves the list of available directions for the given cell using the neighbor calculator.
   *
   * @param cell the cell for which to obtain available directions
   * @return a list of directions based on the cell's position
   */
  public List<Direction> getDirections(Cell<T> cell) {
    int[] coordinates = getCellCoordinates(cell);
    return neighborCalculator.getDirections(coordinates[0], coordinates[1]);
  }
}
