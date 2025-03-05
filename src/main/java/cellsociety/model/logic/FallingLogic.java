package cellsociety.model.logic;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.states.FallingState;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of {@link Logic} for the Falling Sand simulation. Each update tick
 * processes one random valid cell (either sand or water). Sand falling onto water will swap the
 * two, and sand and water movements are handled in separate methods.
 *
 * @author Jacob You
 */
public class FallingLogic extends Logic<FallingState> {

  private final List<Cell<FallingState>> sandCells = new ArrayList<>();
  private final List<Cell<FallingState>> waterCells = new ArrayList<>();

  /**
   * Constructs a {@code FireLogic} instance with the specified grid and parameters.
   *
   * @param grid       The grid representing the simulation state.
   * @param parameters the parameter record containing simulation-specific configurations
   */
  public FallingLogic(Grid<FallingState> grid, ParameterRecord parameters) {
    super(grid, parameters);
    initializeParticles();
  }

  private void initializeParticles() {
    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();
    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        Cell<FallingState> cell = grid.getCell(row, col);
        if (cell.getCurrentState() == FallingState.SAND) {
          sandCells.add(cell);
        } else if (cell.getCurrentState() == FallingState.WATER) {
          waterCells.add(cell);
        }
      }
    }
  }

  /**
   * Each update tick, a single random cell (holding sand or water) is processed. Then the grid is
   * updated to finalize state changes.
   */
  @Override
  public void update() {
    List<Cell<FallingState>> candidates = new ArrayList<>();
    candidates.addAll(sandCells);
    candidates.addAll(waterCells);

    if (!candidates.isEmpty()) {
      int index = (int) (Math.random() * candidates.size());
      Cell<FallingState> chosenCell = candidates.get(index);
      updateSingleCell(chosenCell);
    }
    grid.updateGrid();
  }

  @Override
  protected void updateSingleCell(Cell<FallingState> cell) {
    if (cell.getCurrentState() == FallingState.SAND) {
      moveSand(cell);
    } else if (cell.getCurrentState() == FallingState.WATER) {
      moveWater(cell);
    }
  }

  private void moveSand(Cell<FallingState> cell) {
    Cell<FallingState> below = cell.getNeighbors().get(new Direction(1, 0));
    if (below != null) {
      if (below.getCurrentState() == FallingState.EMPTY) {
        below.setNextState(FallingState.SAND);
        cell.setNextState(FallingState.EMPTY);
        sandCells.remove(cell);
        if (!sandCells.contains(below)) {
          sandCells.add(below);
        }
      } else if (below.getCurrentState() == FallingState.WATER) {
        below.setNextState(FallingState.SAND);
        cell.setNextState(FallingState.WATER);
        sandCells.remove(cell);
        if (!waterCells.contains(cell)) {
          waterCells.add(cell);
        }
        waterCells.remove(below);
        if (!sandCells.contains(below)) {
          sandCells.add(below);
        }
      }
    }
  }

  private void moveWater(Cell<FallingState> cell) {
    // Identify each "downward" neighbor
    Cell<FallingState> below = cell.getNeighbors().get(new Direction(1, 0));
    Cell<FallingState> belowLeft = cell.getNeighbors().get(new Direction(1, -1));
    Cell<FallingState> belowRight = cell.getNeighbors().get(new Direction(1, 1));

    // Collect all empty downward neighbors into a list
    List<Cell<FallingState>> possibleTargets = new ArrayList<>();
    if (below != null && below.getCurrentState() == FallingState.EMPTY) {
      possibleTargets.add(below);
    }
    if (belowLeft != null && belowLeft.getCurrentState() == FallingState.EMPTY) {
      possibleTargets.add(belowLeft);
    }
    if (belowRight != null && belowRight.getCurrentState() == FallingState.EMPTY) {
      possibleTargets.add(belowRight);
    }
    if (!possibleTargets.isEmpty()) {
      Cell<FallingState> target = possibleTargets.get(
          (int) (Math.random() * possibleTargets.size()));
      target.setNextState(FallingState.WATER);
      cell.setNextState(FallingState.EMPTY);
      waterCells.remove(cell);
      if (!waterCells.contains(target)) {
        waterCells.add(target);
      }
    }
  }


  private List<Cell<FallingState>> getEmptyNeighbors(Cell<FallingState> cell) {
    List<Cell<FallingState>> empty = new ArrayList<>();
    for (Cell<FallingState> neighbor : cell.getNeighbors().values()) {
      if (neighbor.getCurrentState() == FallingState.EMPTY) {
        empty.add(neighbor);
      }
    }
    return empty;
  }
}