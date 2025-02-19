package cellsociety.model.data.states;

/**
 * Represents the different possible states for Spreading of Fire
 *
 * @author Jacob You
 */
public enum FireState implements State {
  EMPTY(0),
  TREE(1),
  BURNING(2);

  private final int value;

  FireState(int value) {
    this.value = value;
  }

  @Override
  public int getValue() {
    return value;
  }
}
