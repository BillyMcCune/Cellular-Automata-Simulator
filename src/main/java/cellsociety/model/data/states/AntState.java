package cellsociety.model.data.states;

/**
 * Represents the different possible states for Foraging Ants.
 *
 * @author Jacob You
 */
public enum AntState implements State {
  EMPTY(0),
  BLOCKED(1),
  NEST(2),
  FOOD(3);

  private final int value;

  AntState(int value) {
    this.value = value;
  }

  @Override
  public int getValue() {
    return value;
  }
}
