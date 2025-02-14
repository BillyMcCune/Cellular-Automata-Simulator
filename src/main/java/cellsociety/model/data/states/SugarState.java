package cellsociety.model.data.states;

public enum SugarState implements State {
  EMPTY(0),
  AGENT(1);

  private final int value;

  SugarState(int value) {
    this.value = value;
  }

  @Override
  public int getValue() {
    return value;
  }
}
