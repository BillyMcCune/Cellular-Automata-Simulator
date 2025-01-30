package cellsociety.model.data.factories;

import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.LifeCell;
import cellsociety.model.data.states.LifeState;

public class LifeCellFactory extends CellFactory<LifeState> {

  @Override
  public Cell<LifeState> createCell(int initialState) {
    LifeState state = LifeState.fromInt(initialState);
    return new LifeCell(state);
  }
}

