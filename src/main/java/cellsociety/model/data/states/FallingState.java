package cellsociety.model.data.states;

public enum FallingState implements State {
  EMPTY(0),
  METAL(1),
  SAND(2),
  WATER(3);

  private final int value;

  FallingState(int value) {
    this.value = value;
  }

  @Override
  public int getValue() {
    return value;
  }
}
