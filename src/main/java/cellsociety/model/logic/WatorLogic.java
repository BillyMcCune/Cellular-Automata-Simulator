package cellsociety.model.logic;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.WatorState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the Wa-Tor world simulation logic for {@link Logic}, updating sharks and fish based on
 * energy, breeding times, and moves. All occupant data (energy, time since last breeding) is stored
 * within each Cell's properties map.
 *
 * @author Jacob You
 */
public class WatorLogic extends Logic<WatorState> {

  private static double sharkReproductionTime;
  private static double sharkBaseEnergy;
  private static double fishEnergyGain;
  private static double fishReproductionTime;
  private final List<Cell<WatorState>> sharkCells;
  private final List<Cell<WatorState>> fishCells;
  private static Map<String, Double> baseSharkProperties;
  private static Map<String, Double> baseFishProperties;

  /**
   * Constructs a WatorLogic instance for the given grid.
   *
   * @param grid       The grid on which to run the Wa-Tor simulation.
   * @param parameters The simulation parameters.
   * @throws IllegalArgumentException if any parameter is out of bounds.
   */
  public WatorLogic(Grid<WatorState> grid, ParameterRecord parameters)
      throws IllegalArgumentException {
    super(grid, parameters);
    initializePropertyMaps();

    setSharkBaseEnergy(getDoubleParamOrFallback("sharkBaseEnergy"));
    setFishEnergyGain(getDoubleParamOrFallback("fishEnergyGain"));
    setSharkReproductionTime(getDoubleParamOrFallback("sharkReproductionTime"));
    setFishReproductionTime(getDoubleParamOrFallback("fishReproductionTime"));

    List<List<Cell<WatorState>>> cellStates = getAllCellStates();
    sharkCells = cellStates.get(0);
    fishCells = cellStates.get(1);
  }

  /**
   * Sets the base energy level for sharks.
   *
   * @param energy the shark base energy to set.
   * @throws IllegalArgumentException if the energy value is out of bounds.
   */
  public void setSharkBaseEnergy(double energy) throws IllegalArgumentException {
    double min = getMinParam("sharkBaseEnergy");
    double max = getMaxParam("sharkBaseEnergy");
    checkBounds(energy, min, max);
    sharkBaseEnergy = energy;
    baseSharkProperties.put("energy", energy);
  }

  /**
   * Sets the energy gain for sharks when consuming fish.
   *
   * @param energy the energy gain for sharks.
   * @throws IllegalArgumentException if the energy gain value is out of bounds.
   */
  public void setFishEnergyGain(double energy) throws IllegalArgumentException {
    double min = getMinParam("fishEnergyGain");
    double max = getMaxParam("fishEnergyGain");
    checkBounds(energy, min, max);
    fishEnergyGain = energy;
  }

  /**
   * Sets the reproduction time for sharks.
   *
   * @param time the reproduction time for sharks.
   * @throws IllegalArgumentException if the time value is out of bounds.
   */
  public void setSharkReproductionTime(double time) throws IllegalArgumentException {
    double min = getMinParam("sharkReproductionTime");
    double max = getMaxParam("sharkReproductionTime");
    checkBounds(time, min, max);
    sharkReproductionTime = time;
  }

  /**
   * Sets the reproduction time for fish.
   *
   * @param time the reproduction time for fish.
   * @throws IllegalArgumentException if the time value is out of bounds.
   */
  public void setFishReproductionTime(double time) throws IllegalArgumentException {
    double min = getMinParam("fishReproductionTime");
    double max = getMaxParam("fishReproductionTime");
    checkBounds(time, min, max);
    fishReproductionTime = time;
  }

  /**
   * Returns the base energy level for sharks.
   *
   * @return the shark base energy.
   */
  public double getSharkBaseEnergy() {
    return sharkBaseEnergy;
  }

  /**
   * Returns the energy gain for sharks when consuming fish.
   *
   * @return the fish energy gain.
   */
  public double getFishEnergyGain() {
    return fishEnergyGain;
  }

  /**
   * Returns the reproduction time for sharks.
   *
   * @return the shark reproduction time.
   */
  public double getSharkReproductionTime() {
    return sharkReproductionTime;
  }

  /**
   * Returns the reproduction time for fish.
   *
   * @return the fish reproduction time.
   */
  public double getFishReproductionTime() {
    return fishReproductionTime;
  }

  /**
   * Updates the grid by processing shark movements first, then fish movements, and finalizes the
   * state transitions.
   */
  @Override
  public void update() {
    List<Cell<WatorState>> currentSharks = new ArrayList<>(sharkCells);
    for (Cell<WatorState> sharkCell : currentSharks) {
      updateSingleCell(sharkCell);
    }
    List<Cell<WatorState>> currentFish = new ArrayList<>(fishCells);
    for (Cell<WatorState> fishCell : currentFish) {
      if (fishCell.getNextState() == WatorState.FISH) {
        updateSingleCell(fishCell);
      }
    }
    grid.updateGrid();
  }

  private static void initializePropertyMaps() {
    if (baseSharkProperties == null) {
      baseSharkProperties = new HashMap<>();
      baseSharkProperties.put("time", 0.0);
      baseSharkProperties.put("energy", sharkBaseEnergy);
    }
    if (baseFishProperties == null) {
      baseFishProperties = new HashMap<>();
      baseFishProperties.put("time", 0.0);
    }
  }

  @Override
  protected void updateSingleCell(Cell<WatorState> cell) {
    if (cell.getCurrentState() == WatorState.SHARK) {
      Cell<WatorState> nextLocation = getNextSharkLocation(cell);
      moveShark(cell, nextLocation);
    } else if (cell.getCurrentState() == WatorState.FISH) {
      Cell<WatorState> nextLocation = getNextFishLocation(cell);
      moveFish(cell, nextLocation);
    }
  }

  private Cell<WatorState> getNextSharkLocation(Cell<WatorState> sharkCell) {
    List<Cell<WatorState>> fishNeighbors = new ArrayList<>();
    List<Cell<WatorState>> openNeighbors = new ArrayList<>();
    for (Cell<WatorState> neighbor : sharkCell.getNeighbors().values()) {
      if (neighbor.getNextState() == WatorState.FISH) {
        fishNeighbors.add(neighbor);
      } else if (neighbor.getCurrentState() == WatorState.OPEN
          && neighbor.getNextState() == WatorState.OPEN) {
        openNeighbors.add(neighbor);
      }
    }
    Cell<WatorState> nextLocation = sharkCell;
    if (!fishNeighbors.isEmpty()) {
      int index = (int) (Math.random() * fishNeighbors.size());
      nextLocation = fishNeighbors.get(index);
    } else if (!openNeighbors.isEmpty()) {
      int index = (int) (Math.random() * openNeighbors.size());
      nextLocation = openNeighbors.get(index);
    }
    return nextLocation;
  }

  private void moveShark(Cell<WatorState> sharkCell, Cell<WatorState> nextLocation) {
    int energy = (int) sharkCell.getProperty("energy");
    int time = (int) sharkCell.getProperty("time") + 1;
    sharkCell.setProperty("time", time);

    if (nextLocation.getNextState() == WatorState.FISH) {
      sharkCell.setProperty("energy", energy + fishEnergyGain);
      fishCells.remove(nextLocation);
    } else {
      sharkCell.setProperty("energy", energy - 1);
    }
    if ((int) sharkCell.getProperty("energy") <= 0) {
      removeSharkInCell(sharkCell);
      return;
    }
    if (nextLocation != sharkCell) {
      moveSharkToNextCell(sharkCell, nextLocation);
      if (time >= sharkReproductionTime) {
        resetSharkInOldCell(sharkCell);
        nextLocation.setProperty("time", 0);
      }
    }
  }

  private void moveSharkToNextCell(Cell<WatorState> oldCell, Cell<WatorState> nextCell) {
    nextCell.setNextState(WatorState.SHARK);
    nextCell.setCurrentState(WatorState.SHARK);
    oldCell.setNextState(WatorState.OPEN);
    oldCell.copyAllPropertiesTo(nextCell);
    oldCell.clearAllProperties();
    sharkCells.add(nextCell);
    sharkCells.remove(oldCell);
  }

  private void resetSharkInOldCell(Cell<WatorState> oldCell) {
    oldCell.setNextState(WatorState.SHARK);
    oldCell.setAllProperties(new HashMap<>(baseSharkProperties));
    sharkCells.add(oldCell);
  }

  private void removeSharkInCell(Cell<WatorState> sharkCell) {
    sharkCell.setNextState(WatorState.OPEN);
    sharkCell.clearAllProperties();
    sharkCells.remove(sharkCell);
  }

  private Cell<WatorState> getNextFishLocation(Cell<WatorState> fishCell) {
    List<Cell<WatorState>> openNeighbors = new ArrayList<>();
    for (Cell<WatorState> neighbor : fishCell.getNeighbors().values()) {
      if (neighbor.getNextState() == WatorState.OPEN) {
        openNeighbors.add(neighbor);
      }
    }
    Cell<WatorState> nextLocation = fishCell;
    if (!openNeighbors.isEmpty()) {
      int index = (int) (Math.random() * openNeighbors.size());
      nextLocation = openNeighbors.get(index);
    }
    return nextLocation;
  }

  private void moveFish(Cell<WatorState> fishCell, Cell<WatorState> nextLocation) {
    int time = (int) fishCell.getProperty("time") + 1;
    fishCell.setProperty("time", time);

    if (fishCell != nextLocation) {
      moveFishToNextCell(fishCell, nextLocation);
      if (time >= fishReproductionTime) {
        resetFishInOldCell(fishCell);
        nextLocation.setProperty("time", 0);
      }
    }
  }

  private void moveFishToNextCell(Cell<WatorState> oldCell, Cell<WatorState> nextCell) {
    nextCell.setNextState(WatorState.FISH);
    nextCell.setCurrentState(WatorState.FISH);
    oldCell.setNextState(WatorState.OPEN);
    oldCell.copyAllPropertiesTo(nextCell);
    oldCell.clearAllProperties();
    fishCells.add(nextCell);
    fishCells.remove(oldCell);
  }

  private void resetFishInOldCell(Cell<WatorState> oldCell) {
    oldCell.setNextState(WatorState.FISH);
    oldCell.setAllProperties(new HashMap<>(baseFishProperties));
    fishCells.add(oldCell);
  }

  private List<List<Cell<WatorState>>> getAllCellStates() {
    List<Cell<WatorState>> shark = new ArrayList<>();
    List<Cell<WatorState>> fish = new ArrayList<>();

    for (int r = 0; r < grid.getNumRows(); r++) {
      for (int c = 0; c < grid.getNumCols(); c++) {
        Cell<WatorState> cell = grid.getCell(r, c);
        if (cell.getCurrentState() != WatorState.OPEN) {
          cell.setProperty("time", 0);
        }
        if (cell.getCurrentState() == WatorState.SHARK) {
          shark.add(cell);
          cell.setAllProperties(new HashMap<>(baseSharkProperties));
        } else if (cell.getCurrentState() == WatorState.FISH) {
          fish.add(cell);
          cell.setAllProperties(new HashMap<>(baseFishProperties));
        }
      }
    }

    List<List<Cell<WatorState>>> result = new ArrayList<>();
    result.add(shark);
    result.add(fish);
    return result;
  }
}