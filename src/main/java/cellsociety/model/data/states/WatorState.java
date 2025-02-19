package cellsociety.model.data.states;

/**
 * Represents the different possible states for the Wator Simulation
 *
 * @author Jacob You
 */
public enum WatorState implements State {
  OPEN(0),
  FISH(1),
  SHARK(2);

  private final int value;

  WatorState(int value) {
    this.value = value;
  }

  @Override
  public int getValue() {
    return value;
  }
}
