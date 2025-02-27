package cellsociety.model.logic;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.data.states.SugarState;
import cellsociety.model.data.neighbors.SugarNeighborCalculator;
import cellsociety.model.data.neighbors.Direction;
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
  private final Map<Cell<SugarState>, Double> agentSugarMap;
  private final List<Cell<SugarState>> agentCells;
  private final List<Cell<SugarState>> patchCells;
  private Map<Cell<SugarState>, Map<Direction, Cell<SugarState>>> cachedNeighbors;
  private final SugarNeighborCalculator<SugarState> neighborCalc;

  /**
   * Constructs a SugarLogic instance.
   * Agents move to the patch with the highest sugar within vision and consume sugar.
   * Sugar on patches regrows every sugarGrowBackInterval ticks.
   *
   * @param grid the grid representing the simulation state
   * @param parameters the parameter record containing vision, sugarMetabolism, sugarGrowBackRate, and sugarGrowBackInterval
   */
  public SugarLogic(Grid<SugarState> grid, ParameterRecord parameters) {
    super(grid, parameters);
    neighborCalc = new SugarNeighborCalculator<>();
    setVision(getDoubleParamOrFallback("vision"));
    setSugarMetabolism(getDoubleParamOrFallback("sugarMetabolism"));
    setSugarGrowBackRate(getDoubleParamOrFallback("sugarGrowBackRate"));
    setSugarGrowBackInterval((int) getDoubleParamOrFallback("sugarGrowBackInterval"));
    tick = 0;
    agentSugarMap = new HashMap<>();
    agentCells = new ArrayList<>();
    patchCells = new ArrayList<>();
    cachedNeighbors = new HashMap<>();
    initializeCells();
    recalcNeighbors();
  }

  /**
   * Updates the simulation by growing sugar (if applicable) and updating agent cells.
   * Then, the grid is updated to apply all next states.
   *
   * @return void
   */
  @Override
  public void update() {
    tick++;
    if (tick % sugarGrowBackInterval == 0) {
      growSugar();
    }
    List<Cell<SugarState>> currentAgents = new ArrayList<>(agentCells);
    for (Cell<SugarState> cell : currentAgents) {
      updateSingleCell(cell);
    }
    grid.updateGrid();
  }

  /**
   * Updates a single agent cell by moving it to the best neighboring patch if available;
   * otherwise, the agent loses sugar due to metabolism.
   *
   * @param cell the cell containing the agent to update
   * @return void
   */
  @Override
  protected void updateSingleCell(Cell<SugarState> cell) {
    if (cell.getCurrentState() != SugarState.AGENT) {
      return;
    }
    Map<Direction, Cell<SugarState>> neighbors = cachedNeighbors.get(cell);
    Cell<SugarState> target = findBestPatch(neighbors);
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

  public double getVision() {
    return vision;
  }

  public void setVision(double vision) {
    double min = getMinParam("vision");
    double max = getMaxParam("vision");
    checkBounds(vision, min, max);
    this.vision = vision;
    recalcNeighbors();
  }

  public double getSugarMetabolism() {
    return sugarMetabolism;
  }

  public void setSugarMetabolism(double sugarMetabolism) {
    double min = getMinParam("sugarMetabolism");
    double max = getMaxParam("sugarMetabolism");
    checkBounds(sugarMetabolism, min, max);
    this.sugarMetabolism = sugarMetabolism;
  }

  public double getSugarGrowBackRate() {
    return sugarGrowBackRate;
  }

  public void setSugarGrowBackRate(double sugarGrowBackRate) {
    double min = getMinParam("sugarGrowBackRate");
    double max = getMaxParam("sugarGrowBackRate");
    checkBounds(sugarGrowBackRate, min, max);
    this.sugarGrowBackRate = sugarGrowBackRate;
  }

  public int getSugarGrowBackInterval() {
    return sugarGrowBackInterval;
  }

  public void setSugarGrowBackInterval(int sugarGrowBackInterval) {
    double min = getMinParam("sugarGrowBackInterval");
    double max = getMaxParam("sugarGrowBackInterval");
    checkBounds(sugarGrowBackInterval, min, max);
    this.sugarGrowBackInterval = sugarGrowBackInterval;
  }

  private void recalcNeighbors() {
    cachedNeighbors.clear();
    for (int r = 0; r < grid.getNumRows(); r++) {
      for (int c = 0; c < grid.getNumCols(); c++) {
        Cell<SugarState> cell = grid.getCell(r, c);
        SugarNeighborCalculator<SugarState> calc = (SugarNeighborCalculator<SugarState>) grid.getNeighborCalculator();
        cachedNeighbors.put(cell, calc.getNeighbors(grid, r, c, (int) vision));
      }
    }
  }

  private void initializeCells() {
    for (int r = 0; r < grid.getNumRows(); r++) {
      for (int c = 0; c < grid.getNumCols(); c++) {
        Cell<SugarState> cell = grid.getCell(r, c);
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

  private void growSugar() {
    for (Cell<SugarState> patch : patchCells) {
      double currentSugar = patch.getProperty("sugarAmount");
      double maxSugar = patch.getProperty("maxSugar");
      double newSugar = Math.min(maxSugar, currentSugar + sugarGrowBackRate);
      patch.setProperty("sugarAmount", newSugar);
    }
  }

  private Cell<SugarState> findBestPatch(Map<Direction, Cell<SugarState>> neighbors) {
    double bestSugar = -1;
    int bestDistance = Integer.MAX_VALUE;
    Cell<SugarState> bestPatch = null;
    for (Map.Entry<Direction, Cell<SugarState>> entry : neighbors.entrySet()) {
      Direction dir = entry.getKey();
      Cell<SugarState> candidate = entry.getValue();
      if (candidate.getCurrentState() == SugarState.EMPTY) {
        double sugar = candidate.getProperty("sugarAmount");
        int distance = Math.abs(dir.dx() + dir.dy());
        if (sugar > bestSugar || (sugar == bestSugar && distance < bestDistance)) {
          bestSugar = sugar;
          bestDistance = distance;
          bestPatch = candidate;
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
