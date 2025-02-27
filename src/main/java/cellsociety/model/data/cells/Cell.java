package cellsociety.model.data.cells;

import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.states.State;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract generic base class representing a cell in a grid.
 *
 * @param <T> the enum type representing the cell state
 * @author Jacob You
 */
public class Cell<T extends Enum<T> & State> {

  private Map<Direction, Cell<T>> neighbors = new HashMap<>();
  private T currState;
  private T nextState;
  private Map<String, Double> properties;

  /**
   * Constructs a {@code Cell} with the specified initial state.
   *
   * @param state the initial state of the cell
   */
  public Cell(T state) {
    currState = state;
    nextState = currState;
  }

  /**
   * Returns the neighbors of this cell.
   *
   * @return a map of coordinates to neighboring cells
   */
  public Map<Direction, Cell<T>> getNeighbors() {
    return neighbors;
  }

  /**
   * Returns the current state of this cell.
   *
   * @return the current state
   */
  public T getCurrentState() {
    return currState;
  }

  /**
   * Sets the current state of this cell.
   *
   * @param state the new current state
   */
  public void setCurrentState(T state) {
    currState = state;
  }

  /**
   * Returns the next state of this cell.
   *
   * @return the next state
   */
  public T getNextState() {
    return nextState;
  }

  /**
   * Sets the neighbors of this cell.
   *
   * @param neighbors a map of coordinates to neighboring cells
   */
  public void setNeighbors(Map<Direction, Cell<T>> neighbors) {
    this.neighbors = neighbors;
  }

  /**
   * Sets the next state of this cell.
   *
   * @param nextState the state to be applied in the next update
   */
  public void setNextState(T nextState) {
    this.nextState = nextState;
  }

  /**
   * Updates this cell's current state to its next state. Should be called after all next states
   * have been determined.
   */
  public void update() {
    this.currState = this.nextState;
  }

  /**
   * Assigns or updates a property of this cell with a double value.
   *
   * @param property the name of the property
   * @param value    the double value to store
   */
  public void setProperty(String property, double value) {
    if (properties == null) {
      properties = new HashMap<>();
    }
    properties.put(property, value);
  }

  /**
   * Retrieves the value of a named property of this cell. Returns 0 if the property does not
   * exist.
   *
   * @param property the name of the property
   * @return the property value, or 0 if the property is not found
   */
  public double getProperty(String property) {
    if (properties != null && properties.containsKey(property)) {
      return properties.get(property);
    }
    return 0;
  }

  /**
   * Replaces all properties of this cell with those from the specified map.
   *
   * @param props the map of new property values; null clears all properties
   */
  public void setAllProperties(Map<String, Double> props) {
    if (props == null) {
      properties = null;
    } else {
      properties = new HashMap<>(props);
    }
  }

  /**
   * Returns all properties of this cell as a map.
   *
   * @return a map containing all properties, or null if none are set
   */
  public Map<String, Double> getAllProperties() {
    return properties;
  }

  /**
   * Copies this cell's entire property map to another cell.
   *
   * @param other the cell to which properties are copied
   */
  public void copyAllPropertiesTo(Cell<T> other) {
    other.setAllProperties(this.getAllProperties());
  }

  /**
   * Clears all properties of this cell.
   */
  public void clearAllProperties() {
    properties = null;
  }
}