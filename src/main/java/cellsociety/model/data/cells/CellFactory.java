package cellsociety.model.data.cells;

import cellsociety.model.data.states.State;

/**
 * A generic cell factory that creates Cell instances for any enum type that implements
 * {@link State}.
 *
 * @param <T> the enum type representing the cell state
 * @author Jacob You
 */
public class CellFactory<T extends Enum<T> & State> {

  private final Class<T> stateType;

  /**
   * Constructs a new GenericCellFactory.
   *
   * @param stateType the Class object for the enum type T
   */
  public CellFactory(Class<T> stateType) {
    this.stateType = stateType;
  }

  /**
   * Creates a new Cell instance using the provided integer state.
   *
   * @param initialState the initial state of the cell as an integer
   * @return a new Cell instance with the corresponding enum state
   */
  public Cell<T> createCell(int initialState) {
    T state = State.fromInt(stateType, initialState);
    return new Cell<>(state);
  }
}
