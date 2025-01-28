package cellsociety.model.data.cells;

import cellsociety.model.data.states.PercolationState;

public class PercolationCell extends Cell<PercolationState> {

  /**
   * Constructs a Cell with specified row, column, and initial state.
   *
   * @param row   the row position of the cell in the grid
   * @param col   the column position of the cell in the grid
   * @param state the initial state of the cell
   */
  public PercolationCell(int row, int col, PercolationState state) {
    super(row, col, state);
  }
}
