package cellsociety.model.logic;

/**
 * Abstract superclass responsible for managing the logic of a cellular automaton. Subclasses should
 * implement specific rules.
 */
public abstract class Logic {

  /**
   * Updates the entire game state by one tick. Subclasses should provide an implementation for this
   * method.
   */
  public abstract void update();
}
