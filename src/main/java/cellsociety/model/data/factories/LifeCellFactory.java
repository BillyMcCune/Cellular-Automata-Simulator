package cellsociety.model.data.factories;

import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.LifeCell;
import cellsociety.model.data.states.LifeState;

public class LifeCellFactory implements CellFactory {

  @Override
  public Cell createCell(int row, int col, int initialState) {
    LifeState state = LifeState.fromInt(initialState);
    return new LifeCell(row, col, state);
  }
}

