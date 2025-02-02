package cellsociety.model.data.cells;

import cellsociety.model.data.states.State;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract generic base class representing a cell in a grid.
 *
 * @author Jacob You
 * @param <T> the enum type representing the cell state
 */

public class Cell<T extends Enum<T> & State> {

  protected List<Cell<T>> neighbors = new ArrayList<>();
  protected T currState;
  protected T nextState;

  /**
   * Constructs a Cell with specified row, column, and initial state.
   *
   * @param state the initial state of the cell
   */
  public Cell(T state) {
    currState = state;
    nextState = currState;
  }

  /**
   * Retrieves the neighbors of the cell
   */
  public List<Cell<T>> getNeighbors() {
    return neighbors;
  }

  /**
   * Retrieves the current state of the cell.
   *
   * @return the current state
   */
  public T getCurrentState() {
    return currState;
  }

  /**
   * Sets the neighbors of the cell
   */
  public void setNeighbors(List<Cell<T>> neighbors) {
    this.neighbors = neighbors;
  }

  /**
   * Sets the next state of the cell.
   *
   * @param nextState the state to be applied in the next update
   */
  public void setNextState(T nextState) {
    this.nextState = nextState;
  }

  /**
   * Updates the cell's current state to the next state. This method should be called after all next
   * states have been determined.
   */
  public void update() {
    this.currState = this.nextState;
  }
}