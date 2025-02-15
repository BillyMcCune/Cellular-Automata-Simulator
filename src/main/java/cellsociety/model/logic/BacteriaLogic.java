package cellsociety.model.logic;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.BacteriaState;
import java.util.HashMap;
import java.util.Map;

public class BacteriaLogic extends Logic<BacteriaState> {

  private double beatingThreshold;
  private int numStates;
  private final Map<Cell<BacteriaState>, Integer> nextStates = new HashMap<>();

  public BacteriaLogic(Grid<BacteriaState> grid) {
    super(grid);
  }

  public void setBeatingThreshold(double beatingThreshold) {
    this.beatingThreshold = beatingThreshold;
  }

  public void setNumStates(int numStates) {
    this.numStates = numStates;
  }

  @Override
  protected void updateSingleCell(Cell<BacteriaState> cell) {
    int id = cell.getProperty("id");
    int beatingId = (id + 1) % numStates;

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
