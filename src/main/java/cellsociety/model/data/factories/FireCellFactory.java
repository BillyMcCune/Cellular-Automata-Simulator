package cellsociety.model.data.factories;

import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.FireCell;
import cellsociety.model.data.cells.PercolationCell;
import cellsociety.model.data.states.FireState;
import cellsociety.model.data.states.PercolationState;

public class FireCellFactory extends CellFactory<FireState> {

  @Override
  public Cell<FireState> createCell(int initialState) {
    FireState state = FireState.fromInt(initialState);
    return new FireCell(state);
  }
}

