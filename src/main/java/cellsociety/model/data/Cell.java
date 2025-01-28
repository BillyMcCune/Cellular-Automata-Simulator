package cellsociety.model.data;

/**
 * @author Jacob You Purpose: A data class that represents a single cell in a cellular automaton
 * grid. Each cell has coordinates, a current state, and a next state. Assumptions: Dependecies
 * (classes or packages): How to Use: Call methods to set the next stage, then update to switch the
 * cell to the next step.
 */

public class Cell {

  /**
   * The coordinates of the cell in the grid. Index 0 represents the row, and index 1 represents the
   * column.
   */
  private int[] coordinates = new int[2];

  /**
   * The current state of the cell.
   */
  private int currState;

  /**
   * The next state of the cell, to be updated based on rules.
   */
  private int nextState;

  /**
   * Constructs a Cell with specified row, column, and initial state.
   *
   * @param row   the row position of the cell in the grid
   * @param col   the column position of the cell in the grid
   * @param state the initial state of the cell
   */
  public Cell(int row, int col, int state) {
    coordinates[0] = row;
    coordinates[1] = col;
    currState = state;
    nextState = currState;
  }

  /**
   * Retrieves the coordinates of the cell.
   *
   * @return an array where index 0 is the row and index 1 is the column
   */
  public int[] getCoordinates() {
    return coordinates;
  }

  /**
   * Retrieves the current state of the cell.
   *
   * @return the current state
   */
  public int getCurrState() {
    return currState;
  }

  /**
   * Sets the next state of the cell.
   *
   * @param nextState the state to be applied in the next update
   */
  public void setNextState(int nextState) {
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