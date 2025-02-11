package cellsociety.model.logic;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.FireState;
import java.util.ArrayList;
import java.util.List;

public class FireLogic extends Logic<FireState> {

  private static double probCatch;

  public FireLogic(Grid<FireState> grid) {
    super(grid);
  }

  public static void setProbCatch(double percentCatch) {
    FireLogic.probCatch = percentCatch/100;
  }

  @Override
  protected void updateSingleCell(Cell<FireState> cell) {
    FireState currentState = cell.getCurrentState();

    if (currentState == FireState.BURNING) {
      List<Cell<FireState>> treeNeighbors = getTreeNeighbors(cell);
      for (Cell<FireState> neighbor : treeNeighbors) {
        if (Math.random() < probCatch) {
          neighbor.setNextState(FireState.BURNING);
        }
      }
      cell.setNextState(FireState.EMPTY);
    }
  }

  private List<Cell<FireState>> getTreeNeighbors(Cell<FireState> cell) {
    List<Cell<FireState>> neighbors = cell.getNeighbors();
    List<Cell<FireState>> openNeighbors = new ArrayList<>();

    for (Cell<FireState> neighbor : neighbors) {
      FireState neighborState = neighbor.getCurrentState();
      if (neighborState == FireState.TREE) {
        openNeighbors.add(neighbor);
      }
    }
    return openNeighbors;
  }
}
