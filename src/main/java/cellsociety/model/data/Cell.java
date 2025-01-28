package cellsociety.model.data;

public class Cell {

  private int[] coordinates = new int[2];
  private int currState;
  private int nextState;

  public Cell(int row, int col, int state) {
    coordinates[0] = row;
    coordinates[1] = col;
    currState = state;
    nextState = currState;
  }

  public int[] getCoordinates() {
    return coordinates;
  }

  public int getCurrState() {
    return currState;
  }

  public void setNextState(int currState) {
    this.nextState = currState;
  }

  public void update() {
    this.currState = this.nextState;
  }
}
