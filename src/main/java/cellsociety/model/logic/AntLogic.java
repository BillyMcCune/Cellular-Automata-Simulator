package cellsociety.model.logic;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.AntState;
import cellsociety.model.data.neighbors.Direction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete implementation of {@link Logic} for the Foraging Ants simulation. This class
 * encapsulates the logic for ant behavior including movement, pheromone evaporation, and decision
 * making based on environmental stimuli. It manages ant states within each cell on the grid.
 *
 * <p>The simulation parameters (e.g., maxAnts, evaporationRate, pheromone sensitivities, etc.) are
 * set
 * via a {@link ParameterRecord} provided at construction time. The logic updates the state of the
 * grid by processing each cell's ants, moving them according to pheromone gradients, and handling
 * state transitions.
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
   * Immutable record representing an individual ant in the simulation. Each ant has an orientation
   * indicating its current movement direction and a flag indicating whether it is currently
   * carrying food.
   *
   * @param orientation the ant's current orientation as a {@link Direction}
   * @param hasFood     {@code true} if the ant is carrying food, {@code false} otherwise
   */
  public record AntInfo(Direction orientation, boolean hasFood) {

  }

  /**
   * Constructs an {@code AntLogic} instance for the foraging ant simulation.
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

  /**
   * Sets the maximum number of ants allowed in a cell.
   *
   * @param maxAnts the maximum ants value
   * @throws IllegalArgumentException if the value is out of allowed bounds
   */
  public void setMaxAnts(double maxAnts) throws IllegalArgumentException {
    double min = getMinParam("maxAnts");
    double max = getMaxParam("maxAnts");
    checkBounds(maxAnts, min, max);
    this.maxAnts = maxAnts;
  }

  /**
   * Returns the maximum number of ants allowed in a cell.
   *
   * @return the maximum ants value
   */
  public double getMaxAnts() {
    return maxAnts;
  }

  /**
   * Sets the evaporation rate for pheromones.
   *
   * @param evaporationRate the evaporation rate (in percent) to set
   * @throws IllegalArgumentException if the value is out of allowed bounds
   */
  public void setEvaporationRate(double evaporationRate) throws IllegalArgumentException {
    double min = getMinParam("evaporationRate");
    double max = getMaxParam("evaporationRate");
    checkBounds(evaporationRate, min, max);
    this.evaporationRate = evaporationRate / 100;
  }

  /**
   * Returns the evaporation rate for pheromones (in percent).
   *
   * @return the evaporation rate as a percentage
   */
  public double getEvaporationRate() {
    return evaporationRate * 100;
  }

  /**
   * Sets the maximum home pheromone level.
   *
   * @param maxHomePheromone the maximum home pheromone value to set
   * @throws IllegalArgumentException if the value is out of allowed bounds
   */
  public void setMaxHomePheromone(double maxHomePheromone) throws IllegalArgumentException {
    double min = getMinParam("maxHomePheromone");
    double max = getMaxParam("maxHomePheromone");
    checkBounds(maxHomePheromone, min, max);
    this.maxHomePheromone = maxHomePheromone;
  }

  /**
   * Returns the maximum home pheromone level.
   *
   * @return the maximum home pheromone value
   */
  public double getMaxHomePheromone() {
    return maxHomePheromone;
  }

  /**
   * Sets the maximum food pheromone level.
   *
   * @param maxFoodPheromone the maximum food pheromone value to set
   * @throws IllegalArgumentException if the value is out of allowed bounds
   */
  public void setMaxFoodPheromone(double maxFoodPheromone) throws IllegalArgumentException {
    double min = getMinParam("maxFoodPheromone");
    double max = getMaxParam("maxFoodPheromone");
    checkBounds(maxFoodPheromone, min, max);
    this.maxFoodPheromone = maxFoodPheromone;
  }

  /**
   * Returns the maximum food pheromone level.
   *
   * @return the maximum food pheromone value
   */
  public double getMaxFoodPheromone() {
    return maxFoodPheromone;
  }

  /**
   * Sets the base pheromone weight, which influences how pheromone levels affect ant movement.
   *
   * @param basePheromoneWeight the base pheromone weight to set
   * @throws IllegalArgumentException if the value is out of allowed bounds
   */
  public void setBasePheromoneWeight(double basePheromoneWeight) throws IllegalArgumentException {
    double min = getMinParam("basePheromoneWeight");
    double max = getMaxParam("basePheromoneWeight");
    checkBounds(basePheromoneWeight, min, max);
    this.basePheromoneWeight = basePheromoneWeight;
  }

  /**
   * Returns the base pheromone weight.
   *
   * @return the base pheromone weight value
   */
  public double getBasePheromoneWeight() {
    return basePheromoneWeight;
  }

  /**
   * Sets the pheromone sensitivity which determines how strongly ants respond to pheromone
   * gradients.
   *
   * @param pheromoneSensitivity the pheromone sensitivity to set
   * @throws IllegalArgumentException if the value is out of allowed bounds
   */
  public void setPheromoneSensitivity(double pheromoneSensitivity) throws IllegalArgumentException {
    double min = getMinParam("pheromoneSensitivity");
    double max = getMaxParam("pheromoneSensitivity");
    checkBounds(pheromoneSensitivity, min, max);
    this.pheromoneSensitivity = pheromoneSensitivity;
  }

  /**
   * Returns the pheromone sensitivity.
   *
   * @return the pheromone sensitivity value
   */
  public double getPheromoneSensitivity() {
    return pheromoneSensitivity;
  }

  /**
   * Sets the pheromone diffusion decay, which controls how quickly pheromone signals weaken over
   * distance.
   *
   * @param pheromoneDiffusionDecay the pheromone diffusion decay value to set
   * @throws IllegalArgumentException if the value is out of allowed bounds
   */
  public void setPheromoneDiffusionDecay(double pheromoneDiffusionDecay)
      throws IllegalArgumentException {
    double min = getMinParam("pheromoneDiffusionDecay");
    double max = getMaxParam("pheromoneDiffusionDecay");
    checkBounds(pheromoneDiffusionDecay, min, max);
    this.pheromoneDiffusionDecay = pheromoneDiffusionDecay;
  }

  /**
   * Returns the pheromone diffusion decay.
   *
   * @return the pheromone diffusion decay value
   */
  public double getPheromoneDiffusionDecay() {
    return pheromoneDiffusionDecay;
  }

  /**
   * Initializes the ants on the grid based on the properties of each cell. This method sets up the
   * initial ant distribution by examining the cell properties "searchingEntities" and
   * "returningEntities", and assigns initial directions based on pheromone levels.
   */
  private void initializeAnts() {
    for (int r = 0; r < grid.getNumRows(); r++) {
      for (int c = 0; c < grid.getNumCols(); c++) {
        Cell<AntState> cell = grid.getCell(r, c);
        double searchingEntities = cell.getProperty("searchingEntities");
        double returningEntities = cell.getProperty("returningEntities");
        List<Direction> validDirections = getValidDirections(
            grid.getDirections(cell), cell);
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

  /**
   * Updates the simulation by processing ant movements, pheromone evaporation, and state updates.
   * This method iterates over each cell to update its ants and pheromone levels, and then updates
   * the grid to reflect the new states.
   */
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

  /**
   * Updates the state of a single cell by processing the ants within it. If the cell has no ants,
   * it is removed from the internal ant mapping.
   *
   * @param cell the cell to update
   */
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
    List<Direction> possibleDirections = getPossibleDirections(ant.orientation(), cell);
    String pheromoneType = ant.hasFood() ? "homePheromone" : "foodPheromone";
    List<Direction> validDirections = getValidDirections(possibleDirections, cell);
    Direction chosen = getPheromoneWeightedDirection(validDirections, cell, pheromoneType);
    if (chosen != null) {
      return chosen;
    }
    validDirections = getValidDirections(grid.getDirections(cell), cell);
    chosen = getPheromoneWeightedDirection(validDirections, cell, pheromoneType);
    return (chosen != null) ? chosen : new Direction(0, 0);
  }

  private List<Direction> getPossibleDirections(Direction orientation, Cell<AntState> cell) {
    List<Direction> allDirections = new ArrayList<>(grid.getDirections(cell));
    if (orientation.toString().equals(new Direction(0, 0).toString())) {
      return allDirections;
    }
    int dx = orientation.dx();
    int dy = orientation.dy();
    int[][] candidates = {{dy - 1, dx}, {dy + 1, dx}, {dy, dx - 1}, {dy, dx + 1}};
    List<Direction> result = new ArrayList<>();
    for (int[] candidate : candidates) {
      int x = candidate[0];
      int y = candidate[1];
      if (x == 0 && y == 0) {
        continue;
      }
      Direction direction = new Direction(x, y);
      if (allDirections.contains(direction)) {
        result.add(direction);
      }
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
    if (neighbor == null) {
      return;
    }

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
