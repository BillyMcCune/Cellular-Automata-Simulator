package cellsociety.model.data.states;

/**
 * Represents the different possible states for Game of Life
 */
public enum LifeState implements State {
  DEAD(0),
  ALIVE(1);

  private final int value;

  LifeState(int value) {
    this.value = value;
  }

  @Override
  public int getValue() {
    return value;
  }
}
