package cellsociety.model.modelAPI;

import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigInfo.SimulationType;
import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.logic.Logic;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Billy McCune
 */
//TODO figure out config not loaded error code stuff
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


  public void setConfiginfo(ConfigInfo configInfo) {
    this.configInfo = configInfo;
    this.parameterRecord = configInfo.myParameters();
  }

  /**
   * Updates the simulation by invoking the game logic update method.
   * <p>
   * Note: This method currently iterates over the grid cells and calls {@code gameLogic.update()}.
   * Any view-related code has been commented out.
   * </p>
   */
  public void updateSimulation() {
    if (grid == null || gameLogic == null) {
      return;
    }
        gameLogic.update();
  }


  /**
   * Resets the simulation grid by reinitializing both the grid and game logic
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

      //dynamically load the Logic class
      Class<?> logicClass = Class.forName(LOGIC_PACKAGE + "." + name + "Logic");

      //initialize the internal grid using the configuration
      grid = new Grid<>(configInfo.myGrid(), cellFactory, neighborCalculator);

      //initialize the game logic instance using the grid and parameters
      gameLogic = (Logic<?>) logicClass.getDeclaredConstructor(Grid.class, ParameterRecord.class)
          .newInstance(grid, parameterRecord);
    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
             InstantiationException | IllegalAccessException e) {
      throw new ClassNotFoundException("error-resetGrid", e);
    }
  }

  /**
   * Retrieves the double parameters from the parameter record
   *
   * @return a Map of parameter names to their double values
   * @throws NullPointerException if the configuration information is not loaded
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
   * Sets a double parameter in the simulation
   * <p>
   * This method updates the parameter record and uses reflection to invoke the corresponding setter
   * on the game logic
   * </p>
   *
   * @param paramName the parameter name (e.g., "probCatch")
   * @param value     the new double value
   * @throws IllegalStateException if the game logic has not been initialized
   */
  //TODO implement error messages
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
      //find the setter method that accepts a double
      Method setterMethod = gameLogic.getClass().getMethod(setterName, double.class);
      //invoke the setter on the game logic
      setterMethod.invoke(gameLogic, value);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      System.err.println("Failed to set double parameter '" + paramName + "': " + e.getMessage());
    }
  }


  /**
   * Sets a string parameter in the simulation.
   * <p>
   * This method updates the parameter record and uses reflection to invoke the corresponding setter
   * on the game logic
   * </p>
   *
   * @param paramName the parameter name (e.g., "probCatch")
   * @param value     the new string value
   * @throws NumberFormatException if there is an error related to a null configuration
   */
  public void setStringParameter(String paramName, String value) {
    if (parameterRecord == null) {
      parameterRecord = configInfo.myParameters();
    }
    //update the parameter record
    parameterRecord.myStringParameters().put(paramName, value);

    //construct the setter method name (e.g., "setLabel" for "label")
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


  public String getCellColor(int row, int col) {
    return null;
  }

  public String getCellShape() {
    return null;
  }

  public void setCellShape(String cellShape) {

  }

  public <T extends Logic<?>> void resetParameters(Class<T> logicClass)
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    if (gameLogic == null) {
      throw new IllegalStateException("Game logic has not been initialized.");
    }
    if (parameterRecord == null) {
      parameterRecord = configInfo.myParameters();
    }
    //iterate through each public method in the logic class
    for (Method setterMethod : logicClass.getMethods()) {
      String methodName = setterMethod.getName();
      if (!methodName.startsWith("set") || setterMethod.getParameterCount() != 1) {
        continue;
      }
      //convert method name to parameter name (e.g., setSpeed → speed)
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


  public void resetModel() throws NoSuchMethodException {
    if (configInfo == null) {
      return;
    }

    try {
      SimulationType type = configInfo.myType();
      String name = type.name().charAt(0) + type.name().substring(1).toLowerCase();

      //dynamically load the Logic and State classes
      Class<?> logicClass = Class.forName(LOGIC_PACKAGE + "." + name + "Logic");
      Class<?> stateClass = Class.forName(STATE_PACKAGE + "." + name + "State");
      Class<?> neighborClass = Class.forName(NEIGHBOR_PACKAGE + "." + name + "NeighborCalculator");

      //dynamically create cell factory, grid, and logic
      Constructor<?> cellFactoryConstructor = CellFactory.class.getConstructor(Class.class);
      cellFactory = (CellFactory<?>) cellFactoryConstructor.newInstance(stateClass);

      Object neighborObject = neighborClass.getDeclaredConstructor().newInstance();
      neighborCalculator = (NeighborCalculator<?>) neighborObject;

      grid = new Grid<>(configInfo.myGrid(), cellFactory, neighborCalculator);
      gameLogic = (Logic<?>) logicClass.getDeclaredConstructor(Grid.class, ParameterRecord.class)
          .newInstance(grid, configInfo.myParameters());

      //set the parameters for the simulation
      resetParameters(gameLogic.getClass());

    } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
             IllegalAccessException e) {
      throw new RuntimeException("error-resetModel");
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
   * @return a 2D list where each inner list contains a map of cell properties (property name to value).
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
