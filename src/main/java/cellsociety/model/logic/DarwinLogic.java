package cellsociety.model.logic;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.states.DarwinState;
import cellsociety.model.logic.helpers.DarwinHelper;
import cellsociety.model.logic.helpers.DarwinHelper.InstructionResult;
import cellsociety.model.logic.helpers.InfectionRecord;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Handles Darwin simulation logic.
 * <p>
 * This class processes each cell's instructions using a DarwinHelper,
 * updates cell states based on infections and movement, and coordinates overall simulation updates.
 * </p>
 *
 * @author Jacob You
 */
public class DarwinLogic extends Logic<DarwinState> {

  private DarwinHelper darwinHelper;
  private double nearbyAhead;
  private final Map<Cell<DarwinState>, Integer> movingCells = new HashMap<>();
  private final List<Cell<DarwinState>> infectedCells = new ArrayList<>();
  private final List<Cell<DarwinState>> stationaryCells = new ArrayList<>();

  /**
   * Constructs a DarwinLogic instance using the given grid and parameters.
   *
   * @param grid       the grid on which the simulation is run
   * @param parameters the simulation parameters
   */
  public DarwinLogic(Grid<DarwinState> grid, ParameterRecord parameters) {
    super(grid, parameters);
    setNearbyAhead(getDoubleParamOrFallback("nearbyAhead"));
    darwinHelper = new DarwinHelper(this, grid);
    initializeSpecies();
  }

  /**
   * Assigns species programs to the simulation.
   *
   * @param species a map of species IDs to their program instructions
   */
  public void assignSpeciesPrograms(Map<Integer, List<String>> species) {
    darwinHelper = new DarwinHelper(species, this, grid);
  }

  /**
   * Initializes species data for each cell in the grid.
   * <p>
   * For every cell with a non-zero speciesID, this method initializes the cell's instructionIndex
   * (if not already set), sets its orientation (if not already set), and adds an initial InfectionRecord.
   * </p>
   */
  public void initializeSpecies() {
    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();
    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        Cell<DarwinState> cell = grid.getCell(row, col);
        if (cell.getProperty("speciesID") == 0) {
          continue;
        }
        if (cell.getProperty("instructionIndex") == 0) {
          cell.setProperty("instructionIndex", 1);
        }
        if (cell.getProperty("orientation") == 0) {
          cell.setProperty("orientation", 0);
        }
        InfectionRecord infectionRecord = new InfectionRecord(cell.getProperty("speciesID"), 0);
        cell.addQueueRecord(infectionRecord);
      }
    }
  }

  /**
   * Updates the simulation for one time step.
   * <p>
   * Processes each cell with a non-zero speciesID, updates cells based on infections,
   * moves cells that need to move, and finally updates the grid to finalize state changes.
   * </p>
   */
  @Override
  public void update() {
    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();

    // Process each cell with a non-zero speciesID.
    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        Cell<DarwinState> cell = grid.getCell(row, col);
        if (cell.getProperty("speciesID") != 0) {
          updateSingleCell(cell);
        }
      }
    }

    // Process infected cells.
    for (Cell<DarwinState> cell : infectedCells) {
      InfectionRecord infectionRecord = (InfectionRecord) cell.peekQueueRecord();
      cell.setProperty("speciesID", infectionRecord.getSpeciesID());
      cell.setProperty("instructionIndex", 1);
      movingCells.remove(cell);
      stationaryCells.remove(cell);
    }

    // Update and move cells that are marked as moving.
    for (Entry<Cell<DarwinState>, Integer> cellEntry : movingCells.entrySet()) {
      updateCellData(cellEntry.getKey());
      moveCell(cellEntry.getKey(), cellEntry.getValue());
    }

    // Update stationary cells.
    for (Cell<DarwinState> cell : stationaryCells) {
      updateCellData(cell);
    }

    // Clear temporary collections and update grid.
    infectedCells.clear();
    movingCells.clear();
    stationaryCells.clear();
    grid.updateGrid();
  }

  /**
   * Updates a single cell's state by processing its current instruction.
   * <p>
   * Uses the DarwinHelper to process the cell's instruction, updates the instructionIndex,
   * and classifies the cell for movement, infection, or stationary processing.
   * </p>
   *
   * @param cell the cell to update
   */
  @Override
  protected void updateSingleCell(Cell<DarwinState> cell) {
    InstructionResult result = darwinHelper.processCell(cell);
    cell.setProperty("instructionIndex", cell.getProperty("instructionIndex") + 1);
    if (result == null) {
      return;
    }
    if (result.moveDistance() > 0) {
      movingCells.put(cell, result.moveDistance());
    } else if (result.infectedCell() != null) {
      infectedCells.add(result.infectedCell());
    } else {
      stationaryCells.add(cell);
    }
  }

  /**
   * Updates cell data for a given cell.
   * <p>
   * Checks the infection record queue; if multiple records exist and the current record's duration
   * has expired, removes the record and updates the cell's speciesID and resets instructionIndex.
   * </p>
   *
   * @param cell the cell to update
   */
  private void updateCellData(Cell<DarwinState> cell) {
    InfectionRecord infectionRecord = (InfectionRecord) cell.peekQueueRecord();
    if (cell.getQueueRecords().size() > 1 && infectionRecord.decrementDuration()) {
      cell.removeQueueRecord();
      infectionRecord = (InfectionRecord) cell.peekQueueRecord();
      cell.setProperty("speciesID", infectionRecord.getSpeciesID());
      cell.setProperty("instructionIndex", 1);
    }
  }

  /**
   * Moves the specified cell by a given distance.
   * <p>
   * Performs a raycast based on the cell's orientation, sorts the potential path by Manhattan distance,
   * and moves the cell to the furthest available destination that is empty.
   * </p>
   *
   * @param cell     the cell to move
   * @param distance the number of steps to move
   */
  public void moveCell(Cell<DarwinState> cell, int distance) {
    Direction facing = darwinHelper.getOrientation(cell);
    grid.assignRaycastNeighbor(cell, facing, distance);
    Map<Direction, Cell<DarwinState>> pathMap = cell.getNeighbors();

    List<Map.Entry<Direction, Cell<DarwinState>>> pathEntries = new ArrayList<>(pathMap.entrySet());
    pathEntries.sort(Comparator.comparingInt(entry -> getManhattanDistance(entry.getKey())));

    Cell<DarwinState> destination = null;
    for (Map.Entry<Direction, Cell<DarwinState>> entry : pathEntries) {
      Cell<DarwinState> nextCell = entry.getValue();
      if (nextCell.getProperty("speciesID") != 0) {
        break;
      }
      destination = nextCell;
    }

    if (destination != null) {
      cell.copyAllPropertiesTo(destination);
      cell.copyQueueTo(destination);
      cell.clearAllProperties();
      cell.clearQueueRecords();
    }
  }

  /**
   * Calculates the Manhattan distance of a given direction.
   *
   * @param direction the direction for which to calculate the Manhattan distance
   * @return the Manhattan distance (sum of absolute dy and dx)
   */
  private int getManhattanDistance(Direction direction) {
    return Math.abs(direction.dy()) + Math.abs(direction.dx());
  }

  /**
   * Sets the nearbyAhead parameter, which defines the distance for neighbor detection.
   *
   * @param nearbyAhead the distance value to set (must be at least 1)
   * @throws IllegalArgumentException if the value is out of bounds
   */
  public void setNearbyAhead(double nearbyAhead) throws IllegalArgumentException {
    checkBounds(nearbyAhead, 1, Double.MAX_VALUE);
    this.nearbyAhead = nearbyAhead;
  }

  /**
   * Returns the nearbyAhead parameter.
   *
   * @return the distance used for neighbor detection
   */
  public double getNearbyAhead() {
    return nearbyAhead;
  }
}
