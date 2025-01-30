package cellsociety.model.data.cells;

import cellsociety.model.data.states.PercolationState;

public class PercolationCell extends Cell<PercolationState> {

  /**
   * Constructs a PercolationCell with specified row, column, and initial state.
   *
   * @param state the initial state of the cell
   */
  public PercolationCell(PercolationState state) {
    super(state);
  }
}
