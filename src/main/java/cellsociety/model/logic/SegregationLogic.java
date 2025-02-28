package cellsociety.model.logic;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.SegregationState;
import java.util.ArrayList;

/**
 * Concrete implementation of {@link Logic} for the Schelling Segregation Model.
 *
 * @author Jacob You
 */
public class SegregationLogic extends Logic<SegregationState> {

  private final ArrayList<Cell<SegregationState>> empty = new ArrayList<>();
  private double satisfiedThreshold;

  /**
   * Constructs a {@code SegregationLogic} instance with the specified grid and parameters.
   *
   * @param grid       the grid representing the current state of the simulation
   * @param parameters the parameter record containing simulation-specific configurations
   * @throws IllegalArgumentException if parameters are out of bounds
   */
  public SegregationLogic(Grid<SegregationState> grid, ParameterRecord parameters)
      throws IllegalArgumentException {
    super(grid, parameters);
    setSatisfiedThreshold(getDoubleParamOrFallback("satisfiedThreshold"));

    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();

    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        Cell<SegregationState> cell = grid.getCell(row, col);
        if (cell.getCurrentState() == SegregationState.OPEN) {
          empty.add(cell);
        }
      }
    }
  }

  /**
   * Sets the satisfaction threshold, determining how many similar neighbors are required for a cell
   * to stay in place.
   *
   * @param threshold the satisfaction threshold in percentage
   * @throws IllegalArgumentException if the value is outside the allowed range
   */
  public void setSatisfiedThreshold(double threshold) throws IllegalArgumentException {
    double min = getMinParam("satisfiedThreshold");
    double max = getMaxParam("satisfiedThreshold");
    checkBounds(threshold, min, max);
    satisfiedThreshold = threshold / 100;
  }

  /**
   * Retrieves the satisfaction threshold in percentage form for external use.
   *
   * @return the satisfaction threshold as a percentage
   */
  public double getSatisfiedThreshold() {
    return satisfiedThreshold * 100;
  }

  /**
   * Updates the next state of a single cell based on its similarity to neighboring cells. If a cell
   * is unsatisfied, it moves to a random available empty location.
   *
   * @param cell the cell to update
   */
  @Override
  protected void updateSingleCell(Cell<SegregationState> cell) {
    if (empty.isEmpty()) {
      // No empty spaces available, no movement possible
      return;
    }
    if ((cell.getCurrentState() != SegregationState.OPEN) && (getProportionSimilarNeighbors(cell)
        < satisfiedThreshold)) {
      int randomEmptyIndex = (int) (Math.random() * empty.size());
      Cell<SegregationState> selectedCell = empty.get(randomEmptyIndex);

      selectedCell.setNextState(cell.getCurrentState());
      cell.setNextState(SegregationState.OPEN);

      empty.remove(selectedCell);
      empty.add(cell);
    }
  }

  /**
   * Calculates the proportion of similar neighbors surrounding a given cell.
   *
   * @param cell the cell whose neighbors are analyzed
   * @return the proportion of neighbors that share the same state
   */
  private double getProportionSimilarNeighbors(Cell<SegregationState> cell) {
    double similarNeighbors = 0;
    double totalNeighbors = 0;
    SegregationState state = cell.getCurrentState();
    for (Cell<SegregationState> neighbor : cell.getNeighbors().values()) {
      if (neighbor.getCurrentState() != SegregationState.OPEN) {
        if (neighbor.getCurrentState() == state) {
          similarNeighbors++;
        }
        totalNeighbors++;
      }
    }
    if (cell.getNeighbors().isEmpty()) {
      return 1;
    }

    double satisfaction = similarNeighbors / totalNeighbors;
    return Double.isNaN(satisfaction) ? 0 : satisfaction;
  }
}
