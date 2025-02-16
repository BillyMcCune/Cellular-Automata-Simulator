package cellsociety.model.logic;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.SugarState;
import java.util.List;

public class SugarLogic extends Logic<SugarState> {
  private static List<Cell<SugarState>> sugarCells;
  private static List<Cell<SugarState>> agentCells;

  public SugarLogic(Grid<SugarState> grid) {
    super(grid, );
  }

  @Override
  public void update() {
  }

  @Override
  protected void updateSingleCell(Cell<SugarState> cell) {

  }

}
