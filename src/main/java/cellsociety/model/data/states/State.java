package cellsociety.model.data.states;

/**
 * An interface for enums that have an associated integer state value.
 */
public interface State {

  int getValue();

  /**
   * Converts an integer value to the corresponding enum constant.
   *
   * @param <T>       The type of the enum.
   * @param enumClass The class of the enum.
   * @param value     The integer value.
   * @return The corresponding enum constant.
   * @throws IllegalArgumentException if no matching constant is found.
   */
  static <T extends Enum<T> & State> T fromInt(Class<T> enumClass, int value) {
    for (T constant : enumClass.getEnumConstants()) {
      if (constant.getValue() == value) {
        return constant;
      }
    }
    throw new IllegalArgumentException("Invalid " + enumClass.getSimpleName() + " value: " + value);
  }
}
