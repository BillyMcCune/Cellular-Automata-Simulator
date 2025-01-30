package cellsociety.model.data.cells;

import cellsociety.model.data.states.LifeState;

public class LifeCell extends Cell<LifeState> {

  /**
   * Constructs a LifeCell with specified row, column, and initial state.
   *
   * @param state the initial state of the cell
   */
  public LifeCell(LifeState state) {
    super(state);
  }
}
