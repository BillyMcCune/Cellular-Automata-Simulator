package cellsociety.model.data.factories;

import cellsociety.model.data.cells.Cell;

/**
 * Functional interface for creating Cell instances.
 *
 * @param <T> the enum type representing the cell state
 */

public abstract class CellFactory<T extends Enum<T>> {
  /**
   * Creates a new Cell instance.
   *
   * @param row   the row index of the cell
   * @param col   the column index of the cell
   * @param state the initial state of the cell
   * @return a new Cell instance
   */
  public abstract Cell<T> createCell(int row, int col, int state);
}
