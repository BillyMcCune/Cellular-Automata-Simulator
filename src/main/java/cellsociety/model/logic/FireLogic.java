package cellsociety.model.logic;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.FireState;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of {@link Logic} for the Fire Spread simulation.
 *
 * @author Jacob You
 */
public class FireLogic extends Logic<FireState> {

  private double probCatch;
  private double probIgnite;
  private double probTree;

  /**
   * Constructs a {@code FireLogic} instance with the specified grid and parameters.
   *
   * @param grid       The grid representing the simulation state.
   * @param parameters The parameters defining the probabilities of fire spread, ignition, and tree
   *                   growth.
   * @throws IllegalArgumentException if any parameter value is out of bounds or missing.
   */
  public FireLogic(Grid<FireState> grid, ParameterRecord parameters)
      throws IllegalArgumentException {
    super(grid, parameters);
    setProbCatch(getDoubleParamOrFallback("probCatch"));
    setProbIgnite(getDoubleParamOrFallback("probIgnite"));
    setProbTree(getDoubleParamOrFallback("probTree"));
  }

  /**
   * Sets the probability of a tree catching fire from a burning neighbor.
   *
   * @param percentCatch The probability (0-100) of fire spreading.
   * @throws IllegalArgumentException if the probability is out of bounds.
   */
  public void setProbCatch(double percentCatch) {
    double min = getMinParam("probCatch");
    double max = getMaxParam("probCatch");
    checkBounds(percentCatch, min, max);
    probCatch = percentCatch / 100.0;
  }

  /**
   * Sets the probability of spontaneous fire ignition in a tree.
   *
   * @param percentIgnite The probability (0-100) of a tree spontaneously catching fire.
   * @throws IllegalArgumentException if the probability is out of bounds.
   */
  public void setProbIgnite(double percentIgnite) {
    double min = getMinParam("probIgnite");
    double max = getMaxParam("probIgnite");
    checkBounds(percentIgnite, min, max);
    probIgnite = percentIgnite / 100.0;
  }

  /**
   * Sets the probability of a new tree growing in an empty cell.
   *
   * @param percentTree The probability (0-100) of tree growth in an empty space.
   * @throws IllegalArgumentException if the probability is out of bounds.
   */
  public void setProbTree(double percentTree) {
    double min = getMinParam("probTree");
    double max = getMaxParam("probTree");
    checkBounds(percentTree, min, max);
    probTree = percentTree / 100.0;
  }

  /**
   * Retrieves the probability of fire spreading.
   *
   * @return The probability (0-100) of fire spreading.
   */
  public double getProbCatch() {
    return probCatch * 100;
  }

  /**
   * Retrieves the probability of spontaneous tree ignition.
   *
   * @return The probability (0-100) of spontaneous fire ignition.
   */
  public double getProbIgnite() {
    return probIgnite * 100;
  }

  /**
   * Retrieves the probability of tree growth in an empty cell.
   *
   * @return The probability (0-100) of tree growth.
   */
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
    } else if (currentState == FireState.TREE) {
      if (Math.random() < probIgnite) {
        cell.setNextState(FireState.BURNING);
      }
    } else if (currentState == FireState.EMPTY) {
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
