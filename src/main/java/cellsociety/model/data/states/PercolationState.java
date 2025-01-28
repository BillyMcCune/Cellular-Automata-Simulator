package cellsociety.model.data.states;

/**
 * Represents the different possible states for the Percolation Simulation
 */
public enum PercolationState {
  BLOCKED(0),
  OPEN(1),
  PERCOLATED(2);

  private final int value;

  PercolationState(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  /**
   * Retrieves the PercolationState corresponding to the given integer.
   *
   * @param value The integer value representing the state.
   * @return The corresponding PercolationState.
   * @throws IllegalArgumentException If no state is found.
   */
  public static PercolationState fromInt(int value) {
    for (PercolationState state : PercolationState.values()) {
      if (state.getValue() == value) {
        return state;
      }
    }
    throw new IllegalArgumentException("Invalid PercolationState value: " + value);
  }
}
