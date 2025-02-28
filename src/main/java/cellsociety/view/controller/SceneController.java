package cellsociety.view.controller;

import cellsociety.model.config.CellRecord;
import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigInfo.SimulationType;
import cellsociety.model.config.ConfigReader;
import cellsociety.model.config.ConfigWriter;
import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.logic.Logic;
import cellsociety.view.scene.SceneUIWidget;
import cellsociety.view.scene.SimulationScene;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The SceneController class is responsible for controlling the flow of the application and managing the different scenes.
 * It communicates with the ConfigIO parts and the main Model Logics.
 *
 * @author Hsuan-Kai Liao
 */
public class SceneController {
  // Constant Package Paths
  private static final String LOGIC_PACKAGE = "cellsociety.model.logic";
  private static final String STATE_PACKAGE = "cellsociety.model.data.states";
  private static final String NEIGHBOR_PACKAGE = "cellsociety.model.data.neighbors";

  // ConfigIO
  private final ConfigReader configReader;
  private final ConfigWriter configWriter;
  private ConfigInfo configInfo;

  // Scene
  private final SimulationScene simulationScene;

  // Model
  private Grid<?> grid;
  private CellFactory<?> cellFactory;
  private Logic<?> gameLogic;
  private NeighborCalculator<?> neighborCalculator;

  // Instance variables
  private boolean isLoaded;
  private boolean isPaused;

  /**
   * Constructor for the SceneController class.
   * @param scene The simulation scene to control
   */
  public SceneController(SimulationScene scene) {
    // Initialize
    this.configReader = new ConfigReader();
    this.configWriter = new ConfigWriter();
    this.simulationScene = scene;
    this.isPaused = true;
  }

  /**
   * Update the simulation scene with the latest data from the model.
   */
  public void update() {
    if (grid == null || gameLogic == null) {
      return;
    }

    if (!isPaused) {
      for (int i = 0; i < grid.getNumRows(); i++) {
        for (int j = 0; j < grid.getNumCols(); j++) {
          simulationScene.setCell(grid.getNumCols(), i, j, grid.getCell(i, j).getCurrentState());
          simulationScene.setParameters(i, j, grid.getCell(i, j).getCurrentState(), grid.getCell(i, j).getAllProperties());
        }
      }

      gameLogic.update();
    }
  }

  /* CONFIG IO APIS */

  /**
   * Load the configuration file with the given filename.
   * @param filename The name of the configuration file to load
   */
  public void loadConfig(String filename) {
    try {
      configInfo = configReader.readConfig(filename);
      if (configInfo != null) {
        isLoaded = true;
      }
    } catch (Exception e) {
      SceneUIWidget.createErrorDialog(LanguageController.getStringProperty("error-loadConfig").getValue(), e.getMessage(), e);
    }
  }

  /**
   * Write the current configuration to a file with the given filename.
   * @param path The path to save the configuration file
   */
  public void saveConfig(String path) {
    if (configInfo == null) {
      return;
    }

    try {
      ConfigInfo savedConfigInfo = saveConfigInfo();
      configWriter.saveCurrentConfig(savedConfigInfo, path);
      getAllConfigFileNames();
      String message = String.format(LanguageController.getStringProperty("success-saveConfigMessage").getValue(), path);
      SceneUIWidget.createSuccessSaveDialog(LanguageController.getStringProperty("success-saveConfigTitle").getValue(), message, configWriter.getLastFileSaved());
    } catch (Exception e) {
      SceneUIWidget.createErrorDialog(LanguageController.getStringProperty("error-saveConfig").getValue(), e.getMessage(), e);
    }
  }

  /**
   * Get a list of all the configuration file names.
   * @return A list of all the configuration file names
   */
  public List<String> getAllConfigFileNames() {
    return configReader.getFileNames();
  }

  /**
   * Get the information about the current configuration.
   * @return A string containing the information about the current configuration
   */
  public String getConfigInformation() {
    if (configInfo == null) {
      return "";
    }

    return String.format(
        "Author: %s\nTitle: %s\nType: %s\nDescription: %s",
        configInfo.myAuthor(),
        configInfo.myTitle(),
        configInfo.myType(),
        configInfo.myDescription()
    );
  }

  /**
   * Get the title of the current configuration.
   * @return The title of the current configuration
   */
  public String getConfigTitle() {
    if (configInfo == null) {
      return "";
    }

    return configInfo.myTitle();
  }

  /**
   * Get the speed of the current configuration.
   * @return The speed of the current configuration
   */
  public double getConfigSpeed() {
    if (configInfo == null) {
      return 0;
    }

    return configInfo.myTickSpeed();
  }

  /* MODEL APIS */

  /**
   * Reset the model with the current configuration.
   */
  public void resetModel() {
    if (configInfo == null) {
      return;
    }

    try {
      SimulationType type = configInfo.myType();
      String name = type.name().charAt(0) + type.name().substring(1).toLowerCase();

      // Dynamically load the Logic and State classes
      Class<?> logicClass = Class.forName(LOGIC_PACKAGE + "." + name + "Logic");
      Class<?> stateClass = Class.forName(STATE_PACKAGE + "." + name + "State");
      Class<?> neighborClass = Class.forName(NEIGHBOR_PACKAGE + "." + name + "NeighborCalculator");

      // Dynamically create cell factory, grid, and logic
      Constructor<?> cellFactoryConstructor = CellFactory.class.getConstructor(Class.class);
      cellFactory = (CellFactory<?>) cellFactoryConstructor.newInstance(stateClass);

      Object neighborObject = neighborClass.getDeclaredConstructor().newInstance();
      neighborCalculator = (NeighborCalculator<?>) neighborObject;

      grid = new Grid<>(configInfo.myGrid(), cellFactory, neighborCalculator);
      gameLogic = (Logic<?>) logicClass.getDeclaredConstructor(Grid.class, ParameterRecord.class).newInstance(grid, configInfo.myParameters());

      // Set the parameters for the simulation
      resetParameters(gameLogic.getClass());

      // Set the grid to the scene
      initGrid();
    } catch (Exception e) {
      SceneUIWidget.createErrorDialog(LanguageController.getStringProperty("error-resetModel").getValue(), e.getMessage(), e);
    }
  }

  /**
   * Reset the grid while keep the current parameter configuration.
   */
  public void resetGrid() {
    if (configInfo == null) {
      return;
    }

    try {
      SimulationType type = configInfo.myType();
      String name = type.name().charAt(0) + type.name().substring(1).toLowerCase();

      // Dynamically load the Logic class
      Class<?> logicClass = Class.forName(LOGIC_PACKAGE + "." + name + "Logic");

      // Dynamically create cell grid, and logic
      grid = new Grid<>(configInfo.myGrid(), cellFactory, neighborCalculator);
      gameLogic = (Logic<?>) logicClass.getDeclaredConstructor(Grid.class, ParameterRecord.class).newInstance(grid, configInfo.myParameters());

      // Set the grid to the scene
      initGrid();
    } catch (Exception e) {
      SceneUIWidget.createErrorDialog(LanguageController.getStringProperty("error-resetGrid").getValue(), e.getMessage(), e);
    }
  }

  /**
   * Reset the parameters for the simulation.
   *
   * @param logicClass The logic class to reset the parameters for
   * @param <T> The type of the logic class
   */
  public <T extends Logic<?>> void resetParameters(Class<T> logicClass) throws Exception{
    // Iterate over all public methods in the class
    for (Method setterMethod : logicClass.getMethods()) {
      String methodName = setterMethod.getName();

      // Check if the method starts with "set" and has exactly one parameter
      if (!methodName.startsWith("set") || setterMethod.getParameterCount() != 1) {
        continue;
      }

      // Get the parameter type
      Class<?> paramType = setterMethod.getParameterTypes()[0];

      // Convert method name to parameter name (e.g., setSpeed → speed)
      String paramName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);

      // Get the getter method for the parameter initialization
      Method getterMethod = logicClass.getMethod("get" + methodName.substring(3));
      Method minMethod = logicClass.getMethod("getMinParam", String.class);
      Method maxMethod = logicClass.getMethod("getMaxParam", String.class);

      // Set the parameter listener
      if (paramType == double.class) {
        // Get the default and minmax values
        double min = (double) minMethod.invoke(gameLogic, paramName);
        double max = (double) maxMethod.invoke(gameLogic, paramName);
        double defaultValue = (double) getterMethod.invoke(gameLogic);

        // Create a consumer for UI updates
        Consumer<Double> consumer = v -> {
          try {
            setterMethod.invoke(gameLogic, v);
          } catch (Exception ex) {
            String message = String.format(LanguageController.getStringProperty("error-invalidParameterMessage").getValue(), paramName);
            SceneUIWidget.createErrorDialog(LanguageController.getStringProperty("error-setParameter").getValue(), message, ex);
          }
        };

        // Register parameter in simulation UI
        simulationScene.setParameter(min, max, defaultValue, paramName + "-label", paramName + "-tooltip", consumer);
      } else if (paramType == String.class) {
        // Get the default value
        String defaultValue = (String) getterMethod.invoke(gameLogic);

        // Create a consumer for UI updates
        Consumer<String> consumer = v -> {
          try {
            setterMethod.invoke(gameLogic, v);
          } catch (Exception ex) {
            String message = String.format(LanguageController.getStringProperty("error-invalidParameterMessage").getValue(), paramName);
            SceneUIWidget.createErrorDialog(LanguageController.getStringProperty("error-setParameter").getValue(), message, ex);
          }
        };

        // Register parameter in simulation UI
        simulationScene.setParameter(defaultValue, paramName + "-label", paramName + "-tooltip", consumer);
      }
    }
  }

  /* CONTROLLER APIS */

  /**
   * Start or pause the simulation.
   * @param isPaused True if the simulation should be paused, false otherwise
   */
  public void setStartPause(boolean isPaused) {
    this.isPaused = isPaused;
  }

  /**
   * Check if the simulation is currently paused.
   * @return True if the simulation is paused, false otherwise
   */
  public boolean isLoaded() {
    return isLoaded;
  }

  /**
   * Check if the simulation is currently paused.
   * @return True if the simulation is paused, false otherwise
   */
  public boolean isPaused() {
    return isPaused;
  }

  /* PRIVATE HELPER METHODS */

  private void initGrid() {
    simulationScene.setGrid(grid.getNumRows(), grid.getNumCols());
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        simulationScene.setCell(grid.getNumCols(), i, j, grid.getCell(i, j).getCurrentState());
        simulationScene.setParameters(i, j, grid.getCell(i, j).getCurrentState(), grid.getCell(i, j).getAllProperties());
      }
    }
  }

  private ConfigInfo saveConfigInfo() throws InvocationTargetException, IllegalAccessException {
    // Save the grid data
    List<List<CellRecord>> gridData = new ArrayList<>();
    for (int i = 0; i < grid.getNumRows(); i++) {
      List<CellRecord> row = new ArrayList<>();
      for (int j = 0; j < grid.getNumCols(); j++) {
        Cell<?> cell = grid.getCell(i, j);
        row.add(new CellRecord(cell.getCurrentState().getValue(), cell.getAllProperties()));
      }
      gridData.add(row);
    }

    // Save the parameters
    Map<String, Double> doubleParams = new HashMap<>();
    Map<String, String> stringParams = new HashMap<>();

    for (Method method : gameLogic.getClass().getDeclaredMethods()) {
      String methodName = method.getName();
      if (!methodName.startsWith("get")) {
        continue;
      }

      String paramName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
      if (method.getReturnType() == double.class) {
        doubleParams.put(paramName, (double) method.invoke(gameLogic));
      } else if (method.getReturnType() == String.class) {
        stringParams.put(paramName, (String) method.invoke(gameLogic));
      }
    }

    ParameterRecord parameters = new ParameterRecord(doubleParams, stringParams);


    // TODO: Make user input for title, author, description
    return new ConfigInfo(
        configInfo.myType(),
        configInfo.myCellShapeType(),
        configInfo.myGridEdgeType(),
        configInfo.myneighborArrangementType(),
        configInfo.myTitle(),
        configInfo.myAuthor(),
        configInfo.myDescription(),
        grid.getNumCols(),
        grid.getNumRows(),
        simulationScene.getTickSpeed(),
        gridData,
        parameters,
        configInfo.acceptedStates(),
        configInfo.myFileName()
    );
  }
}
