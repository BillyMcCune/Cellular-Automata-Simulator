package cellsociety.model.logic;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.FireState;
import java.util.ArrayList;
import java.util.List;

public class FireLogic extends Logic<FireState> {

  private double probCatch;
  private double probIgnite;
  private double probTree;

  public FireLogic(Grid<FireState> grid) {
    super(grid);
  }

  public void setProbCatch(double percentCatch) {
    probCatch = percentCatch/100;
  }

  public void setProbIgnite(double percentIgnite) {
    probIgnite = percentIgnite/100;
  }

  public void setProbGrowTree(double percentGrowTree) {
    probTree = percentGrowTree/100;
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
