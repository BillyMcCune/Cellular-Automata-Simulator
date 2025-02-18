package cellsociety.model.logic;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.neighbors.AntNeighborCalculator;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.data.states.AntState;
import cellsociety.model.data.neighbors.Direction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete logic class for the foraging ant simulation. This class tracks individual ant agents
 * using an internal record and updates their positions, orientations, and pheromone deposits. The
 * cell properties maintain aggregate ant counts and pheromone levels, with deposition and
 * evaporation handled according to simulation rules.
 *
 * @author Jacob You
 */
public class AntLogic extends Logic<AntState> {

  private int maxAnts;
  private double evaporationAmount;
  private double maxHomePheromone;
  private double maxFoodPheromone;
  private double baseSelectionProbability;
  private double pheromoneSensitivity;
  private final Map<Cell<AntState>, List<AntInfo>> cellAntsMap = new HashMap<>();


  /**
   * Record representing an individual ant in the simulation.
   *
   * @param row         the row index of the ant's location
   * @param col         the column index of the ant's location
   * @param orientation the ant's current orientation as a Direction
   * @param hasFood     true if the ant is carrying food, false otherwise
   */
  public record AntInfo(int row, int col, Direction orientation, boolean hasFood) {

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
    maxAnts = (int) getDoubleParamOrFallback("maxAnts");
    evaporationAmount = getDoubleParamOrFallback("evaporationAmount");
    maxHomePheromone = getDoubleParamOrFallback("maxHomePheromone");
    maxFoodPheromone = getDoubleParamOrFallback("maxFoodPheromone");
    baseSelectionProbability = getDoubleParamOrFallback("baseSelectionProbability");
    pheromoneSensitivity = getDoubleParamOrFallback("pheromoneSensitivity");
    initializeAnts();
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
    grid.updateGrid();
  }

  @Override
  public void updateSingleCell(Cell<AntState> cell) {
    List<AntInfo> ants = cellAntsMap.get(cell);
    if (ants.isEmpty()) {
      cellAntsMap.remove(cell);
      return;
    }
    List<AntInfo> newAnts = new ArrayList<>();
    for (AntInfo ant : ants) {
      Direction newDirection = determineDirection(cell, ant);
      newAnts.add(new AntInfo(ant.row, ant.col, newDirection, ant.hasFood));
    }
    cellAntsMap.put(cell, newAnts);
  }

  private void initializeAnts() {
    for (int r = 0; r < grid.getNumRows(); r++) {
      for (int c = 0; c < grid.getNumCols(); c++) {
        Cell<AntState> cell = grid.getCell(r, c);
        if (cell.getCurrentState() == AntState.NEST) {
          int antCount = (int) cell.getProperty("antCount");
          for (int i = 0; i < antCount; i++) {
            cellAntsMap.computeIfAbsent(cell, k -> new ArrayList<>())
                .add(new AntInfo(r, c, new Direction(0, 1), false));
          }
        }
      }
    }
  }

  private Direction determineDirection(Cell<AntState> cell, AntInfo ant) {
    List<Direction> possibleDirections = getPossibleDirections(ant.orientation);
    String pheromoneType = ant.hasFood ? "homePheromone" : "foodPheromone";
    Direction chosen = selectLocation(cell, possibleDirections, pheromoneType);
    if (chosen != null) {
      return chosen;
    }
    chosen = selectLocation(cell, AntNeighborCalculator.getDirections(), pheromoneType);
    return (chosen != null) ? chosen : ant.orientation;
  }

  private List<Direction> getPossibleDirections(Direction orientation) {
    int dy = orientation.row();
    int dx = orientation.col();
    int[][] candidates = {{dx - 1, dy}, {dx + 1, dy}, {dx, dy - 1}, {dx, dy + 1}};
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

  private Direction selectLocation(Cell<AntState> cell, List<Direction> candidateDirs,
      String pheromoneType) {
    List<Direction> validDirections = new ArrayList<>();
    for (Direction direction : candidateDirs) {
      Cell<AntState> neighbor = getCellInDirection(direction, cell);
      if (neighbor == null || neighbor.getCurrentState() == AntState.BLOCKED
          || neighbor.getProperty("antCount") >= maxAnts) {
        continue;
      }
      validDirections.add(direction);
    }
    if (validDirections.isEmpty()) {
      return null;
    }
    return getPheromoneWeightedDirection(validDirections, cell, pheromoneType);
  }

  private Direction getPheromoneWeightedDirection(List<Direction> validDirections, Cell<AntState> cell, String pheromoneType) {
    double totalWeight = 0;
    List<Double> weights = new ArrayList<>();
    for (Direction d : validDirections) {
      Cell<AntState> neighbor = getCellInDirection(d, cell);
      if (neighbor == null) {
        continue;
      }
      double pheromoneLevel = neighbor.getProperty(pheromoneType);
      double weight = Math.pow(baseSelectionProbability + pheromoneLevel, pheromoneSensitivity);
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
    return validDirections.getLast();
  }

  private Cell<AntState> getCellInDirection(Direction direction, Cell<AntState> cell) {
    if (cell.getNeighbors().containsKey(direction)) {
      return cell.getNeighbors().get(direction);
    }
    return null;
  }
}