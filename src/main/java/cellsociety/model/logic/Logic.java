package cellsociety.model.logic;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.states.State;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Abstract superclass responsible for managing the logic of a cellular automaton. Subclasses should
 * implement specific rules.
 *
 * @param <T> The enum type representing the cell state
 * @author Jacob You
 */
public abstract class Logic<T extends Enum<T> & State> {

  protected final Grid<T> grid;
  private final ParameterRecord parameters;
  private final String logicClassName;
  private static final Properties logicProps = new Properties();
  private static final String propertyFile = "/cellsociety/property/Parameters.properties";

  static {
    try (InputStream is = Logic.class.getResourceAsStream(propertyFile)) {
      logicProps.load(is);
    } catch (IOException e) {
      System.err.println("Error loading parameters properties file: " + e);
    }
  }

  /**
   * Constructs a Logic instance associated with a given grid and parameter set.
   *
   * @param grid       The grid representing the cellular automaton
   * @param parameters The parameter record containing simulation-specific configurations
   */
  public Logic(Grid<T> grid, ParameterRecord parameters) {
    this.grid = grid;
    this.parameters = parameters;
    this.logicClassName = this.getClass().getSimpleName();
  }

  /**
   * Retrieves a double parameter from the ParameterRecord if available; otherwise, falls back to
   * the default value stored in the Parameters.properties file.
   *
   * @param paramRecordKey The key used to look up the parameter in the ParameterRecord
   * @return The parameter value, either from the ParameterRecord or the properties file
   * @throws IllegalArgumentException If the parameter is missing from both sources
   */
  protected double getDoubleParamOrFallback(String paramRecordKey) throws IllegalArgumentException {
    Double recordVal = parameters.myDoubleParameters().get(paramRecordKey);
    if (recordVal != null) {
      return recordVal;
    }
    String defaultKey = logicClassName + "." + paramRecordKey + ".default";
    return loadDoubleProperty(defaultKey);
  }

  /**
   * Retrieves a string parameter from the ParameterRecord if available; otherwise, falls back to
   * the default value stored in the Parameters.properties file.
   *
   * @param paramRecordKey The key used to look up the parameter in the ParameterRecord
   * @return The parameter value, either from the ParameterRecord or the properties file
   * @throws IllegalArgumentException If the parameter is missing from both sources
   */
  protected String getStringParamOrFallback(String paramRecordKey) throws IllegalArgumentException {
    String recordVal = parameters.myStringParameters().get(paramRecordKey);
    if (recordVal != null) {
      return recordVal;
    }
    String defaultKey = logicClassName + "." + paramRecordKey + ".default";
    return loadStringProperty(defaultKey);
  }

  /**
   * Reads a double parameter from the Parameters.properties file. If the parameter is missing,
   * throws an IllegalArgumentException.
   */
  protected double loadDoubleProperty(String key) throws IllegalArgumentException {
    String valString = logicProps.getProperty(key);
    if (valString == null) {
      throw new IllegalArgumentException();
    }
    return Double.parseDouble(valString.trim());
  }

  /**
   * Reads a String parameter from the ParameterRecord. If the parameter is missing, throws an
   * IllegalArgumentException.
   */
  protected String loadStringProperty(String key) throws IllegalArgumentException {
    String val = logicProps.getProperty(key);
    if (val == null) {
      throw new IllegalArgumentException();
    }
    return val.trim();
  }

  /**
   * Checks if a value is less than or greater than a min or max If it is, throws an
   * IllegalArgumentException
   *
   * @param value The value to analyze
   * @param min   The minimum value
   * @param max   The maximum value
   */
  protected void checkBounds(double value, double min, double max) throws IllegalArgumentException {
    if (value < min || value > max) {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Retrieves the minimum allowed value for a given parameter.
   *
   * @param paramName The name of the parameter
   * @return The minimum allowed value of the parameter
   */
  public double getMinParam(String paramName) {
    String key = logicClassName + "." + paramName + ".min";
    return loadDoubleProperty(key);
  }

  /**
   * Retrieves the maximum allowed value for a given parameter.
   *
   * @param paramName The name of the parameter
   * @return The maximum allowed value of the parameter
   */
  public double getMaxParam(String paramName) {
    String key = logicClassName + "." + paramName + ".max";
    return loadDoubleProperty(key);
  }

  /**
   * Updates the entire game state by one tick, applying the logic to each cell. The grid is updated
   * after all cells have processed their next states.
   */
  public void update() {
    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();

    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        Cell<T> cell = grid.getCell(row, col);
        updateSingleCell(cell);
      }
    }
    grid.updateGrid();
  }

  protected abstract void updateSingleCell(Cell<T> cell);
}
