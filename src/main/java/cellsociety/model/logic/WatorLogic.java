package cellsociety.model.logic;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.WatorState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Implements the Wa-Tor world simulation logic, updating sharks and fish based on energy,
 * breeding times, and moves. This class uses nextState checks to avoid collisions.
 */
public class WatorLogic extends Logic<WatorState> {

  private final HashMap<Cell<WatorState>, Integer> sharkEnergy = new HashMap<>();
  private final HashMap<Cell<WatorState>, Integer> time = new HashMap<>();

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
   * Sets the number of cycles after which a shark reproduces (resets its chronon).
   *
   * @param time how many updates it takes for a shark to breed
   */
  public static void setSharkBreedingTime(int time) {
    sharkBreedingTime = time;
  }

  /**
   * Sets the number of cycles after which a fish reproduces (resets its chronon).
   *
   * @param time how many updates it takes for a fish to breed
   */
  public static void setFishBreedingTime(int time) {
    fishBreedingTime = time;
  }

  /**
   * Updates the grid by running shark logic first, then fish logic, to avoid partial collisions.
   * Then, calls {@code grid.updateGrid()} to finalize nextState -> currentState for each cell.
   */
  @Override
  public void update() {
    List<Cell<WatorState>> sharkCells = getAllCellsOfState(WatorState.SHARK);
    List<Cell<WatorState>> fishCells = getAllCellsOfState(WatorState.FISH);

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
      } else if (neighbor.getCurrentState() == WatorState.OPEN && neighbor.getNextState() == WatorState.OPEN) {
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
    if (nextLocation.getNextState() == WatorState.FISH) {
      sharkEnergy.put(sharkCell, sharkEnergy.get(sharkCell) + fishEnergyGain);
    }
    else {
      sharkEnergy.put(sharkCell, sharkEnergy.get(sharkCell) - 1);
    }

    if (sharkEnergy.get(sharkCell) <= 0) {
      sharkEnergy.remove(sharkCell);
      time.remove(sharkCell);
      sharkCell.setNextState(WatorState.OPEN);
      return;
    }

    time.put(sharkCell, time.get(sharkCell) + 1);

    if (nextLocation != sharkCell) {
      moveSharkInLists(sharkCell, nextLocation);
      if (time.get(sharkCell) >= sharkBreedingTime) {
        sharkEnergy.put(sharkCell, baseSharkEnergy);
        time.put(sharkCell, 0);
        sharkCell.setNextState(WatorState.SHARK);
      }
    }
  }

  private void moveSharkInLists(Cell<WatorState> sharkCell, Cell<WatorState> nextLocation) {
    sharkEnergy.put(nextLocation, sharkEnergy.get(sharkCell));
    time.put(nextLocation, time.get(sharkCell));
    nextLocation.setNextState(WatorState.SHARK);

    sharkEnergy.remove(sharkCell);
    time.remove(sharkCell);
    sharkCell.setNextState(WatorState.OPEN);
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
    time.put(nextLocation, time.get(fishCell) + 1);

    if (fishCell != nextLocation) {
      moveFishInLists(fishCell, nextLocation);
      if (time.get(fishCell) >= fishBreedingTime) {
        time.put(fishCell, 0);
        fishCell.setNextState(WatorState.FISH);
      }
    }
  }

  private void moveFishInLists(Cell<WatorState> fishCell, Cell<WatorState> nextLocation) {
    nextLocation.setNextState(WatorState.FISH);
    time.put(nextLocation, time.get(fishCell));
    fishCell.setNextState(WatorState.OPEN);
    time.remove(fishCell);
  }

  private List<Cell<WatorState>> getAllCellsOfState(WatorState state) {
    List<Cell<WatorState>> result = new ArrayList<>();
    for (int row = 0; row < grid.getNumRows(); row++) {
      for (int col = 0; col < grid.getNumCols(); col++) {
        if (grid.getCell(row, col).getCurrentState() == state) {
          result.add(grid.getCell(row, col));
        }
      }
    }
    return result;
  }
}
