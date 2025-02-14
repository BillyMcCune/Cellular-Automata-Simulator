package cellsociety.model.data.cells;

import cellsociety.model.data.neighbors.Coord;
import cellsociety.model.data.states.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract generic base class representing a cell in a grid.
 *
 * @author Jacob You
 * @param <T> the enum type representing the cell state
 */

public class Cell<T extends Enum<T> & State> {

  protected Map<Coord, Cell<T>> neighbors = new HashMap<Coord, Cell<T>>();
  protected T currState;
  protected T nextState;
  protected Map<String, Object> properties;

  /**
   * Constructs a Cell with specified row, column, and initial state.
   *
   * @param state the initial state of the cell
   */
  public Cell(T state) {
    currState = state;
    nextState = currState;
  }

  /**
   * Retrieves the neighbors of the cell
   */
  public Map<Coord, Cell<T>> getNeighbors() {
    return neighbors;
  }

  /**
   * Retrieves the current state of the cell.
   *
   * @return the current state
   */
  public T getCurrentState() {
    return currState;
  }

  public void setCurrentState(T state) {
    currState = state;
  }

  /**
   * Retrieves the next state of the cell
   *
   * @return the next state
   */
  public T getNextState() {
    return nextState;
  }

  /**
   * Sets the neighbors of the cell
   */
  public void setNeighbors(Map<Coord, Cell<T>> neighbors) {
    this.neighbors = neighbors;
  }

  /**
   * Sets the next state of the cell.
   *
   * @param nextState the state to be applied in the next update
   */
  public void setNextState(T nextState) {
    this.nextState = nextState;
  }

  /**
   * Updates the cell's current state to the next state. This method should be called after all next
   * states have been determined.
   */
  public void update() {
    this.currState = this.nextState;
  }

  public void setProperty(String property, Object value) {
    if (properties == null) {
      properties = new HashMap<>();
    }
    properties.put(property, value);
  }

  public Object getProperty(String property) {
    if (properties != null && properties.containsKey(property)) {
      return properties.get(property);
    }
    System.err.printf("Property not found: %s \n", property);
    return null;
  }

  public void setAllProperties(Map<String, Object> props) {
    if (props == null) {
      properties = null;
    } else {
      properties = new HashMap<>(props);
    }
  }


  public Map<String, Object> getAllProperties() {
    return properties;
  }

  public void copyAllPropertiesTo(Cell<T> other) {
    other.setAllProperties(this.getAllProperties());
  }

  public void clearAllProperties() {
    properties = null;
  }
}