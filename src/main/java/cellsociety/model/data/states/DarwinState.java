package cellsociety.model.data.states;

/**
 * A placeholder enum that implements State for Darwin simulations but doesn't actually do anything.
 * The purpose of this class is to satisfy abstraction. The real value is stored elsewhere.
 *
 * @author Jacob You
 */
public enum DarwinState implements State {
  DUMMY;

  /**
   * Ignores 'value' and always returns DUMMY. The real value is stored elsewhere.
   */
  public static DarwinState fromInt(int value) {
    return DUMMY;
  }

  /**
   * This is required by the State interface, but should not be used.
   */
  @Override
  public int getValue() {
    return 0;
  }
}
