package cellsociety.model.data.states;

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
