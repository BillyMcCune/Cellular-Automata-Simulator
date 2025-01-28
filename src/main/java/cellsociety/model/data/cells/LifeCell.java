package cellsociety.model.data.cells;

import cellsociety.model.data.states.LifeState;

public class LifeCell extends Cell {

  /**
   * Constructs a Cell with specified row, column, and initial state.
   *
   * @param row   the row position of the cell in the grid
   * @param col   the column position of the cell in the grid
   * @param state the initial state of the cell
   */
  public LifeCell(int row, int col, LifeState state) {
    super(row, col, state);
  }
}
