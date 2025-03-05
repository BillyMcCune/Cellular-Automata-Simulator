package cellsociety.model.logic;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.BacteriaState;
import java.util.HashMap;
import java.util.Map;

/**
 * Concrete implementation of {@link Logic} for the Bacteria simulation. This class encapsulates the
 * rules and state transitions for a bacteria model based on cell interactions. It uses a beating
 * threshold to determine when a cell's state should change.
 *
 * <p>The simulation parameters (e.g., beatingThreshold, numStates) are provided via a
 * {@link ParameterRecord} and are validated upon construction.
 *
 * @author Jacob You
 */
public class BacteriaLogic extends Logic<BacteriaState> {

  private double beatingThreshold;
  private int numStates;
  private final Map<Cell<BacteriaState>, Double> nextStates = new HashMap<>();

  /**
   * Constructs a {@code BacteriaLogic} instance with the specified grid and parameters.
   *
   * @param grid       The grid representing the simulation state.
   * @param parameters The parameters defining the simulation behavior.
   * @throws IllegalArgumentException if any parameter value is out of bounds or missing.
   */
  public BacteriaLogic(Grid<BacteriaState> grid, ParameterRecord parameters)
      throws IllegalArgumentException {
    super(grid, parameters);
    setBeatingThreshold(getDoubleParamOrFallback("beatingThreshold"));
    setNumStates(getDoubleParamOrFallback("numStates"));
  }

  /**
   * Sets the threshold for the percentage of surrounding cells that must beat the current cell to
   * change the current cell state.
   *
   * @param percThreshold The percentage of surrounding cells required to change the cell.
   * @throws IllegalArgumentException if the beatingThreshold is out of bounds or invalid.
   */
  public void setBeatingThreshold(double percThreshold) throws IllegalArgumentException {
    double min = getMinParam("beatingThreshold");
    double max = getMaxParam("beatingThreshold");
    checkBounds(percThreshold, min, max);
    this.beatingThreshold = percThreshold / 100.0;
  }

  /**
   * Returns the beating threshold in percentage form.
   *
   * @return The beating threshold as a percentage.
   */
  public double getBeatingThreshold() {
    return beatingThreshold * 100;
  }

  /**
   * Sets the total number of states in the simulation.
   *
   * @param n The number of states in the simulation.
   * @throws IllegalArgumentException if the number of states is out of bounds or invalid.
   */
  public void setNumStates(double n) throws IllegalArgumentException {
    checkBounds(n, 1, Double.MAX_VALUE);
    this.numStates = (int) n;
  }

  /**
   * Returns the number of states in the simulation.
   *
   * @return The total number of states.
   */
  public double getNumStates() {
    return numStates;
  }

  /**
   * Updates the simulation by processing each cell in the grid. For each cell, it computes the next
   * state based on the beating rules, updates cell properties, and then applies all state changes
   * to the grid.
   */
  @Override
  public void update() {
    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();

    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        Cell<BacteriaState> cell = grid.getCell(row, col);
        updateSingleCell(cell);
      }
    }

    for (Cell<BacteriaState> changedCell : nextStates.keySet()) {
      changedCell.setProperty("coloredId", nextStates.get(changedCell));
    }
    grid.updateGrid();
  }

  @Override
  protected void updateSingleCell(Cell<BacteriaState> cell) {
    double id = cell.getProperty("coloredId");
    double beatingId = (id + 1) % numStates;

    double numBeating = getNumBeating(cell, beatingId);
    int numNeighbors = cell.getNeighbors().size();
    if (numNeighbors != 0 && numBeating / numNeighbors >= beatingThreshold) {
      nextStates.put(cell, beatingId);
    }
  }

  private int getNumBeating(Cell<BacteriaState> cell, double beatingId) {
    int numBeating = 0;
    for (Cell<BacteriaState> neighbor : cell.getNeighbors().values()) {
      if (neighbor.getProperty("coloredId") == beatingId) {
        numBeating++;
      }
    }
    return numBeating;
  }
}