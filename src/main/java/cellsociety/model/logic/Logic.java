package cellsociety.model.logic;

import cellsociety.model.data.Grid;

/**
 * Abstract superclass responsible for managing the logic of a cellular automaton. Subclasses should
 * implement specific rules.
 *
 * @param <T> The enum type representing the cell state
 */
public abstract class Logic<T extends Enum<T>> {
  protected Grid<T> grid;

  public Logic(Grid<T> grid) {
    this.grid = grid;
  }

  /**
   * Updates the entire game state by one tick. Subclasses should provide an implementation for this
   * method.
   */
  public abstract void update();
}
