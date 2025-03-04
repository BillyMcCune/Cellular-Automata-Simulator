package cellsociety.model.logic;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.SugarState;
import cellsociety.model.data.neighbors.Direction;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implements SugarScape logic using a SugarNeighborCalculator for orthogonal neighbor retrieval at
 * runtime. Agents look for the patch with the most sugar (within vision) and move there, consuming
 * sugar each tick. Agents store their own sugar in the "agentSugar" property.
 *
 * @author Jacob You
 */
public class SugarLogic extends Logic<SugarState> {

  private double vision;
  private double sugarMetabolism;
  private double sugarGrowBackRate;
  private double sugarGrowBackInterval;
  private int tick;

  private final List<Cell<SugarState>> agentCells;
  private final List<Cell<SugarState>> patchCells;

  /**
   * Constructs a SugarLogic instance. Agents move to the patch with the highest sugar within vision
   * and consume sugar. Patches regrow sugar every sugarGrowBackInterval ticks.
   *
   * @param grid       the grid representing the simulation state
   * @param parameters the parameter record containing vision, sugarMetabolism, sugarGrowBackRate,
   *                   and sugarGrowBackInterval
   * @throws IllegalArgumentException if any parameter is out of bounds
   */
  public SugarLogic(Grid<SugarState> grid, ParameterRecord parameters)
      throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    super(grid, parameters);
    setVision(getDoubleParamOrFallback("vision"));
    setSugarMetabolism(getDoubleParamOrFallback("sugarMetabolism"));
    setSugarGrowBackRate(getDoubleParamOrFallback("sugarGrowBackRate"));
    setSugarGrowBackInterval((int) getDoubleParamOrFallback("sugarGrowBackInterval"));

    tick = 0;
    agentCells = new ArrayList<>();
    patchCells = new ArrayList<>();
    initializeCells();
  }

  /**
   * Updates the simulation each tick by regrowing sugar (if needed) and updating every agent cell.
   * Then, applies next states to the grid.
   */
  @Override
  public void update() {
    tick++;
    if (tick % sugarGrowBackInterval == 0) {
      growSugar();
    }
    List<Cell<SugarState>> currentAgentCells = new ArrayList<>(agentCells);
    for (Cell<SugarState> agentCell : currentAgentCells) {
      updateSingleCell(agentCell);
    }
    grid.updateGrid();
  }

  /**
   * Updates a single cell that contains an agent: the agent tries to find the best patch with the
   * most sugar, moves and consumes sugar, or else remains and loses sugar through metabolism.
   *
   * @param cell the cell containing the agent
   */
  @Override
  protected void updateSingleCell(Cell<SugarState> cell) {
    if (cell.getCurrentState() != SugarState.AGENT) {
      return;
    }
    Cell<SugarState> bestPatch = findBestPatch(cell);
    if (bestPatch != null && bestPatch != cell) {
      moveAgent(cell, bestPatch);
    } else {
      double agentSugar = cell.getProperty("agentSugar");
      agentSugar -= sugarMetabolism;
      if (agentSugar <= 0) {
        cell.setNextState(SugarState.EMPTY);
        agentCells.remove(cell);
        cell.setProperty("agentSugar", 0);
      } else {
        cell.setProperty("agentSugar", agentSugar);
      }
    }
  }

  /**
   * Returns the current vision parameter.
   *
   * @return the vision distance for agent perception
   */
  public double getVision() {
    return vision;
  }

  /**
   * Sets the vision distance and checks bounds.
   *
   * @param vision the new vision distance
   * @throws IllegalArgumentException if vision is out of valid range
   */
  public void setVision(double vision)
      throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    double min = getMinParam("vision");
    double max = getMaxParam("vision");
    checkBounds(vision, min, max);
    this.vision = vision;
    grid.assignAllRaycastNeighbors((int) vision);
  }

  /**
   * Returns the current sugar metabolism value.
   *
   * @return the sugar metabolism (amount lost per tick)
   */
  public double getSugarMetabolism() {
    return sugarMetabolism;
  }

  /**
   * Sets the sugar metabolism value, checking parameter bounds.
   *
   * @param sugarMetabolism the new sugarMetabolism
   * @throws IllegalArgumentException if sugarMetabolism is out of valid range
   */
  public void setSugarMetabolism(double sugarMetabolism) {
    double min = getMinParam("sugarMetabolism");
    double max = getMaxParam("sugarMetabolism");
    checkBounds(sugarMetabolism, min, max);
    this.sugarMetabolism = sugarMetabolism;
  }

  /**
   * Returns the sugar grow-back rate.
   *
   * @return sugarGrowBackRate
   */
  public double getSugarGrowBackRate() {
    return sugarGrowBackRate;
  }

  /**
   * Sets the sugar grow-back rate, checking parameter bounds.
   *
   * @param sugarGrowBackRate new sugarGrowBackRate
   * @throws IllegalArgumentException if out of valid range
   */
  public void setSugarGrowBackRate(double sugarGrowBackRate) {
    double min = getMinParam("sugarGrowBackRate");
    double max = getMaxParam("sugarGrowBackRate");
    checkBounds(sugarGrowBackRate, min, max);
    this.sugarGrowBackRate = sugarGrowBackRate;
  }

  /**
   * Returns the sugar grow-back interval.
   *
   * @return sugarGrowBackInterval
   */
  public double getSugarGrowBackInterval() {
    return sugarGrowBackInterval;
  }

  /**
   * Sets the sugar grow-back interval, checking parameter bounds.
   *
   * @param sugarGrowBackInterval new sugarGrowBackInterval
   * @throws IllegalArgumentException if out of valid range
   */
  public void setSugarGrowBackInterval(double sugarGrowBackInterval) {
    double min = getMinParam("sugarGrowBackInterval");
    double max = getMaxParam("sugarGrowBackInterval");
    checkBounds(sugarGrowBackInterval, min, max);
    this.sugarGrowBackInterval = sugarGrowBackInterval;
  }

  private void initializeCells() {
    int rows = grid.getNumRows();
    int cols = grid.getNumCols();
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        Cell<SugarState> cell = grid.getCell(r, c);
        double maxSugar = cell.getProperty("maxSugar");
        if (maxSugar != 0) {
          patchCells.add(cell);
        }
        if (cell.getCurrentState() == SugarState.AGENT) {
          agentCells.add(cell);
          double currentAgentSugar = cell.getProperty("agentSugar");
          if (currentAgentSugar == 0.0) {
            currentAgentSugar = 10.0;
            cell.setProperty("agentSugar", currentAgentSugar);
          }
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

  private Cell<SugarState> findBestPatch(Cell<SugarState> agentCell) {
    double bestSugar = -1;
    int bestDistance = Integer.MAX_VALUE;
    Cell<SugarState> bestPatch = null;
    for (Map.Entry<Direction, Cell<SugarState>> entry : agentCell.getNeighbors().entrySet()) {
      Direction dir = entry.getKey();
      Cell<SugarState> candidate = entry.getValue();
      if (candidate.getCurrentState() == SugarState.EMPTY) {
        double sugar = candidate.getProperty("sugarAmount");
        int distance = Math.abs(dir.dx()) + Math.abs(dir.dy());
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
    double agentSugar = from.getProperty("agentSugar");
    double patchSugar = to.getProperty("sugarAmount");
    agentSugar += patchSugar;
    to.setProperty("sugarAmount", 0.0);
    agentSugar -= sugarMetabolism;
    if (agentSugar <= 0) {
      to.setNextState(SugarState.EMPTY);
      agentCells.remove(from);
    } else {
      to.setNextState(SugarState.AGENT);
      agentCells.remove(from);
      agentCells.add(to);
      to.setProperty("agentSugar", agentSugar);
    }
    from.setNextState(SugarState.EMPTY);
    from.setProperty("agentSugar", 0.0);
  }
}