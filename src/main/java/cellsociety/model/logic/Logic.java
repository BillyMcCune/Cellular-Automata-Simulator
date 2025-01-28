package cellsociety.model.logic;

import cellsociety.model.data.Cell;
import cellsociety.model.data.Grid;
import java.util.List;

/**
 * Abstract superclass responsible for managing the logic of a cellular automaton. Subclasses should
 * implement specific rules.
 */
public abstract class Logic {
  private Grid grid;

  public Logic(Grid grid) {
    this.grid = grid;
  }

  /**
   * Updates the entire game state by one tick. Subclasses should provide an implementation for this
   * method.
   */
  public abstract void update();
}
