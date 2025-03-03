package cellsociety.model.modelAPI;

import cellsociety.model.config.CellRecord;
import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigInfo.SimulationType;
import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.logic.Logic;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * @author Billy McCune
 */
public class modelAPI {

  private static final String LOGIC_PACKAGE = "cellsociety.model.logic";
  private static final String STATE_PACKAGE = "cellsociety.model.data.states";
  private static final String NEIGHBOR_PACKAGE = "cellsociety.model.data.neighbors";
  private ParameterRecord myParameterRecord;
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

  //Style Property names:
  private final String gridOutlineProperty = "";
  private final String edgePolicyProperty = "";
  private final String neighborArrangementProperty = "";
  private final String cellShapeProperty = "";


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
    this.myParameterRecord = configInfo.myParameters();
  }
  private static final Properties USER_STYLE_PREFERENCES = new Properties();

  static {
    try (InputStream in = modelAPI.class.getResourceAsStream("/cellsociety/property/SimulationStyle.properties")) {
      USER_STYLE_PREFERENCES.load(in);
    } catch (IOException ex) {
      throw new RuntimeException("error-getting-user-defined-color-mapping");
    }
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
      List<List<CellRecord>> gridCopy = deepCopyGrid(configInfo.myGrid());

      grid = new Grid<>(gridCopy, cellFactory, neighborCalculator);
      System.out.println(configInfo.myGrid());
      // Initialize the game logic instance using the grid and parameters.
      gameLogic = (Logic<?>) logicClass.getDeclaredConstructor(Grid.class, ParameterRecord.class)
          .newInstance(grid, myParameterRecord);
    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
             InstantiationException | IllegalAccessException e) {
      throw new ClassNotFoundException("error-resetGrid", e);
    }
  }

  private List<List<CellRecord>> deepCopyGrid(List<List<CellRecord>> grid) {
    List<List<CellRecord>> copy = new ArrayList<>();
    for (List<CellRecord> row : grid) {
      copy.add(new ArrayList<>(row));
    }
    return copy;
  }

  /**
   * Retrieves the double parameters from the parameter record.
   *
   * @return a Map of parameter names to their double values.
   * @throws NullPointerException if the configuration information is not loaded.
   */
  public Map<String, Double> getDoubleParameters() {
    try {
      if (myParameterRecord == null) {
        myParameterRecord = configInfo.myParameters();
      }
      System.out.println(myParameterRecord.myDoubleParameters());
      return myParameterRecord.myDoubleParameters();
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
    if (myParameterRecord == null) {
      myParameterRecord = configInfo.myParameters();
    }

    myParameterRecord.myDoubleParameters().put(paramName, value);
    String setterName = "set" + Character.toUpperCase(paramName.charAt(0)) + paramName.substring(1);
    System.out.println(setterName);
    try {
      // Find the setter method that accepts a double.
      Method setterMethod = gameLogic.getClass().getMethod(setterName, double.class);
      // Invoke the setter on the game logic.
      System.out.println(value);
      setterMethod.invoke(gameLogic, value);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      System.out.println("error with setter");
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
    if (myParameterRecord == null) {
      myParameterRecord = configInfo.myParameters();
    }
    // Update the parameter record.
    myParameterRecord.myStringParameters().put(paramName, value);

    // Construct the setter method name (e.g., "setLabel" for "label").
    String setterName = "set" + Character.toUpperCase(paramName.charAt(0)) + paramName.substring(1);
    System.out.println(setterName);
    try {
      Method setterMethod = gameLogic.getClass().getMethod(setterName, String.class);
      setterMethod.invoke(gameLogic, value);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
             NullPointerException e) {
      System.out.println("error with setter");
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
      if (myParameterRecord == null) {
        myParameterRecord = configInfo.myParameters();
      }
      return myParameterRecord.myStringParameters();
    } catch (NullPointerException e) {
      throw new NullPointerException("error-configInfo-NULL");
    }
  }

  /**
   * Returns the color for the cell at (row, col). First, the color is determined from the cell's
   * current state. If that color is WHITE, the method will check if any of the cell’s property
   * values (if nonzero) have an associated color.
   */
  public String getCellColor(int row, int col, boolean wantDefaultColor) {
    if (col >= grid.getNumCols() || row >= grid.getNumRows()) {
      return null;
    }
    Cell<?> cell = grid.getCell(row, col);
    String stateColor = getStateColor(cell,wantDefaultColor);
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
  private String getStateColor(Cell<?> cell, boolean wantDefaultColor) {
    // Get the state value (e.g., "TREE" or "BURNING")
    String stateValue = cell.getCurrentState().toString();
    // Get the simple name of the cell state class (e.g., "FireState")
    String statePrefix = cell.getCurrentState().getClass().getSimpleName();
    // Construct the full key (e.g., "FireState.TREE")
    String key = statePrefix + "." + stateValue;
    // If the mapping is not found, default to "WHITE"
    if (!wantDefaultColor) {
      return USER_STYLE_PREFERENCES.getProperty(key, "WHITE");
    }
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
      return uniqueColorGenerator(id);
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
   * Given a random id number, generates a random color. Uses a large prime number to ensure
   * scrambling and very few overlapping colors, while ensuring an id is the same color every single
   * time through simulations.
   *
   * @param id The id number.
   * @return A random Color based off of the id.
   */
  public static String uniqueColorGenerator(int id) {
    long scrambled = (GOLDEN_RATIO_HASH_MULTIPLIER * id) & 0xffffffffL;

    // Convert to HSB
    float hue = (scrambled % 360) / 360f;
    float sat = 0.5f + ((scrambled >> 8) % 50) / 100f;
    float bright = 0.5f + ((scrambled >> 16) % 50) / 100f;

    // Convert to RGB
    int rgb = java.awt.Color.HSBtoRGB(hue, sat, bright);
    int r = (rgb >> 16) & 0xFF;
    int g = (rgb >> 8) & 0xFF;
    int b = rgb & 0xFF;

    // Return the color as a hex string
    return String.format("#%02X%02X%02X", r, g, b);
  }


  /**
   * Resets the simulation parameters by iterating over the public setter methods
   * of the currently loaded gameLogic. For each setter, the corresponding getter,
   * getMinParam, and getMaxParam methods are used to obtain the default and bounds values,
   * which are then stored in the parameter record.
   *
   * @throws IllegalArgumentException if the game logic is not initialized.
   * @throws NullPointerException     if the game logic is not initialized.
   * @throws IllegalStateException    if the game logic is not initialized.
   * @throws NoSuchMethodException    if a required method is not found.
   */
  public void resetParameters()
      throws IllegalArgumentException, NullPointerException, IllegalStateException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    if (gameLogic == null) {
      throw new IllegalStateException("Game logic is not initialized.");
    }
    Class<?> logicClass = gameLogic.getClass();
    for (Method setterMethod : logicClass.getMethods()) {
      String methodName = setterMethod.getName();
      // Only consider public setter methods that start with "set" and take one parameter.
      if (!methodName.startsWith("set") || setterMethod.getParameterCount() != 1) {
        continue;
      }
      // Derive the parameter name from the setter method.
      String paramName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);

      // Retrieve the corresponding getter method.
      Method getterMethod = logicClass.getMethod("get" + methodName.substring(3));
      Class<?> paramType = setterMethod.getParameterTypes()[0];

      if (paramType == double.class) {
        double defaultValue = (double) getterMethod.invoke(gameLogic);
        // Update the parameter record for double parameters.
        myParameterRecord.myDoubleParameters().put(paramName, defaultValue);
        System.out.println(myParameterRecord.myDoubleParameters());
      } else if (paramType == String.class) {
        String defaultValue = (String) getterMethod.invoke(gameLogic);
        // Update the parameter record for string parameters.
        myParameterRecord.myStringParameters().put(paramName, defaultValue);
        System.out.println(myParameterRecord.myStringParameters());
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

  /**
   * Returns a Consumer that will invoke the appropriate setter for a double parameter.
   *
   * @param paramName the name of the parameter (e.g., "probCatch")
   * @return a Consumer<Double> that calls setDoubleParameter(paramName, newValue)
   * @throws NoSuchElementException if the corresponding setter is not found.
   */
  public Consumer<Double> getDoubleParameterConsumer(String paramName) {
    String setterName = "set" + Character.toUpperCase(paramName.charAt(0)) + paramName.substring(1);
    try {
      Method setterMethod = gameLogic.getClass().getMethod(setterName, double.class);
      return newVal -> {
        try {
          setterMethod.invoke(gameLogic, newVal);
        } catch (InvocationTargetException e) {
          throw new RuntimeException("error-invalidParameterMessage");
        } catch (IllegalAccessException e) {
          throw new RuntimeException("error-setParameter");
        }};
    } catch (NoSuchMethodException e) {
      throw new NoSuchElementException("error-invalidParameterMessage");
    }
  }

  /**
   * Returns a Consumer that will invoke the appropriate setter for a String parameter.
   *
   * @param paramName the name of the parameter
   * @return a Consumer<String> that calls setStringParameter(paramName, newValue)
   * @throws NoSuchElementException if the corresponding setter is not found.
   */
  public Consumer<String> getStringParameterConsumer(String paramName) {
    String setterName = "set" + Character.toUpperCase(paramName.charAt(0)) + paramName.substring(1);
    try {
      Method setterMethod = gameLogic.getClass().getMethod(setterName, String.class);
      return newVal -> {
        try {
          setterMethod.invoke(gameLogic, newVal);

        } catch (InvocationTargetException e) {
          throw new RuntimeException("error-invalidParameterMessage");
        } catch (IllegalAccessException e) {
          throw new RuntimeException("error-setParameter");
        }
      };
    } catch (NoSuchMethodException e) {
      throw new NoSuchElementException("error-invalidParameterMessage");
    }
  }

  /**
   * Returns the minimum and maximum bounds for a given parameter.
   *
   * @param paramName the name of the parameter (e.g., "probCatch")
   * @return a double array where index 0 is the minimum value and index 1 is the maximum value
   * @throws NoSuchMethodException if the corresponding getter is not found.
   * @throws InvocationTargetException if the method cannot be invoked.
   * @throws IllegalAccessException if the method cannot be accessed.
   */
  public double[] getParameterBounds(String paramName)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    if (gameLogic == null) {
      throw new IllegalStateException("Game logic is not initialized.");
    }
    Class<?> logicClass = gameLogic.getClass();
    Method minMethod = logicClass.getMethod("getMinParam", String.class);
    Method maxMethod = logicClass.getMethod("getMaxParam", String.class);
    double min = (double) minMethod.invoke(gameLogic, paramName);
    double max = (double) maxMethod.invoke(gameLogic, paramName);
    return new double[]{ min, max };
  }

  public Map<String,String> getCellTypesAndDefaultColors(String SimulationType) {
    Map<String,String> possibleStates = new HashMap<>();
    try (InputStream input = new FileInputStream("CellColor.properties")) {
      Properties defaultColors = new Properties();
      defaultColors.load(input);
      for (String key : defaultColors.stringPropertyNames()) {
        possibleStates.put(key, defaultColors.getProperty(key));
      }
    } catch (IOException e) {
      System.err.println("Error reading default cell colors: " + e.getMessage());
    }
    return possibleStates;
  }

  public void setNewColorPreference(String stateName, String newColor) {
    try {
      Properties simulationStyle = new Properties();
      File file = new File("SimulationStyle.properties");
      if (file.exists()) {
        try (InputStream input = new FileInputStream(file)) {
          simulationStyle.load(input);
        }
      }
      simulationStyle.setProperty(stateName, newColor);
      try (OutputStream output = new FileOutputStream(file)) {
        simulationStyle.store(output, "User-defined cell colors");
      }
    } catch (IOException e) {
      System.err.println("Error saving new color preference: " + e.getMessage());
    }
  }

  public String getColorFromPreferences(String stateName) {
    try (InputStream input = new FileInputStream("SimulationStyle.properties")) {
      Properties simulationStyle = new Properties();
      simulationStyle.load(input);
      return simulationStyle.getProperty(stateName, getDefaultColorByState(stateName));
    } catch (IOException e) {
      System.err.println("Error reading simulation style: " + e.getMessage());
      return getDefaultColorByState(stateName);
    }
  }

  public String getDefaultColorByState(String stateName) {
    try (InputStream input = new FileInputStream("CellColor.properties")) {
      Properties defaultColors = new Properties();
      defaultColors.load(input);
      return defaultColors.getProperty(stateName, "WHITE"); // fallback to WHITE
    } catch (IOException e) {
      System.err.println("Error reading default color for " + stateName + ": " + e.getMessage());
      return "WHITE";
    }
}
  public void setNeighborArrangement(String neighborArrangement) {
    try {
      Properties simulationStyle = new Properties();
      File file = new File("SimulationStyle.properties");
      if (file.exists()) {
        try (InputStream input = new FileInputStream(file)) {
          simulationStyle.load(input);
        }
      }
      simulationStyle.setProperty(neighborArrangementProperty, neighborArrangement);
      try (OutputStream output = new FileOutputStream(file)) {
        simulationStyle.store(output, "User-defined cell colors");
      }
    } catch (IOException e) {
      System.err.println("Error saving new color preference: " + e.getMessage());
    }
  }
  public List<String> getPossibleNeighborArrangements(){
    return null;
  }

  public void setEdgePolicy(String edgePolicy){
    try {
      Properties simulationStyle = new Properties();
      File file = new File("SimulationStyle.properties");
      if (file.exists()) {
        try (InputStream input = new FileInputStream(file)) {
          simulationStyle.load(input);
        }
      }
      simulationStyle.setProperty(edgePolicyProperty, edgePolicy);
      try (OutputStream output = new FileOutputStream(file)) {
        simulationStyle.store(output, "User-defined cell colors");
      }
    } catch (IOException e) {
      System.err.println("Error saving new color preference: " + e.getMessage());
    }
  }

  public List<String> getPossibleEdgePolicies(){
      return null;
  }

  public void setCellShape(String cellShape){
    try {
      Properties simulationStyle = new Properties();
      File file = new File("SimulationStyle.properties");
      if (file.exists()) {
        try (InputStream input = new FileInputStream(file)) {
          simulationStyle.load(input);
        }
      }
      simulationStyle.setProperty(cellShapeProperty, cellShape);
      try (OutputStream output = new FileOutputStream(file)) {
        simulationStyle.store(output, "User-defined cell colors");
      }
    } catch (IOException e) {
      System.err.println("Error saving new color preference: " + e.getMessage());
    }
  }

  public List<String> getPossibleCellShapes(){
    return null;
  }

  public void setGridOutlinePreference(boolean wantsGridOutline){
    try {
      Properties simulationStyle = new Properties();
      File file = new File("SimulationStyle.properties");
      if (file.exists()) {
        try (InputStream input = new FileInputStream(file)) {
          simulationStyle.load(input);
        }
      }
      simulationStyle.setProperty(gridOutlineProperty, String.valueOf(wantsGridOutline));
      try (OutputStream output = new FileOutputStream(file)) {
        simulationStyle.store(output, "User-defined cell colors");
      }
    } catch (IOException e) {
      System.err.println("Error saving new color preference: " + e.getMessage());
    }
  }

  public boolean getGridOutlinePreference(){
      return false;
  }

}

