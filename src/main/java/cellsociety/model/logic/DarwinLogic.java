package cellsociety.model.logic;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellQueueRecord;
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
 *
 * @author Jacob You
 */
public class DarwinLogic extends Logic<DarwinState> {

  private DarwinHelper darwinHelper;
  private double nearbyAhead;
  private final Map<Cell<DarwinState>, Integer> movingCells = new HashMap<>();
  private final List<Cell<DarwinState>> infectedCells = new ArrayList<>();
  private final List<Cell<DarwinState>> stationaryCells = new ArrayList<>();

  public DarwinLogic(Grid<DarwinState> grid, ParameterRecord parameters) {
    super(grid, parameters);
    setNearbyAhead(getDoubleParamOrFallback("nearbyAhead"));
    darwinHelper = new DarwinHelper(this, grid);
    initializeSpecies();
  }

  public void assignSpeciesPrograms(Map<Integer, List<String>> species) {
    darwinHelper = new DarwinHelper(species, this, grid);
  }

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

  @Override
  public void update() {
    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();

    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        Cell<DarwinState> cell = grid.getCell(row, col);
        if (cell.getProperty("speciesID") != 0) {
          updateSingleCell(cell);
        }
      }
    }
    for (Cell<DarwinState> cell : infectedCells) {
      InfectionRecord infectionRecord = (InfectionRecord) cell.peekQueueRecord();
      cell.setProperty("speciesID", infectionRecord.getSpeciesID());
      cell.setProperty("instructionIndex", 1);
      movingCells.remove(cell);
      stationaryCells.remove(cell);
    }
    for (Entry<Cell<DarwinState>, Integer> cellEntry : movingCells.entrySet()) {
      updateCellData(cellEntry.getKey());
      moveCell(cellEntry.getKey(), cellEntry.getValue());
    }
    for (Cell<DarwinState> cell : stationaryCells) {
      updateCellData(cell);
    }
    infectedCells.clear();
    movingCells.clear();
    stationaryCells.clear();
    grid.updateGrid();
  }

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

  private void updateCellData(Cell<DarwinState> cell) {
    InfectionRecord infectionRecord = (InfectionRecord) cell.peekQueueRecord();
    if (cell.getQueueRecords().size() > 1 && infectionRecord.decrementDuration()) {
      cell.removeQueueRecord();
      infectionRecord = (InfectionRecord) cell.peekQueueRecord();
      cell.setProperty("speciesID", infectionRecord.getSpeciesID());
      cell.setProperty("instructionIndex", 1);
    }
  }

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

  private int getManhattanDistance(Direction direction) {
    return Math.abs(direction.dy()) + Math.abs(direction.dx());
  }

  public void setNearbyAhead(double nearbyAhead) throws IllegalArgumentException {
    checkBounds(nearbyAhead, 1, Double.MAX_VALUE);
    this.nearbyAhead = nearbyAhead;
  }

  public double getNearbyAhead() {
    return nearbyAhead;
  }
}
