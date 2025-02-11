package cellsociety.model.logic;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.FallingState;
import cellsociety.model.data.states.PercolationState;

public class FallingLogic extends Logic<FallingState>{

  public FallingLogic(Grid<FallingState> grid) {
    super(grid);
  }

  @Override
  public void update() {
    int row = (int) (Math.random() * grid.getNumRows());
    int col = (int) (Math.random() * grid.getNumCols());
    updateSingleCell(grid.getCell(row, col));
  }

  @Override
  protected void updateSingleCell(Cell<FallingState> cell) {
    if (cell.getCurrentState() == FallingState.SAND) {
      //FALL
    }
  }
}
