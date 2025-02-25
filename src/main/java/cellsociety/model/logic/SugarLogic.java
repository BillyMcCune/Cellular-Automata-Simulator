package cellsociety.model.logic;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.SugarState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SugarLogic extends Logic<SugarState> {

  private double vision;
  private double sugarMetabolism;
  private double sugarGrowBackRate;
  private int sugarGrowBackInterval;
  private int tick;
  private Map<Cell<SugarState>, Double> agentSugarMap;
  private List<Cell<SugarState>> agentCells;
  private List<Cell<SugarState>> patchCells;
  private Map<Cell<SugarState>, int[]> cellPositions;

  public SugarLogic(Grid<SugarState> grid, ParameterRecord parameters) {
    super(grid, parameters);
    vision = getDoubleParamOrFallback("vision");
    sugarMetabolism = getDoubleParamOrFallback("sugarMetabolism");
    sugarGrowBackRate = getDoubleParamOrFallback("sugarGrowBackRate");
    sugarGrowBackInterval = (int) getDoubleParamOrFallback("sugarGrowBackInterval");
    tick = 0;
    agentSugarMap = new HashMap<>();
    agentCells = new ArrayList<>();
    patchCells = new ArrayList<>();
    cellPositions = new HashMap<>();
    initializeCells();
  }

  private void initializeCells() {
    for (int r = 0; r < grid.getNumRows(); r++) {
      for (int c = 0; c < grid.getNumCols(); c++) {
        Cell<SugarState> cell = grid.getCell(r, c);
        cellPositions.put(cell, new int[]{r, c});
        if (cell.getProperty("sugarAmount") == 0.0 && cell.getProperty("maxSugar") == 0.0) {
          cell.setProperty("sugarAmount", 5.0);
          cell.setProperty("maxSugar", 10.0);
        }
        if (cell.getCurrentState() == SugarState.AGENT) {
          agentCells.add(cell);
          double initialAgentSugar = cell.getProperty("agentSugar");
          if (initialAgentSugar == 0.0) {
            initialAgentSugar = 10.0;
            cell.setProperty("agentSugar", initialAgentSugar);
          }
          agentSugarMap.put(cell, initialAgentSugar);
        } else {
          patchCells.add(cell);
        }
      }
    }
  }

  @Override
  public void update() {
    tick++;
    if (tick % sugarGrowBackInterval == 0) {
      growSugar();
    }
    List<Cell<SugarState>> currentAgentCells = new ArrayList<>(agentCells);
    for (Cell<SugarState> cell : currentAgentCells) {
      updateSingleCell(cell);
    }
    grid.updateGrid();
  }

  @Override
  protected void updateSingleCell(Cell<SugarState> cell) {
    if (cell.getCurrentState() != SugarState.AGENT) {
      return;
    }
    Cell<SugarState> target = findBestPatch(cell);
    if (target != null && target != cell) {
      moveAgent(cell, target);
    } else {
      double agentSugar = agentSugarMap.get(cell);
      agentSugar -= sugarMetabolism;
      if (agentSugar <= 0) {
        cell.setNextState(SugarState.EMPTY);
        agentSugarMap.remove(cell);
        agentCells.remove(cell);
      } else {
        agentSugarMap.put(cell, agentSugar);
      }
    }
  }

  private void growSugar() {
    for (Cell<SugarState> patch : patchCells) {
      double currentSugar = patch.getProperty("sugarAmount");
      double maxSugar = patch.getProperty("maxSugar");
      double newSugar = Math.min(maxSugar, currentSugar + sugarGrowBackRate);
      patch.setProperty("sugarAmount", newSugar);
    }
  }

  private List<Cell<SugarState>> getOrthogonalCellsInVision(Cell<SugarState> cell) {
    List<Cell<SugarState>> visibleCells = new ArrayList<>();
    int[] pos = cellPositions.get(cell);
    int row = pos[0];
    int col = pos[1];
    int visionInt = (int) vision;
    for (int d = 1; d <= visionInt; d++) {
      visibleCells.add(grid.getCell((row - d + grid.getNumRows()) % grid.getNumRows(), col));
      visibleCells.add(grid.getCell(row, (col + d) % grid.getNumCols()));
      visibleCells.add(grid.getCell((row + d) % grid.getNumRows(), col));
      visibleCells.add(grid.getCell(row, (col - d + grid.getNumCols()) % grid.getNumCols()));
    }
    return visibleCells;
  }

  private Cell<SugarState> findBestPatch(Cell<SugarState> agentCell) {
    List<Cell<SugarState>> candidates = getOrthogonalCellsInVision(agentCell);
    double bestSugar = -1;
    int bestDistance = Integer.MAX_VALUE;
    Cell<SugarState> bestPatch = null;
    int[] pos = cellPositions.get(agentCell);
    int row = pos[0];
    int col = pos[1];
    int visionInt = (int) vision;
    for (int d = 1; d <= visionInt; d++) {
      Cell<SugarState> up = grid.getCell((row - d + grid.getNumRows()) % grid.getNumRows(), col);
      if (up.getCurrentState() == SugarState.EMPTY) {
        double sugar = up.getProperty("sugarAmount");
        if (sugar > bestSugar || (sugar == bestSugar && d < bestDistance)) {
          bestSugar = sugar;
          bestDistance = d;
          bestPatch = up;
        }
      }
      Cell<SugarState> right = grid.getCell(row, (col + d) % grid.getNumCols());
      if (right.getCurrentState() == SugarState.EMPTY) {
        double sugar = right.getProperty("sugarAmount");
        if (sugar > bestSugar || (sugar == bestSugar && d < bestDistance)) {
          bestSugar = sugar;
          bestDistance = d;
          bestPatch = right;
        }
      }
      Cell<SugarState> down = grid.getCell((row + d) % grid.getNumRows(), col);
      if (down.getCurrentState() == SugarState.EMPTY) {
        double sugar = down.getProperty("sugarAmount");
        if (sugar > bestSugar || (sugar == bestSugar && d < bestDistance)) {
          bestSugar = sugar;
          bestDistance = d;
          bestPatch = down;
        }
      }
      Cell<SugarState> left = grid.getCell(row, (col - d + grid.getNumCols()) % grid.getNumCols());
      if (left.getCurrentState() == SugarState.EMPTY) {
        double sugar = left.getProperty("sugarAmount");
        if (sugar > bestSugar || (sugar == bestSugar && d < bestDistance)) {
          bestSugar = sugar;
          bestDistance = d;
          bestPatch = left;
        }
      }
    }
    return bestPatch;
  }

  private void moveAgent(Cell<SugarState> from, Cell<SugarState> to) {
    double agentSugar = agentSugarMap.get(from);
    double patchSugar = to.getProperty("sugarAmount");
    agentSugar += patchSugar;
    to.setProperty("sugarAmount", 0.0);
    agentSugar -= sugarMetabolism;
    if (agentSugar <= 0) {
      from.setNextState(SugarState.EMPTY);
      to.setNextState(SugarState.EMPTY);
      agentSugarMap.remove(from);
      agentCells.remove(from);
    } else {
      from.setNextState(SugarState.EMPTY);
      to.setNextState(SugarState.AGENT);
      agentSugarMap.remove(from);
      agentSugarMap.put(to, agentSugar);
      agentCells.remove(from);
      agentCells.add(to);
    }
  }
}
