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
    Object stored = cell.getProperty("id");
    if (stored == null) {
      cell.setProperty("id", 0);
      cell.setNextState(BacteriaState.DUMMY);
      return;
    }
    int id = (int) stored;
    int beatingId = (id + 1) % numStates;

    int numBeating = getNumBeating(cell, beatingId);
    int numNeighbors = cell.getNeighbors().size();
    if (numNeighbors != 0 && (double) numBeating/numNeighbors >= beatingThreshold) {
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

  private int getNumBeating(Cell<BacteriaState> cell, int beatingId) {
    int numBeating = 0;
    for (Cell<BacteriaState> neighbor : cell.getNeighbors().values()) {
      if (neighbor.getProperty("id").equals(beatingId)) {
        numBeating++;
      }
    }
    return numBeating;
  }
}
