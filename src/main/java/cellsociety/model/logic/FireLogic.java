package cellsociety.model.logic;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.FireState;
import java.util.ArrayList;
import java.util.List;

public class FireLogic extends Logic<FireState> {

  private static double probCatch;
  private static double probIgnite;
  private static double probTree;

  public FireLogic(Grid<FireState> grid) {
    super(grid);
  }

  public static void setProbCatch(double percentCatch) {
    FireLogic.probCatch = percentCatch/100;
  }

  public static void setProbIgnite(double percentIgnite) {
    FireLogic.probIgnite = percentIgnite/100;
  }

  public static void setProbGrowTree(double percentGrowTree) {
    FireLogic.probTree = percentGrowTree/100;
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
    else if (currentState == FireState.TREE) {
      if (Math.random() < probIgnite) {
        cell.setNextState(FireState.BURNING);
      }
    }
    else if (currentState == FireState.EMPTY) {
      if (Math.random() < probTree) {
        cell.setNextState(FireState.TREE);
      }
    }
  }

  private List<Cell<FireState>> getTreeNeighbors(Cell<FireState> cell) {
    List<Cell<FireState>> openNeighbors = new ArrayList<>();

    for (Cell<FireState> neighbor : cell.getNeighbors().values()) {
      FireState neighborState = neighbor.getCurrentState();
      if (neighborState == FireState.TREE) {
        openNeighbors.add(neighbor);
      }
    }
    return openNeighbors;
  }
}
