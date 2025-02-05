package cellsociety.model.logic;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.SegregationState;
import java.util.ArrayList;

public class SegregationLogic extends Logic<SegregationState> {

  private final ArrayList<Cell<SegregationState>> empty = new ArrayList<>();
  private static double satisfiedThreshold;

  public SegregationLogic(Grid<SegregationState> grid) {
    super(grid);

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

  public static void setSatisfiedThreshold(double satisfiedThreshold) {
    SegregationLogic.satisfiedThreshold = satisfiedThreshold;
  }

  @Override
  protected void updateSingleCell(Cell<SegregationState> cell) {
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
    for (Cell<SegregationState> neighbor : cell.getNeighbors()) {
      if (neighbor.getCurrentState() != SegregationState.OPEN) {
        if (neighbor.getCurrentState() == state) {
          similarNeighbors++;
        }
        totalNeighbors++;
      }
    }
    if (totalNeighbors == 0) {
      return 0;
    }
    return (similarNeighbors/totalNeighbors);
  }
}
