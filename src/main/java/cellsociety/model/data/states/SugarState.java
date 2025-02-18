package cellsociety.model.data.states;

/**
 * Represents the different possible states for SugarScape
 *
 * @author Jacob You
 */
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
