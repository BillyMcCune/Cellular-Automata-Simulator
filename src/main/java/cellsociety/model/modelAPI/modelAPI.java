package cellsociety.model.modelAPI;

import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigInfo.SimulationType;
import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.logic.Logic;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import javafx.scene.paint.Color;

/**
 * @author Billy McCune
 */
public class modelAPI {

  private static final String LOGIC_PACKAGE = "cellsociety.model.logic";
  private static final String STATE_PACKAGE = "cellsociety.model.data.states";
  private static final String NEIGHBOR_PACKAGE = "cellsociety.model.data.neighbors";
  private ParameterRecord parameterRecord;
  private ConfigInfo configInfo;

  // Model
  private Grid<?> grid;
  private CellFactory<?> cellFactory;
  private Logic<?> gameLogic;
  private NeighborCalculator<?> neighborCalculator;

  // Instance variables
  private boolean isLoaded;

  //crazy color stuff:
    private static final String PROPERTY_TO_DETECT = "coloredId";
    private static final long GOLDEN_RATIO_HASH_MULTIPLIER = 2654435761L;

  // Load the color mapping from the properties file once (assumes the file is in your resources folder).
  private static final Properties COLOR_MAPPING = new Properties();

  static {
    try (InputStream in = modelAPI.class.getResourceAsStream("/cellsociety/property/CellColor.properties")) {
      COLOR_MAPPING.load(in);
    } catch (IOException ex) {
      throw new RuntimeException("error-getting-color-mapping");
    }
  }

  public modelAPI() {}

  public void setConfigInfo(ConfigInfo configInfo) {
    this.configInfo = configInfo;
    this.parameterRecord = configInfo.myParameters();
  }

  /**
   * Updates the simulation by invoking the game logic update method.
   */
  public void updateSimulation() {
    if (grid == null || gameLogic == null) {
      return;
    }
    gameLogic.update();
  }

  /**
   * Resets the simulation grid by reinitializing both the grid and game logic.
   *
   * @throws ClassNotFoundException if the required logic class cannot be found
   */
  public void resetGrid() throws ClassNotFoundException {
    if (configInfo == null) {
      return;
    }
    try {
      SimulationType type = configInfo.myType();
      String name = type.name().charAt(0) + type.name().substring(1).toLowerCase();

      // Dynamically load the Logic class.
      Class<?> logicClass = Class.forName(LOGIC_PACKAGE + "." + name + "Logic");

      // Initialize the internal grid using the configuration.
      grid = new Grid<>(configInfo.myGrid(), cellFactory, neighborCalculator);

      // Initialize the game logic instance using the grid and parameters.
      gameLogic = (Logic<?>) logicClass.getDeclaredConstructor(Grid.class, ParameterRecord.class)
          .newInstance(grid, parameterRecord);
    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
             InstantiationException | IllegalAccessException e) {
      throw new ClassNotFoundException("error-resetGrid", e);
    }
  }

  /**
   * Retrieves the double parameters from the parameter record.
   *
   * @return a Map of parameter names to their double values.
   * @throws NullPointerException if the configuration information is not loaded.
   */
  public Map<String, Double> getDoubleParameters() {
    try {
      if (parameterRecord == null) {
        parameterRecord = configInfo.myParameters();
      }
      return parameterRecord.myDoubleParameters();
    } catch (NullPointerException e) {
      throw new NullPointerException("error-configInfo-NULL");
    }
  }

  /**
   * Sets a double parameter in the simulation. This method updates the parameter record and uses
   * reflection to invoke the corresponding setter on the game logic.
   *
   * @param paramName the parameter name (e.g., "probCatch")
   * @param value     the new double value
   * @throws IllegalStateException if the game logic has not been initialized.
   */
  public void setDoubleParameter(String paramName, Double value) {
    if (gameLogic == null) {
      throw new IllegalStateException("Game logic has not been initialized.");
    }
    if (parameterRecord == null) {
      parameterRecord = configInfo.myParameters();
    }

    parameterRecord.myDoubleParameters().put(paramName, value);
    String setterName = "set" + Character.toUpperCase(paramName.charAt(0)) + paramName.substring(1);

    try {
      // Find the setter method that accepts a double.
      Method setterMethod = gameLogic.getClass().getMethod(setterName, double.class);
      // Invoke the setter on the game logic.
      setterMethod.invoke(gameLogic, value);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new NoSuchElementException(e);
    }
  }

  /**
   * Sets a string parameter in the simulation. This method updates the parameter record and uses
   * reflection to invoke the corresponding setter on the game logic.
   *
   * @param paramName the parameter name (e.g., "probCatch")
   * @param value     the new string value
   * @throws NumberFormatException if there is an error related to a null configuration.
   */
  public void setStringParameter(String paramName, String value) {
    if (parameterRecord == null) {
      parameterRecord = configInfo.myParameters();
    }
    // Update the parameter record.
    parameterRecord.myStringParameters().put(paramName, value);

    // Construct the setter method name (e.g., "setLabel" for "label").
    String setterName = "set" + Character.toUpperCase(paramName.charAt(0)) + paramName.substring(1);
    try {
      Method setterMethod = gameLogic.getClass().getMethod(setterName, String.class);
      setterMethod.invoke(gameLogic, value);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
             NullPointerException e) {
      throw new NumberFormatException("error-configInfo-NULL");
    }
  }

  /**
   * Retrieves the string parameters from the parameter record.
   *
   * @return a Map of parameter names to their string values.
   * @throws NullPointerException if the configuration information is not loaded.
   */
  public Map<String, String> getStringParameters() {
    try {
      if (parameterRecord == null) {
        parameterRecord = configInfo.myParameters();
      }
      return parameterRecord.myStringParameters();
    } catch (NullPointerException e) {
      throw new NullPointerException("error-configInfo-NULL");
    }
  }

  /**
   * Returns the color for the cell at (x, y). First, the color is determined from the cell's
   * current state. If that color is WHITE, the method will check if any of the cell’s property
   * values (if nonzero) have an associated color.
   */
  public String getCellColor(int x, int y) {
    if (x >= grid.getNumCols() || y >= grid.getNumRows()) {
      return null;
    }
    Cell<?> cell = grid.getCell(x, y);
    String stateColor = getStateColor(cell);
    if (!"WHITE".equalsIgnoreCase(stateColor)) {
      return stateColor;
    }
    String propertyColor = getPropertyColor(cell);
    return propertyColor != null ? propertyColor : stateColor;
  }

  /**
   * Determines the color from the cell's current state. It uses the cell's state (for example,
   * "AntState.EMPTY" or "FireState.BURNING") as a key in the properties file.
   */
  private String getStateColor(Cell<?> cell) {
    // Get the state value (e.g., "TREE" or "BURNING")
    String stateValue = cell.getCurrentState().toString();
    // Get the simple name of the cell state class (e.g., "FireState")
    String statePrefix = cell.getCurrentState().getClass().getSimpleName();
    // Construct the full key (e.g., "FireState.TREE")
    String key = statePrefix + "." + stateValue;
    // If the mapping is not found, default to "WHITE"
    return COLOR_MAPPING.getProperty(key, "WHITE");
  }

  /**
   * Checks the cell's properties for a non-white color override.
   * <p>
   * First, it checks if the cell has the "coloredId" property. If so, it uses the id value to
   * generate a unique color. If not, it iterates through the remaining properties using a prefix-
   * based lookup.
   *
   * @param cell the cell whose properties are checked.
   * @return the first non-white color found from the properties; returns null if none exists.
   */
  private String getPropertyColor(Cell<?> cell) {
    Map<String, Double> properties = cell.getAllProperties();

    // Check if the special "coloredId" property exists.
    if (properties.containsKey(PROPERTY_TO_DETECT) && properties.get(PROPERTY_TO_DETECT) != 0) {
      int id = properties.get(PROPERTY_TO_DETECT).intValue();
      // Convert the generated Color to a hex string.
      return toHexString(uniqueColorGenerator(id));
    }

    // Otherwise, use the state's prefix to check for any other property-based color.
    String stateString = cell.getCurrentState().toString();
    String prefix = stateString.contains(".") ? stateString.substring(0, stateString.indexOf('.')) : "";
    for (Map.Entry<String, Double> entry : properties.entrySet()) {
      if (entry.getValue() != 0) {
        // Construct key like "AntState.searchingEntities"
        String propertyKey = prefix + "." + entry.getKey();
        String color = COLOR_MAPPING.getProperty(propertyKey);
        if (color != null && !"WHITE".equalsIgnoreCase(color)) {
          return color;
        }
      }
    }
    return null;
  }

  /**
   * Converts a JavaFX Color object to a hex string (e.g., "#RRGGBB").
   *
   * @param color the JavaFX Color object.
   * @return a hex string representation of the color.
   */
  private String toHexString(Color color) {
    int r = (int) (color.getRed() * 255);
    int g = (int) (color.getGreen() * 255);
    int b = (int) (color.getBlue() * 255);
    return String.format("#%02X%02X%02X", r, g, b);
  }

  /**
   * Given a random id number, generates a random color. Uses a large prime number to ensure
   * scrambling and very few overlapping colors, while ensuring an id is the same color every single
   * time through simulations.
   *
   * @param id The id number.
   * @return A random Color based off of the id.
   */
  public static Color uniqueColorGenerator(int id) {
    long scrambled = (GOLDEN_RATIO_HASH_MULTIPLIER * id) & 0xffffffffL;
    double hue = scrambled % 360;
    // Randomly generate values for saturation and brightness, with range 0.5 - 1
    double sat = 0.5 + ((scrambled >> 8) % 50) / 100.0;
    double bright = 0.5 + ((scrambled >> 16) % 50) / 100.0;
    return Color.hsb(hue, sat, bright);
  }

  public void setCellShape(String cellShape) {
    // Not implemented.
  }

  /**
   * Resets the simulation parameters by iterating over the game logic's setter methods and updating
   * the parameter record accordingly.
   *
   * @throws InvocationTargetException if a getter or setter method throws an exception.
   * @throws IllegalAccessException    if the currently executing method does not have access.
   * @throws NoSuchMethodException     if a required getter method is not found.
   */
  public void resetParameters()
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    if (gameLogic == null) {
      throw new IllegalStateException("Game logic has not been initialized.");
    }
    if (parameterRecord == null) {
      parameterRecord = configInfo.myParameters();
    }
    Class<?> logicClass = gameLogic.getClass();
    // Iterate through each public method in the logic class.
    for (Method setterMethod : logicClass.getMethods()) {
      String methodName = setterMethod.getName();
      if (!methodName.startsWith("set") || setterMethod.getParameterCount() != 1) {
        continue;
      }
      // Convert method name to parameter name (e.g., setSpeed → speed).
      String paramName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
      Method getterMethod = logicClass.getMethod("get" + methodName.substring(3));
      Class<?> paramType = setterMethod.getParameterTypes()[0];

      if (paramType == double.class) {
        double defaultValue = (double) getterMethod.invoke(gameLogic);
        parameterRecord.myDoubleParameters().put(paramName, defaultValue);
      } else if (paramType == String.class) {
        String defaultValue = (String) getterMethod.invoke(gameLogic);
        parameterRecord.myStringParameters().put(paramName, defaultValue);
      }
    }
  }

  /**
   * Resets the simulation model by dynamically loading the appropriate classes for the logic,
   * state, and neighbor calculator, and then initializing the grid and game logic.
   *
   * @throws NoSuchMethodException if a required constructor or method is not found.
   */
  public void resetModel() throws NoSuchMethodException {
    try {
      SimulationType type = configInfo.myType();
      String name = type.name().charAt(0) + type.name().substring(1).toLowerCase();

      // Dynamically load the Logic and State classes.
      Class<?> logicClass = Class.forName(LOGIC_PACKAGE + "." + name + "Logic");
      Class<?> stateClass = Class.forName(STATE_PACKAGE + "." + name + "State");
      Class<?> neighborClass = Class.forName(NEIGHBOR_PACKAGE + "." + name + "NeighborCalculator");

      // Dynamically create cell factory, grid, and logic.
      Constructor<?> cellFactoryConstructor = CellFactory.class.getConstructor(Class.class);
      cellFactory = (CellFactory<?>) cellFactoryConstructor.newInstance(stateClass);

      Object neighborObject = neighborClass.getDeclaredConstructor().newInstance();
      neighborCalculator = (NeighborCalculator<?>) neighborObject;

      grid = new Grid<>(configInfo.myGrid(), cellFactory, neighborCalculator);
      gameLogic = (Logic<?>) logicClass.getDeclaredConstructor(Grid.class, ParameterRecord.class)
          .newInstance(grid, configInfo.myParameters());

      // Reset simulation parameters using the new backend logic.
      resetParameters();

    } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
             IllegalAccessException e) {
      throw new RuntimeException("error-resetModel", e);
    }
  }

  /**
   * Retrieves the states of the cells in the grid.
   *
   * @return a 2D list of integers representing each cell's state.
   */
  public List<List<Integer>> getCellStates() {
    List<List<Integer>> cellStates = new ArrayList<>();
    if (grid == null) {
      return cellStates;
    }
    for (int i = 0; i < grid.getNumRows(); i++) {
      List<Integer> rowStates = new ArrayList<>();
      for (int j = 0; j < grid.getNumCols(); j++) {
        Cell<?> cell = grid.getCell(i, j);
        rowStates.add(cell.getCurrentState().getValue());
      }
      cellStates.add(rowStates);
    }
    return cellStates;
  }

  /**
   * Retrieves the properties of the cells in the grid.
   *
   * @return a 2D list where each inner list contains a map of cell properties (property name to
   * value).
   */
  public List<List<Map<String, Double>>> getCellProperties() {
    List<List<Map<String, Double>>> cellProperties = new ArrayList<>();
    if (grid == null) {
      return cellProperties;
    }
    for (int i = 0; i < grid.getNumRows(); i++) {
      List<Map<String, Double>> rowProperties = new ArrayList<>();
      for (int j = 0; j < grid.getNumCols(); j++) {
        // Assuming grid.getCell returns an instance of Cell with a getAllProperties method.
        Cell<?> cell = grid.getCell(i, j);
        rowProperties.add(cell.getAllProperties());
      }
      cellProperties.add(rowProperties);
    }
    return cellProperties;
  }
}