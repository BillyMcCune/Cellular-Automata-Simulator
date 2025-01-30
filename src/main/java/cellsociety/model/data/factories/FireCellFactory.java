package cellsociety.model.data.factories;

import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.FireState;

public class FireCellFactory extends CellFactory<FireState> {

  @Override
  public Cell<FireState> createCell(int initialState) {
    FireState state = FireState.fromInt(initialState);
    return new Cell<>(state);
  }
}

