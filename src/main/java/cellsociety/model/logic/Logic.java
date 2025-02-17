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
 */
public abstract class Logic<T extends Enum<T> & State> {

  protected final Grid<T> grid;
  private final ParameterRecord parameters;
  private final String logicClassName;
  private static final Properties logicProps = new Properties();
  private static final String propertyFile = "/cellsociety/property/Parameters.properties";

  static {
    try (InputStream is = Logic.class.getResourceAsStream(propertyFile)) {
      if (is == null) {
        System.err.println("Warning: Could not find '/cellsociety/property/Parameters.properties' in resources.");
      } else {
        logicProps.load(is);
      }
    } catch (IOException e) {
      System.err.println("Error loading parameters properties file: " + e);
    }
  }

  public Logic(Grid<T> grid, ParameterRecord parameters) {
    this.grid = grid;
    this.parameters = parameters;
    this.logicClassName = this.getClass().getSimpleName();;
  }

  /**
   * Reads a double parameter from the Parameters.properties file.
   * If the parameter is missing, throws an IllegalArgumentException.
   */
  protected double loadDoubleProperty(String key) throws IllegalArgumentException {
    String valString = logicProps.getProperty(key);
    if (valString == null) {
      throw new IllegalArgumentException();
    }
    return Double.parseDouble(valString.trim());
  }

  /**
   * Reads a String parameter from the ParameterRecord.
   * If the parameter is missing, throws an IllegalArgumentException.
   */
  protected String loadStringProperty(String key) throws IllegalArgumentException {
    String val = logicProps.getProperty(key);
    if (val == null) {
      throw new IllegalArgumentException();
    }
    return val.trim();
  }

  /**
   * Checks if a value is less than or greater than a min or max
   * If it is, throws an IllegalArgumentException
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

  public double getMinParam(String paramName) {
    String key = logicClassName + "." + paramName + ".min";
    return loadDoubleProperty(key);
  }

  public double getMaxParam(String paramName) {
    String key = logicClassName + "." + paramName + ".max";
    return loadDoubleProperty(key);
  }

  /**
   * Updates the entire game state by one tick. Subclasses should provide an implementation for this
   * method.
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
