package cellsociety.model.logic;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.BacteriaState;
import java.util.HashMap;
import java.util.Map;

public class BacteriaLogic extends Logic<BacteriaState> {

  private double beatingThreshold;
  private int numStates;
  private final Map<Cell<BacteriaState>, Double> nextStates = new HashMap<>();

  public BacteriaLogic(Grid<BacteriaState> grid, ParameterRecord parameters) throws IllegalArgumentException{
    super(grid, parameters);
    setBeatingThreshold(getDoubleParamOrFallback("beatingThreshold"));
    setNumStates(getDoubleParamOrFallback("numStates"));
  }

  public void setBeatingThreshold(double percThreshold) throws IllegalArgumentException {
    double min = getMinParam("beatingThreshold");
    double max = getMaxParam("beatingThreshold");
    checkBounds(percThreshold, min, max);
    this.beatingThreshold = percThreshold / 100.0;
  }

  /**
   * Returns the beating threshold in percentage form for the Controller
   *
   * @return The beating threshold in percentage form
   */
  public double getBeatingThreshold() {
    return beatingThreshold * 100;
  }

  public void setNumStates(double n) throws IllegalArgumentException {
    checkBounds(n, 0, Double.MAX_VALUE);
    this.numStates = (int) n;
  }

  public double getNumStates() {
    return numStates;
  }

  @Override
  protected void updateSingleCell(Cell<BacteriaState> cell) {
    double id = cell.getProperty("id");
    double beatingId = (id + 1) % numStates;

    double numBeating = getNumBeating(cell, beatingId);
    int numNeighbors = cell.getNeighbors().size();
    if (numNeighbors != 0 && numBeating/numNeighbors >= beatingThreshold) {
      nextStates.put(cell, beatingId);
    }
  }

  @Override
  public void update() {
    for (Cell<BacteriaState> changedCell : nextStates.keySet()) {
      changedCell.setProperty("id", nextStates.get(changedCell));
    }
    grid.updateGrid();
  }

  private int getNumBeating(Cell<BacteriaState> cell, double beatingId) {
    int numBeating = 0;
    for (Cell<BacteriaState> neighbor : cell.getNeighbors().values()) {
      if (neighbor.getProperty("id") == beatingId) {
        numBeating++;
      }
    }
    return numBeating;
  }
}
