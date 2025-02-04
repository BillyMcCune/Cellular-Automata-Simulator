package cellsociety.model.data.states;

/**
 * Represents the different possible states for the Percolation Simulation
 */
public enum SegregationState implements State {
  OPEN(0),
  RED(1),
  BLUE(2);

  private final int value;

  SegregationState(int value) {
    this.value = value;
  }

  @Override
  public int getValue() {
    return value;
  }
}
