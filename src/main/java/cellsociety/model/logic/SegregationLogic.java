package cellsociety.model.logic;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.SegregationState;
import java.util.ArrayList;

public class SegregationLogic extends Logic<SegregationState> {

  private final ArrayList<Cell<SegregationState>> empty = new ArrayList<>();
  private double satisfiedThreshold;

  public SegregationLogic(Grid<SegregationState> grid) {
    super(grid, );

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

  public void setSatisfiedThreshold(double percSatisfiedThreshold) {
    satisfiedThreshold = percSatisfiedThreshold/100;
  }

  public double getSatisfiedThreshold() {
    return satisfiedThreshold;
  }

  @Override
  protected void updateSingleCell(Cell<SegregationState> cell) {
    if (empty.isEmpty()) {
      // NO EMPTY SPOTS, the entire grid is gridlocked
      return;
    }
    if ((cell.getCurrentState() != SegregationState.OPEN) && (getProportionSimilarNeighbors(cell) < satisfiedThreshold)) {
      int randomEmptyIndex = (int) (Math.random() * empty.size());
      Cell<SegregationState> selectedCell = empty.get(randomEmptyIndex);

      selectedCell.setNextState(cell.getCurrentState());
      cell.setNextState(SegregationState.OPEN);

      empty.remove(selectedCell);
      empty.add(cell);
    }
  }

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
    if (Double.isNaN(satisfaction)) {
      return 0;
    } else {
      return (satisfaction);
    }
  }
}
