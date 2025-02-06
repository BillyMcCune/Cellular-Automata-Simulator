package cellsociety.model.logic;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.WatorState;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the Wa-Tor world simulation logic, updating sharks and fish based on
 * energy, breeding times, and moves. All occupant data (energy, time since last breeding)
 * is stored within each Cell's properties map.
 */
public class WatorLogic extends Logic<WatorState> {

  private static int baseSharkEnergy;
  private static int fishEnergyGain;
  private static int sharkBreedingTime;
  private static int fishBreedingTime;

  /**
   * Constructs a WatorLogic instance for the given grid.
   *
   * @param grid The grid on which to run the Wa-Tor simulation.
   */
  public WatorLogic(Grid<WatorState> grid) {
    super(grid);
  }

  /**
   * Sets the base energy that each shark starts with or resets to upon breeding.
   *
   * @param energy the initial or reset energy value for sharks
   */
  public static void setBaseSharkEnergy(int energy) {
    baseSharkEnergy = energy;
  }

  /**
   * Sets the amount of energy sharks gain upon eating a fish.
   *
   * @param energy how much energy a shark gains for each fish consumed
   */
  public static void setFishEnergyGain(int energy) {
    fishEnergyGain = energy;
  }

  /**
   * Sets the number of cycles after which a shark reproduces (resets its breeding chronon).
   *
   * @param time how many updates it takes for a shark to breed
   */
  public static void setSharkBreedingTime(int time) {
    sharkBreedingTime = time;
  }

  /**
   * Sets the number of cycles after which a fish reproduces (resets its breeding chronon).
   *
   * @param time how many updates it takes for a fish to breed
   */
  public static void setFishBreedingTime(int time) {
    fishBreedingTime = time;
  }

  /**
   * Updates the grid by running shark logic first, then fish logic, to avoid partial collisions.
   * Then calls {@code grid.updateGrid()} to finalize nextState -> currentState for each cell.
   */
  @Override
  public void update() {
    List<Cell<WatorState>> sharkCells = getAllCellsOfState(WatorState.SHARK);
    List<Cell<WatorState>> fishCells  = getAllCellsOfState(WatorState.FISH);

    for (Cell<WatorState> sharkCell : sharkCells) {
      updateSingleCell(sharkCell);
    }
    for (Cell<WatorState> fishCell : fishCells) {
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
    for (Cell<WatorState> neighbor : sharkCell.getNeighbors()) {
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
    int energy = getCellEnergy(sharkCell);
    int chronon = getCellTime(sharkCell);

    if (nextLocation.getNextState() == WatorState.FISH) {
      energy += fishEnergyGain;
    }
    else {
      energy -= 1;
    }
    if (energy <= 0) {
      sharkCell.setNextState(WatorState.OPEN);
      sharkCell.clearAllProperties();
      return;
    }
    chronon += 1;
    if (nextLocation != sharkCell) {
      moveSharkToNextCell(sharkCell, nextLocation, energy, chronon);
      if (chronon >= sharkBreedingTime) {
        resetSharkInOldCell(sharkCell);
      }
    }
    else {
      sharkCell.setProperty("energy", energy);
      sharkCell.setProperty("time", chronon);
    }
  }

  private void moveSharkToNextCell(Cell<WatorState> oldCell, Cell<WatorState> nextCell, int energy, int chronon) {
    oldCell.setNextState(WatorState.OPEN);
    oldCell.clearAllProperties();

    nextCell.setNextState(WatorState.SHARK);
    nextCell.setProperty("energy", energy);
    nextCell.setProperty("time", chronon);
  }

  private void resetSharkInOldCell(Cell<WatorState> oldCell) {
    oldCell.setNextState(WatorState.SHARK);
    oldCell.setProperty("energy", baseSharkEnergy);
    oldCell.setProperty("time", 0);
  }

  private Cell<WatorState> getNextFishLocation(Cell<WatorState> fishCell) {
    List<Cell<WatorState>> openNeighbors = new ArrayList<>();
    for (Cell<WatorState> neighbor : fishCell.getNeighbors()) {
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
    int chronon = getCellTime(fishCell);
    chronon += 1;

    if (fishCell != nextLocation) {
      moveFishToNextCell(fishCell, nextLocation, chronon);
      if (chronon >= fishBreedingTime) {
        resetFishInOldCell(fishCell);
      }
    }
    else {
      fishCell.setProperty("time", chronon);
    }
  }

  private void moveFishToNextCell(Cell<WatorState> oldCell, Cell<WatorState> nextCell, int newChronon) {
    oldCell.setNextState(WatorState.OPEN);
    oldCell.clearAllProperties();

    nextCell.setNextState(WatorState.FISH);
    nextCell.setProperty("time", newChronon);
  }

  private void resetFishInOldCell(Cell<WatorState> oldCell) {
    oldCell.setNextState(WatorState.FISH);
    oldCell.setProperty("time", 0);
  }

  private int getCellEnergy(Cell<WatorState> cell) {
    Object energyVal = cell.getProperty("energy");
    if (energyVal == null) {
      cell.setProperty("energy", baseSharkEnergy);
      return baseSharkEnergy;
    }
    return (int) energyVal;
  }

  private int getCellTime(Cell<WatorState> cell) {
    Object timeVal = cell.getProperty("time");
    if (timeVal == null) {
      cell.setProperty("time", 0);
      return 0;
    }
    return (int) timeVal;
  }

  private List<Cell<WatorState>> getAllCellsOfState(WatorState state) {
    List<Cell<WatorState>> result = new ArrayList<>();
    for (int r = 0; r < grid.getNumRows(); r++) {
      for (int c = 0; c < grid.getNumCols(); c++) {
        Cell<WatorState> cell = grid.getCell(r, c);
        if (cell.getCurrentState() == state) {
          result.add(cell);
        }
      }
    }
    return result;
  }
}
