package cellsociety.view.controller;

import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigInfo.SimulationType;
import cellsociety.model.config.ConfigReader;
import cellsociety.model.config.ConfigWriter;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.states.FireState;
import cellsociety.model.data.states.LifeState;
import cellsociety.model.data.states.PercolationState;
import cellsociety.model.data.states.SegregationState;
import cellsociety.model.data.states.State;
import cellsociety.model.data.states.WatorState;
import cellsociety.model.logic.FireLogic;
import cellsociety.model.logic.LifeLogic;
import cellsociety.model.logic.Logic;
import cellsociety.model.logic.PercolationLogic;
import cellsociety.model.logic.SegregationLogic;
import cellsociety.model.logic.WatorLogic;
import cellsociety.view.scene.SimulationScene;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.xml.parsers.ParserConfigurationException;

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
    this.configWriter = new ConfigWriter(configInfo); // TODO: REQUIRE CONSTRUCTOR WITH NO ARGUMENTS
    this.simulationScene = scene;
    this.isPaused = true;
  }

  /**
   * Update the simulation scene with the latest data from the model.
   */
  public void update() {
    if (!isPaused) {
      for (int i = 0; i < grid.getNumRows(); i++) {
        for (int j = 0; j < grid.getNumCols(); j++) {
          simulationScene.setCell(i, j, grid.getCell(i, j).getCurrentState());
        }
      }

      gameLogic.update();
    }
  }

  // ConfigIO APIs

  /**
   * Load the configuration file with the given filename.
   * @param filename The name of the configuration file to load
   */
  public void loadConfig(String filename) {
    configInfo = configReader.readConfig(filename);
    configWriter.setConfigInfo(configInfo);
    if (configInfo != null) {
      isLoaded = true;
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
      configWriter.saveCurrentConfig();
    } catch (ParserConfigurationException e) {
      System.out.println("Error saving the configuration file: " + e);
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
        configInfo.getAuthor(),
        configInfo.getTitle(),
        configInfo.getType(),
        configInfo.getDescription()
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

    return configInfo.getTitle();
  }

  /**
   * Get the speed of the current configuration.
   * @return The speed of the current configuration
   */
  public double getConfigSpeed() {
    if (configInfo == null) {
      return 0;
    }

    return configInfo.getSpeed();
  }

  // Model APIs
  /**
   * Reset the model with the current configuration.
   */
  public void resetModel() {
    if (configInfo == null) {
      return;
    }

    try {
      SimulationType type = configInfo.getType();
      String name = type.name().charAt(0) + type.name().substring(1).toLowerCase();

      // Dynamically load the Logic and State classes
      Class<?> logicClass = Class.forName(LOGIC_PACKAGE + "." + name + "Logic");
      Class<?> stateClass = Class.forName(STATE_PACKAGE + "." + name + "State");

      // Dynamically create cell factory, grid, and logic
      Constructor<?> cellFactoryConstructor = CellFactory.class.getConstructor(Class.class);
      cellFactory = (CellFactory<?>) cellFactoryConstructor.newInstance(stateClass);
      grid = new Grid<>(configInfo.getGrid(), cellFactory);
      gameLogic = (Logic<?>) logicClass.getDeclaredConstructor(Grid.class).newInstance(grid);

      // Set the parameters for the simulation
      resetParameters(gameLogic.getClass());

      // Set the grid to the scene
      simulationScene.setGrid(grid.getNumRows(), grid.getNumCols());
      for (int i = 0; i < grid.getNumRows(); i++) {
        for (int j = 0; j < grid.getNumCols(); j++) {
          simulationScene.setCell(i, j, grid.getCell(i, j).getCurrentState());
        }
      }
    } catch (Exception e) {
      // TODO: Handle this exception
      throw new UnsupportedOperationException(e);
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
      SimulationType type = configInfo.getType();
      String name = type.name().charAt(0) + type.name().substring(1).toLowerCase();

      // Dynamically load the Logic class
      Class<?> logicClass = Class.forName(LOGIC_PACKAGE + "." + name + "Logic");

      // Dynamically create cell grid, and logic
      grid = new Grid<>(configInfo.getGrid(), cellFactory);
      gameLogic = (Logic<?>) logicClass.getDeclaredConstructor(Grid.class).newInstance(grid);

      // Set the grid to the scene
      simulationScene.setGrid(grid.getNumRows(), grid.getNumCols());
      for (int i = 0; i < grid.getNumRows(); i++) {
        for (int j = 0; j < grid.getNumCols(); j++) {
          simulationScene.setCell(i, j, grid.getCell(i, j).getCurrentState());
        }
      }
    } catch (Exception e) {
      // TODO: Handle this exception
      throw new UnsupportedOperationException(e);
    }
  }

  /**
   * Reset the parameters for the simulation.
   *
   * @param logicClass The logic class to reset the parameters for
   * @param <T> The type of the logic class
   */
  public <T extends Logic<?>>void resetParameters(Class<T> logicClass) {
    Map<String, Double> parameters = configInfo.getParameters();

    // TODO: SET UNIQUE MIN AND MAX VALUES FOR PARAMETERS
    double MIN = 0;
    double MAX = 100;

    // Set the parameter in the logic class through reflection
    for (Map.Entry<String, Double> entry : parameters.entrySet()) {
      try {
        // Get the method name
        String paramName = entry.getKey();
        String setMethodName = "set" + paramName.substring(0, 1).toUpperCase() + paramName.substring(1);

        // Get the method argument type (double or int)
        Method setMethod;
        try {
          setMethod = logicClass.getDeclaredMethod(setMethodName, double.class);
        } catch (NoSuchMethodException e) {
          try {
            setMethod = logicClass.getDeclaredMethod(setMethodName, int.class);
          } catch (NoSuchMethodException ex) {
            // TODO: Handle this exception
            throw new UnsupportedOperationException(ex);
          }
        }

        // Set the parameter in the simulation scene
        Class<?> paramType = setMethod.getParameterTypes()[0];
        Method finalSetMethod = setMethod;
        switch (paramType.getName()) {
          case "int" -> {
            int finalParamValueInt = entry.getValue().intValue();
            Consumer<Integer> consumer = value -> {
              try {
                finalSetMethod.invoke(gameLogic, value);
              } catch (Exception ex) {
                // TODO: Handle this exception
              }
            };

            simulationScene.setParameter(
                paramName,
                MIN,
                MAX,
                finalParamValueInt,
                "",
                consumer
            );
          }
          case "double" -> {
            double finalParamValueDouble = entry.getValue();
            Consumer<Double> consumer = value -> {
              try {
                finalSetMethod.invoke(gameLogic, value);
              } catch (Exception ex) {
                // TODO: Handle this exception
              }
            };

            simulationScene.setParameter(
                paramName,
                MIN,
                MAX,
                finalParamValueDouble,
                "",
                consumer
            );
          }
        }
      } catch (Exception e) {
        // TODO: Handle this exception
        throw new UnsupportedOperationException(e);
      }
    }
  }

  // Controller APIs
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

}
