package cellsociety.model.logic;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.neighbors.Coord;
import cellsociety.model.data.states.FallingState;
import cellsociety.model.data.states.PercolationState;
import java.util.ArrayList;
import java.util.List;

public class FallingLogic extends Logic<FallingState>{

  public FallingLogic(Grid<FallingState> grid) {
    super(grid);
  }

  @Override
  public void update() {
    int row = (int) (Math.random() * grid.getNumRows());
    int col = (int) (Math.random() * grid.getNumCols());
    updateSingleCell(grid.getCell(row, col));
    grid.updateGrid();
  }

  @Override
  protected void updateSingleCell(Cell<FallingState> cell) {
    if (cell.getCurrentState() == FallingState.SAND) {
      Cell<FallingState> neighbor = cell.getNeighbors().get(new Coord(1, 0));
      if (neighbor != null) {
        if (neighbor.getCurrentState() == FallingState.EMPTY) {
          neighbor.setNextState(FallingState.SAND);
          cell.setNextState(FallingState.EMPTY);
        }
      }
    }
    if (cell.getCurrentState() == FallingState.WATER) {
      List<Cell<FallingState>> neighbors = getEmptyNeighbors(cell);
      if (!neighbors.isEmpty()) {
        int index = (int) (Math.random() * neighbors.size());
        neighbors.get(index).setNextState(FallingState.WATER);
        cell.setNextState(FallingState.EMPTY);
      }
    }
  }

  private List<Cell<FallingState>> getEmptyNeighbors(Cell<FallingState> cell) {
    List<Cell<FallingState>> neighbors = new ArrayList<>();
    for (Cell<FallingState> neighbor : cell.getNeighbors().values()) {
      if (neighbor.getCurrentState() == FallingState.EMPTY) {
        neighbors.add(neighbor);
      }
    }
    return neighbors;
  }
}
