package cellsociety.model.data.states;

/**
 * Represents the different possible states for Game of Life
 */
public enum LifeState {
  DEAD(0),
  ALIVE(1);

  private final int value;

  LifeState(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  /**
   * Retrieves the LifeState corresponding to the given integer.
   *
   * @param value The integer value representing the state.
   * @return The corresponding LifeState.
   * @throws IllegalArgumentException If no state is found.
   */
  public static LifeState fromInt(int value) {
    for (LifeState state : LifeState.values()) {
      if (state.getValue() == value) {
        return state;
      }
    }
    throw new IllegalArgumentException("Invalid LifeState value: " + value);
  }
}
