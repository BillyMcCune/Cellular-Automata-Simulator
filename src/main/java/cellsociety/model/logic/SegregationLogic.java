package cellsociety.model.logic;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.SegregationState;
import java.util.ArrayList;
import java.util.Random;

public class SegregationLogic extends Logic<SegregationState> {

  private ArrayList<Cell<SegregationState>> empty = new ArrayList<>();
  private ArrayList<Cell<SegregationState>> occupied = new ArrayList<>();

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
        else {
          occupied.add(cell);
        }
      }
    }
  }

  @Override
  protected void updateSingleCell(Cell<SegregationState> cell) {
    if (cell.getCurrentState() != SegregationState.OPEN) {
      int randomEmptyIndex = new Random().nextInt(empty.size());
      Cell<SegregationState> selectedCell = empty.get(randomEmptyIndex);

      selectedCell.setNextState(cell.getCurrentState());
      cell.setNextState(SegregationState.OPEN);

      empty.remove(randomEmptyIndex);
      occupied.remove(selectedCell);
    }
  }
}
