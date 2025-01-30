package cellsociety.model.data.states;

public enum FireState {
  EMPTY(0),
  TREE(1),
  BURNING(2);

  private final int value;

  FireState(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  /**
   * Retrieves the FireState corresponding to the given integer.
   *
   * @param value The integer value representing the state.
   * @return The corresponding FireState.
   * @throws IllegalArgumentException If no state is found.
   */
  public static FireState fromInt(int value) {
    for (FireState state : FireState.values()) {
      if (state.getValue() == value) {
        return state;
      }
    }
    throw new IllegalArgumentException("Invalid FireState value: " + value);
  }
}
