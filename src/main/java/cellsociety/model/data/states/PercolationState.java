package cellsociety.model.data.states;

/**
 * Represents the different possible states for the Percolation Simulation
 *
 * @author Jacob You
 */
public enum PercolationState implements State {
  BLOCKED(0),
  OPEN(1),
  PERCOLATED(2);

  private final int value;

  PercolationState(int value) {
    this.value = value;
  }

  @Override
  public int getValue() {
    return value;
  }
}
