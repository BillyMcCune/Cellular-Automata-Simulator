package cellsociety.model.logic;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.neighbors.AntNeighborCalculator;
import cellsociety.model.data.states.AntState;
import cellsociety.model.data.neighbors.Direction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete implementation of {@link Logic} for the Foraging Ants simulation.
 *
 * @author Jacob You
 */
public class AntLogic extends Logic<AntState> {

  private double maxAnts;
  private double evaporationRate;
  private double maxHomePheromone;
  private double maxFoodPheromone;
  private double basePheromoneWeight;
  private double pheromoneSensitivity;
  private double pheromoneDiffusionDecay;
  private final Map<Cell<AntState>, List<AntInfo>> cellAntsMap = new HashMap<>();


  /**
   * Record representing an individual ant in the simulation.
   *
   * @param orientation the ant's current orientation as a Direction
   * @param hasFood     true if the ant is carrying food, false otherwise
   */
  public record AntInfo(Direction orientation, boolean hasFood) {

  }

  /**
   * Constructs an AntLogic instance for the foraging ant simulation.
   *
   * @param grid       the grid representing the simulation state
   * @param parameters the parameter record containing simulation-specific configurations
   * @throws IllegalArgumentException if required parameters are missing or out of bounds
   */
  public AntLogic(Grid<AntState> grid, ParameterRecord parameters) throws IllegalArgumentException {
    super(grid, parameters);
    setMaxAnts(getDoubleParamOrFallback("maxAnts"));
    setEvaporationRate(getDoubleParamOrFallback("evaporationRate"));
    setMaxHomePheromone(getDoubleParamOrFallback("maxHomePheromone"));
    setMaxFoodPheromone(getDoubleParamOrFallback("maxFoodPheromone"));
    setBasePheromoneWeight(getDoubleParamOrFallback("basePheromoneWeight"));
    setPheromoneSensitivity(getDoubleParamOrFallback("pheromoneSensitivity"));
    setPheromoneDiffusionDecay(getDoubleParamOrFallback("pheromoneDiffusionDecay"));
    initializeAnts();
  }

  public void setMaxAnts(double maxAnts) throws IllegalArgumentException {
    double min = getMinParam("maxAnts");
    double max = getMaxParam("maxAnts");
    checkBounds(maxAnts, min, max);
    this.maxAnts = maxAnts;
  }

  public double getMaxAnts() {
    return maxAnts;
  }

  public void setEvaporationRate(double evaporationRate) throws IllegalArgumentException {
    double min = getMinParam("evaporationRate");
    double max = getMaxParam("evaporationRate");
    checkBounds(evaporationRate, min, max);
    this.evaporationRate = evaporationRate / 100;
  }

  public double getEvaporationRate() {
    return evaporationRate * 100;
  }

  public void setMaxHomePheromone(double maxHomePheromone) throws IllegalArgumentException {
    double min = getMinParam("maxHomePheromone");
    double max = getMaxParam("maxHomePheromone");
    checkBounds(maxHomePheromone, min, max);
    this.maxHomePheromone = maxHomePheromone;
  }

  public double getMaxHomePheromone() {
    return maxHomePheromone;
  }

  public void setMaxFoodPheromone(double maxFoodPheromone) throws IllegalArgumentException {
    double min = getMinParam("maxFoodPheromone");
    double max = getMaxParam("maxFoodPheromone");
    checkBounds(maxFoodPheromone, min, max);
    this.maxFoodPheromone = maxFoodPheromone;
  }

  public double getMaxFoodPheromone() {
    return maxFoodPheromone;
  }

  public void setBasePheromoneWeight(double basePheromoneWeight) throws IllegalArgumentException {
    double min = getMinParam("basePheromoneWeight");
    double max = getMaxParam("basePheromoneWeight");
    checkBounds(basePheromoneWeight, min, max);
    this.basePheromoneWeight = basePheromoneWeight;
  }

  public double getBasePheromoneWeight() {
    return basePheromoneWeight;
  }

  public void setPheromoneSensitivity(double pheromoneSensitivity) throws IllegalArgumentException {
    double min = getMinParam("pheromoneSensitivity");
    double max = getMaxParam("pheromoneSensitivity");
    checkBounds(pheromoneSensitivity, min, max);
    this.pheromoneSensitivity = pheromoneSensitivity;
  }

  public double getPheromoneSensitivity() {
    return pheromoneSensitivity;
  }

  public void setPheromoneDiffusionDecay(double pheromoneDiffusionDecay)
      throws IllegalArgumentException {
    double min = getMinParam("pheromoneDiffusionDecay");
    double max = getMaxParam("pheromoneDiffusionDecay");
    checkBounds(pheromoneDiffusionDecay, min, max);
    this.pheromoneDiffusionDecay = pheromoneDiffusionDecay;
  }

  public double getPheromoneDiffusionDecay() {
    return pheromoneDiffusionDecay;
  }

  private void initializeAnts() {
    for (int r = 0; r < grid.getNumRows(); r++) {
      for (int c = 0; c < grid.getNumCols(); c++) {
        Cell<AntState> cell = grid.getCell(r, c);
        double searchingEntities = cell.getProperty("searchingEntities");
        double returningEntities = cell.getProperty("returningEntities");
        List<Direction> validDirections = getValidDirections(
            grid.getNeighborCalculator().getDirections(), cell);
        Direction chosenSearching = getPheromoneWeightedDirection(validDirections, cell,
            "foodPheromone");
        Direction chosenReturning = getPheromoneWeightedDirection(validDirections, cell,
            "homePheromone");
        if (chosenSearching == null) {
          chosenSearching = new Direction(0, 0);
        }
        if (chosenReturning == null) {
          chosenReturning = new Direction(0, 0);
        }
        for (int i = 0; i < searchingEntities; i++) {
          cellAntsMap.computeIfAbsent(cell, k -> new ArrayList<>())
              .add(new AntInfo(chosenSearching, false));
        }
        for (int i = 0; i < returningEntities; i++) {
          cellAntsMap.computeIfAbsent(cell, k -> new ArrayList<>())
              .add(new AntInfo(chosenReturning, true));
        }
      }
    }
  }

  @Override
  public void update() {
    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();
    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        Cell<AntState> cell = grid.getCell(row, col);
        if (cellAntsMap.containsKey(cell)) {
          updateSingleCell(cell);
        }
      }
    }
    List<Cell<AntState>> cellsToProcess = new ArrayList<>(cellAntsMap.keySet());
    for (Cell<AntState> cell : cellsToProcess) {
      List<AntInfo> antsInCell = new ArrayList<>(cellAntsMap.get(cell));
      for (AntInfo ant : antsInCell) {
        moveAnt(cell, ant);
      }
    }
    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        Cell<AntState> cell = grid.getCell(row, col);
        evaporatePheromones(cell);
      }
    }
    grid.updateGrid();
  }

  @Override
  public void updateSingleCell(Cell<AntState> cell) {
    List<AntInfo> ants = cellAntsMap.get(cell);
    if (ants == null) {
      return;
    }
    if (ants.isEmpty()) {
      cellAntsMap.remove(cell);
      return;
    }

    List<AntInfo> newAnts = new ArrayList<>();
    for (AntInfo ant : ants) {
      AntInfo newAnt = ant.hasFood() ? antReturnToNest(cell, ant) : antFindFoodSource(cell, ant);
      newAnts.add(newAnt);
    }
    cellAntsMap.put(cell, newAnts);
  }

  // STUFF TO DETERMINE WHETHER THE ANT IS COMING OR GOING

  private AntInfo antReturnToNest(Cell<AntState> cell, AntInfo ant) {
    if (cell.getCurrentState() == AntState.NEST) {
      ant = new AntInfo(new Direction(0, 0), false);
      cell.setProperty("searchingEntities", cell.getProperty("searchingEntities") + 1);
      cell.setProperty("returningEntities", cell.getProperty("returningEntities") - 1);
      return antFindFoodSource(cell, ant);
    }
    Direction direction = determineDirection(cell, ant);
    return new AntInfo(direction, true);
  }

  private AntInfo antFindFoodSource(Cell<AntState> cell, AntInfo ant) {
    if (cell.getCurrentState() == AntState.FOOD) {
      cell.setNextState(AntState.EMPTY);
      ant = new AntInfo(new Direction(0, 0), true);
      cell.setProperty("searchingEntities", cell.getProperty("searchingEntities") - 1);
      cell.setProperty("returningEntities", cell.getProperty("returningEntities") + 1);
      return antReturnToNest(cell, ant);
    }
    Direction direction = determineDirection(cell, ant);
    return new AntInfo(direction, false);
  }

  // STUFF TO FIND THE DIRECTION TO GO TO

  private Direction determineDirection(Cell<AntState> cell, AntInfo ant) {
    List<Direction> possibleDirections = getPossibleDirections(ant.orientation());
    String pheromoneType = ant.hasFood() ? "homePheromone" : "foodPheromone";
    List<Direction> validDirections = getValidDirections(possibleDirections, cell);
    Direction chosen = getPheromoneWeightedDirection(validDirections, cell, pheromoneType);
    if (chosen != null) {
      return chosen;
    }
    validDirections = getValidDirections(grid.getNeighborCalculator().getDirections(), cell);
    chosen = getPheromoneWeightedDirection(validDirections, cell, pheromoneType);
    return (chosen != null) ? chosen : new Direction(0, 0);
  }

  private List<Direction> getPossibleDirections(Direction orientation) {
    if (orientation.toString().equals(new Direction(0, 0).toString())) {
      return grid.getNeighborCalculator().getDirections();
    }
    int dx = orientation.dx();
    int dy = orientation.dy();
    int[][] candidates = {{dy - 1, dx}, {dy + 1, dx}, {dy, dx - 1}, {dy, dx + 1}};
    List<Direction> result = new ArrayList<>();
    for (int[] candidate : candidates) {
      int x = candidate[0];
      int y = candidate[1];
      if (Math.abs(x) > 1 || Math.abs(y) > 1 || (x == 0 && y == 0)) {
        continue;
      }
      result.add(new Direction(x, y));
    }
    result.add(orientation);
    return result;
  }

  private List<Direction> getValidDirections(List<Direction> candidateDirs, Cell<AntState> cell) {
    List<Direction> validDirections = new ArrayList<>();
    for (Direction direction : candidateDirs) {
      Cell<AntState> neighbor = getCellInDirection(direction, cell);
      if ((neighbor == null || neighbor.getCurrentState() == AntState.BLOCKED)
          || neighbor.getProperty("searchingEntities") + neighbor.getProperty("returningEntities")
          >= maxAnts) {
        continue;
      }
      validDirections.add(direction);
    }
    return validDirections;
  }

  private Direction getPheromoneWeightedDirection(List<Direction> validDirections,
      Cell<AntState> cell, String pheromoneType) {
    if (validDirections == null || validDirections.isEmpty()) {
      return null;
    }
    double totalWeight = 0;
    List<Double> weights = new ArrayList<>();
    for (Direction d : validDirections) {
      Cell<AntState> neighbor = getCellInDirection(d, cell);
      if (neighbor == null) {
        continue;
      }
      double pheromoneLevel = neighbor.getProperty(pheromoneType);
      double weight = Math.pow(basePheromoneWeight + pheromoneLevel, pheromoneSensitivity);
      weights.add(weight);
      totalWeight += weight;
    }
    double selection = Math.random() * totalWeight;
    double total = 0;
    for (int i = 0; i < validDirections.size(); i++) {
      total += weights.get(i);
      if (selection <= total) {
        return validDirections.get(i);
      }
    }
    return validDirections.get(validDirections.size() - 1);
  }

  private Cell<AntState> getCellInDirection(Direction direction, Cell<AntState> cell) {
    if (cell.getNeighbors().containsKey(direction)) {
      return cell.getNeighbors().get(direction);
    }
    return null;
  }

  // STUFF TO MOVE THE ANT

  private void moveAnt(Cell<AntState> cell, AntInfo ant) {
    Cell<AntState> neighbor = getCellInDirection(ant.orientation(), cell);
    assert neighbor != null; // The validDirections should confirm this

    dropPheromone(cell, ant.hasFood() ? "foodPheromone" : "homePheromone");
    if (ant.hasFood()) {
      neighbor.setProperty("returningEntities", neighbor.getProperty("returningEntities") + 1);
      cell.setProperty("returningEntities", cell.getProperty("returningEntities") - 1);
    } else {
      neighbor.setProperty("searchingEntities", neighbor.getProperty("searchingEntities") + 1);
      cell.setProperty("searchingEntities", cell.getProperty("searchingEntities") - 1);
    }
    cellAntsMap.get(cell).remove(ant);
    cellAntsMap.computeIfAbsent(neighbor, k -> new ArrayList<>()).add(ant);
  }

  // STUFF TO DROP PHEROMONES

  private void dropPheromone(Cell<AntState> cell, String pheromoneType) {
    double maxNeighbor = 0;
    if (cell.getCurrentState() == AntState.NEST && pheromoneType.equals("homePheromone")) {
      cell.setProperty(pheromoneType, maxHomePheromone);
    } else if (cell.getCurrentState() == AntState.FOOD && pheromoneType.equals("foodPheromone")) {
      cell.setProperty(pheromoneType, maxFoodPheromone);
    } else {
      for (Cell<AntState> neighbor : cell.getNeighbors().values()) {
        double neighborLevel = neighbor.getProperty(pheromoneType);
        if (neighborLevel > maxNeighbor) {
          maxNeighbor = neighborLevel;
        }
      }
      double desired = maxNeighbor - pheromoneDiffusionDecay;
      if (desired > 0) {
        cell.setProperty(pheromoneType, desired);
      }
    }
  }

  private void evaporatePheromones(Cell<AntState> cell) {
    double currentHome = cell.getProperty("homePheromone");
    double currentFood = cell.getProperty("foodPheromone");
    cell.setProperty("homePheromone", Math.max(0, currentHome * (1 - evaporationRate)));
    cell.setProperty("foodPheromone", Math.max(0, currentFood * (1 - evaporationRate)));
  }
}