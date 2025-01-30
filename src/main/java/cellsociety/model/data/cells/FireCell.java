package cellsociety.model.data.cells;

import cellsociety.model.data.states.FireState;

public class FireCell extends Cell<FireState> {
  /**
   * Constructs a Cell with specified row, column, and initial state.
   *
   * @param state the initial state of the cell
   */
  public FireCell(FireState state) {
    super(state);
  }
}
