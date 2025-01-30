package cellsociety.model.data.factories;

import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.PercolationCell;
import cellsociety.model.data.states.PercolationState;

public class PercolationCellFactory extends CellFactory<PercolationState> {

  @Override
  public Cell<PercolationState> createCell(int initialState) {
    PercolationState state = PercolationState.fromInt(initialState);
    return new PercolationCell(state);
  }
}