package cellsociety.model.API;

import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigInfo.SimulationType;
import cellsociety.model.config.ConfigReader;
import cellsociety.model.config.ConfigWriter;
import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.logic.Logic;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * @author Billy McCune
 */
public class CellModelApi {

  private static final String LOGIC_PACKAGE = "cellsociety.model.logic";
  private static final String STATE_PACKAGE = "cellsociety.model.data.states";
  private static final String NEIGHBOR_PACKAGE = "cellsociety.model.data.neighbors";
  private ConfigReader configReader;
  private ConfigWriter configWriter;
  private ConfigInfo configInfo;
  private ParameterRecord parameterRecord;

  // Model
  private Grid<?> grid;
  private CellFactory<?> cellFactory;
  private Logic<?> gameLogic;
  private NeighborCalculator<?> neighborCalculator;

  // Instance variables
  private boolean isLoaded;


  /**
   * Retrieves a list of available configuration file names.
   *
   * @return a List of configuration file names.
   */
  public List<String> getFileNames() {
    ConfigReader configReader = new ConfigReader();
    return configReader.getFileNames();
  }

  /**
   * Loads a simulation configuration from the specified file.
   *
   * @param fileName the name of the configuration file to load
   * @throws ParserConfigurationException if a parser configuration error occurs
   * @throws IOException                  if an I/O error occurs
   * @throws SAXException                 if a SAX parsing error occurs - check configReader for
   *                                      more information regarding load simulation errors
   */
  public void loadSimulation(String fileName)
      throws ParserConfigurationException, IOException, SAXException {
    try {
      configInfo = configReader.readConfig(fileName);
      if (configInfo != null) {
        isLoaded = true;
        parameterRecord = configInfo.myParameters();
      }
    } catch (ParserConfigurationException e) {
      throw new ParserConfigurationException(e.getMessage());
    } catch (SAXException e) {
      throw new SAXException(e.getMessage());
    } catch (IOException e) {
      throw new IOException(e.getMessage());
    }
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

    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
//        simulationScene.setCell(i, j, grid.getCell(i, j).getCurrentState());
//        simulationScene.setParameters(i, j, grid.getCell(i, j).getCurrentState(),
        //   grid.getCell(i, j).getAllProperties());

        gameLogic.update();
      }
    }
  }


  /**
   * Saves the current simulation configuration to the specified file path
   *
   * @param FilePath the file path where the configuration should be saved
   * @return the file name of the simulation that was saved
   * @throws ParserConfigurationException if a parser configuration error occurs
   * @throws IOException                  if an I/O error occurs
   * @throws TransformerException         if an error occurs during transformation
   */
  public String saveSimulation(String FilePath)
      throws ParserConfigurationException, IOException, TransformerException {
    ConfigWriter configWriter = new ConfigWriter();
    configWriter.saveCurrentConfig(configInfo, FilePath);
    return configWriter.getLastFileSaved();
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
          .newInstance(grid, configInfo.myParameters());
    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
             InstantiationException | IllegalAccessException e) {
      throw new ClassNotFoundException("error-resetGrid", e);
    }
  }

  public Map<String, String> getSimulationStyles() {
    return new HashMap<>();
  }

  public void setSimulationStyles(Map<String, String> simulationStyles) {

  }

  public Map<String, String> getSimulationInformation() {
    if (configInfo == null) {
      return new HashMap<>();
    }
    HashMap<String, String> simulationDetails = new HashMap<>();
    simulationDetails.put("author", configInfo.myAuthor());
    simulationDetails.put("title", configInfo.myTitle());
    simulationDetails.put("type", configInfo.myType().toString());
    simulationDetails.put("description", configInfo.myDescription());

    return simulationDetails;
  }


  /**
   * Retrieves the accepted simulation states
   *
   * @return a of accepted state integers
   * @throws NullPointerException if the configuration information is not loaded
   */
  public Set<Integer> getAcceptedStates() {
    try {
      return configInfo.acceptedStates();
    } catch (NullPointerException e) {
      throw new NullPointerException("error-configInfo-NULL");
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


  /**
   * Retrieves the grid width from the configuration
   *
   * @return the grid width
   * @throws NumberFormatException if the configuration information is not loaded
   */
  public int getGridWidth() {
    return 0;
  }

  public int getGridHeight() {
    return 0;
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





      Object neighborObject = neighborClass.getDeclaredConstructor().newInstance();
      neighborCalculator = (NeighborCalculator<?>) neighborObject;




}
