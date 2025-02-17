package cellsociety.model.logic;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.FireState;
import java.util.ArrayList;
import java.util.List;

public class FireLogic extends Logic<FireState> {

  private double probCatch;
  private double probIgnite;
  private double probTree;

  public FireLogic(Grid<FireState> grid, ParameterRecord parameters) {
    super(grid, parameters);
    setProbCatch(loadDoubleProperty("FireLogic.probCatch.default"));
    setProbIgnite(loadDoubleProperty("FireLogic.probIgnite.default"));
    setProbTree(loadDoubleProperty("FireLogic.probTree.default"));
  }

  public void setProbCatch(double percentCatch) {
    double min = getMinParam("probCatch");
    double max = getMaxParam("probCatch");
    checkBounds(percentCatch, min, max);
    probCatch = percentCatch / 100.0;
  }

  public void setProbIgnite(double percentIgnite) {
    double min = getMinParam("probIgnite");
    double max = getMaxParam("probIgnite");
    checkBounds(percentIgnite, min, max);
    probIgnite = percentIgnite / 100.0;
  }

  public void setProbTree(double percentTree) {
    double min = getMinParam("probTree");
    double max = getMaxParam("probTree");
    checkBounds(percentTree, min, max);
    probTree = percentTree / 100.0;
  }

  public double getProbCatch() {
    return probCatch * 100;
  }

  public double getProbIgnite() {
    return probIgnite * 100;
  }

  public double getProbTree() {
    return probTree * 100;
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
