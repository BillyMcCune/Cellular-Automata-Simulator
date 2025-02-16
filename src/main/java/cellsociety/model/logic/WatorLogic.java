package cellsociety.model.logic;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.WatorState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Implements the Wa-Tor world simulation logic, updating sharks and fish based on
 * energy, breeding times, and moves. All occupant data (energy, time since last breeding)
 * is stored within each Cell's properties map.
 */
public class WatorLogic extends Logic<WatorState> {

  private static double fishEnergyGain;
  private static double sharkReproductionTime;
  private static double fishReproductionTime;
  private final List<Cell<WatorState>> sharkCells;
  private final List<Cell<WatorState>> fishCells;
  private static HashMap<String, Double> baseSharkProperties;
  private static HashMap<String, Double> baseFishProperties;

  /**
   * Constructs a WatorLogic instance for the given grid.
   *
   * @param grid The grid on which to run the Wa-Tor simulation.
   */
  public WatorLogic(Grid<WatorState> grid) {
    super(grid);
    initializePropertyMaps();
    List<List<Cell<WatorState>>> cellStates = getAllCellStates();
    sharkCells = cellStates.get(0);
    fishCells = cellStates.get(1);
  }

  private static void initializePropertyMaps() {
    if (baseSharkProperties == null) {
      baseSharkProperties = new HashMap<>();
      baseSharkProperties.put("time", 0.0);
      baseSharkProperties.put("energy", 0.0);
    }
    if (baseFishProperties == null) {
      baseFishProperties = new HashMap<>();
      baseFishProperties.put("time", 0.0);
    }
  }

  /**
   * Sets the base energy that each shark starts with or resets to upon breeding.
   *
   * @param energy the initial or reset energy value for sharks
   */
  public static void setBaseSharkEnergy(double energy) {
    baseSharkProperties.put("energy", energy);
  }

  /**
   * Sets the amount of energy sharks gain upon eating a fish.
   *
   * @param energy how much energy a shark gains for each fish consumed
   */
  public static void setFishEnergyGain(double energy) {
    fishEnergyGain = energy;
  }

  /**
   * Sets the number of cycles after which a shark reproduces (resets its breeding chronon).
   *
   * @param time how many updates it takes for a shark to breed
   */
  public static void setSharkReproductionTime(double time) {
    sharkReproductionTime = time;
  }

  /**
   * Sets the number of cycles after which a fish reproduces (resets its breeding chronon).
   *
   * @param time how many updates it takes for a fish to breed
   */
  public static void setFishReproductionTime(double time) {
    fishReproductionTime = time;
  }

  /**
   * Updates the grid by running shark logic first, then fish logic, to avoid partial collisions.
   * Then calls {@code grid.updateGrid()} to finalize nextState -> currentState for each cell.
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

  @Override
  protected void updateSingleCell(Cell<WatorState> cell) {
    if (cell.getCurrentState() == WatorState.SHARK) {
      Cell<WatorState> nextLocation = getNextSharkLocation(cell);
      moveShark(cell, nextLocation);
    }
    else if (cell.getCurrentState() == WatorState.FISH) {
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
      }
      else if (neighbor.getCurrentState() == WatorState.OPEN && neighbor.getNextState() == WatorState.OPEN) {
        openNeighbors.add(neighbor);
      }
    }
    Cell<WatorState> nextLocation = sharkCell;
    if (!fishNeighbors.isEmpty()) {
      int index = (int) (Math.random() * fishNeighbors.size());
      nextLocation = fishNeighbors.get(index);
    }
    else if (!openNeighbors.isEmpty()) {
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
    }
    else {
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
