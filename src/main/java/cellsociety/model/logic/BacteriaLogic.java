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

  public BacteriaLogic(Grid<BacteriaState> grid, ParameterRecord parameters) {
    super(grid, parameters);
  }

  public void setPercBeatingThreshold(double beatingThreshold) {
    this.beatingThreshold = beatingThreshold/100;
  }

  public void setNumStates(double numStates) {
    this.numStates = (int) numStates;
  }

  public double getBeatingThreshold() {
    return beatingThreshold;
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
