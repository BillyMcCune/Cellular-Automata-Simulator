package cellsociety.view.controller;

import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigInfo.SimulationType;
import cellsociety.model.config.ConfigReader;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.states.FireState;
import cellsociety.model.data.states.LifeState;
import cellsociety.model.data.states.PercolationState;
import cellsociety.model.logic.FireLogic;
import cellsociety.model.logic.LifeLogic;
import cellsociety.model.logic.Logic;
import cellsociety.model.logic.PercolationLogic;
import cellsociety.view.scene.SimulationScene;
import java.util.List;

/**
 * The SceneController class is responsible for controlling the flow of the application and managing the different scenes.
 * It communicates with the ConfigIO parts and the main Model Logics.
 */
public class SceneController {

  // ConfigIO
  private ConfigReader configReader;
  private ConfigInfo configInfo;

  // Scene
  private SimulationScene simulationScene;

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
    if (configInfo != null) {
      isLoaded = true;
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

    String info = String.format(
        "Author: %s\nTitle: %s\nType: %s\nDescription: %s",
        configInfo.getAuthor(),
        configInfo.getTitle(),
        configInfo.getType(),
        configInfo.getDescription()
    );

    return info;
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
    // NOTES: Maybe we can redesign a map to take in a SimulationType,
    //        so that we can get rid of the annoying switch statement.
    //        This is just a temporary solution.
    //    BY: Hsuan-Kai Liao

    if (configInfo == null) {
      return;
    }

    SimulationType type = configInfo.getType();

    switch (type) {
      case GAMEOFLIFE -> {
        CellFactory<LifeState> lifeFactory = new CellFactory<>(LifeState.class);
        Grid<LifeState> lifeGrid = new Grid<>(configInfo.getGrid(), lifeFactory);
        gameLogic = new LifeLogic(lifeGrid);
        grid = lifeGrid;
      }
      case PERCOLATION -> {
        CellFactory<PercolationState> percFactory = new CellFactory<>(PercolationState.class);
        Grid<PercolationState> percGrid = new Grid<>(configInfo.getGrid(), percFactory);
        gameLogic = new PercolationLogic(percGrid);
        grid = percGrid;
      }
      case SPREADINGOFFIRE -> {
        CellFactory<FireState> fireFactory = new CellFactory<>(FireState.class);
        Grid<FireState> fireGrid = new Grid<>(configInfo.getGrid(), fireFactory);
        gameLogic = new FireLogic(fireGrid);
        grid = fireGrid;
      }
      default -> throw new UnsupportedOperationException("Unsupported simulation type: " + type);
    }
      // TODO: Add more cases for other simulation types

    // Set the grid to the scene
    simulationScene.setGrid(grid.getNumRows(), grid.getNumCols());
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        simulationScene.setCell(i, j, grid.getCell(i, j).getCurrentState());
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
