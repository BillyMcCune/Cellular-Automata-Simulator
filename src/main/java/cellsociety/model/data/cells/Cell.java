package cellsociety.model.data.cells;

/**
 * Abstract generic base class representing a cell in a grid.
 *
 * @author Jacob You
 * @param <T> the enum type representing the cell state
 */

public abstract class Cell {

  protected int row;
  protected int col;
  protected T currState;
  protected T nextState;

  /**
   * Constructs a Cell with specified row, column, and initial state.
   *
   * @param row   the row position of the cell in the grid
   * @param col   the column position of the cell in the grid
   * @param state the initial state of the cell
   */
  public Cell(int row, int col, T state) {
    this.row = row;
    this.col = col;
    currState = state;
    nextState = currState;
  }

  /**
   * Returns the row of the cell.
   *
   * @return the row of the cell
   */
  public int getRow() {
    return row;
  }

  /**
   * Returns the column of the cell.
   *
   * @return the column of the cell
   */
  public int getCol() {
    return col;
  }

  /**
   * Retrieves the current state of the cell.
   *
   * @return the current state
   */
  public T getCurrState() {
    return currState;
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