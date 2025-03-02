package cellsociety.view.controller;

import cellsociety.model.config.ConfigInfo;
import cellsociety.model.configAPI.configAPI;
import cellsociety.model.modelAPI.modelAPI;
import cellsociety.view.scene.SceneUIWidget;
import cellsociety.view.scene.SimulationScene;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class SceneController {

  private modelAPI myModelAPI;
  private configAPI myConfigAPI;
  private final SimulationScene simulationScene;
  private boolean isLoaded;
  private boolean isPaused;
  private int numRows;
  private int numCols;
  private String configTitle;

  /**
   * Constructor for the SceneController. Creates the model and configuration APIs and links them
   * together.
   *
   * @param scene the simulation scene to control
   */
  public SceneController(SimulationScene scene) {
    myModelAPI = new modelAPI();
    myConfigAPI = new configAPI();
    // Connect the config API to the model API so that configuration updates propagate.
    myConfigAPI.setModelAPI(myModelAPI);
    this.simulationScene = scene;
    this.isPaused = true;
  }

  /**
   * Updates the simulation by delegating to the model API and then refreshing the scene.
   */
  public void update() {
    if (!isPaused) {
      myModelAPI.updateSimulation();
      updateGrid();
    }
  }

  /* CONFIGURATION IO APIS */

  /**
   * Loads a configuration file using configAPI. On success, resets the model and refreshes the
   * scene.
   *
   * @param filename the configuration file to load
   */
  public void loadConfig(String filename) {
    try {
      myConfigAPI.loadSimulation(filename);
      myModelAPI.resetModel();
      initGrid();
      isLoaded = true;
      resetParameters();
    } catch (ParserConfigurationException | IOException | SAXException | NoSuchMethodException | InvocationTargetException |IllegalAccessException ex) {
      SceneUIWidget.createErrorDialog(
          LanguageController.getStringProperty("error-loadConfig").getValue(),
          ex.getMessage(), ex);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Saves the current simulation configuration using configAPI.
   *
   * @param path the path to save the configuration file
   */
  public void saveConfig(String path) {
    try {
      String savedFile = myConfigAPI.saveSimulation(path);
      List<String> fileNames = myConfigAPI.getFileNames();
      String message = String.format(
          LanguageController.getStringProperty("success-saveConfigMessage").getValue(), path);
      SceneUIWidget.createSuccessSaveDialog(
          LanguageController.getStringProperty("success-saveConfigTitle").getValue(),
          message, savedFile);
    } catch (Exception e) {
      SceneUIWidget.createErrorDialog(
          LanguageController.getStringProperty("error-saveConfig").getValue(),
          e.getMessage(), e);
    }
  }

  /**
   * Retrieves a list of all available configuration file names.
   *
   * @return a list of configuration file names
   */
  public List<String> getAllConfigFileNames() {
    return myConfigAPI.getFileNames();
  }

  /**
   * Retrieves simulation information from the configuration.
   *
   * @return a formatted string containing author, title, type, and description.
   */
  public String getConfigInformation() {
    try {
      Map<String, String> simulationInfo = myConfigAPI.getSimulationInformation();
      configTitle = simulationInfo.get("title");
      return String.format("Author: %s\nTitle: %s\nType: %s\nDescription: %s",
          simulationInfo.get("author"),
          simulationInfo.get("title"),
          simulationInfo.get("type"),
          simulationInfo.get("description"));
    } catch (NullPointerException ex) {
      // TODO: Handle missing configuration appropriately.
    }
    return "";
  }

  public String getSimulationTitle() {
    return configTitle;
  }

  /**
   * Retrieves the simulation tick speed from the configuration.
   *
   * @return the simulation speed
   */
  public double getConfigSpeed() {
    try {
      return myConfigAPI.getConfigSpeed();
    } catch (NullPointerException ex) {
      // TODO: Handle missing configuration appropriately.
    }
    return 0;
  }

  /* MODEL APIS */

  /**
   * Resets the entire model using the model API and refreshes the scene.
   */
  public void resetModel() {
    try {
      myModelAPI.resetModel();
      resetParameters();
      updateGrid();
    } catch (Exception e) {
      SceneUIWidget.createErrorDialog(
          LanguageController.getStringProperty("error-resetModel").getValue(),
          e.getMessage(), e);
    }
  }

  /**
   * Resets only the grid in the model using the model API and refreshes the scene.
   */
  public void resetGrid() {
    try {
      myModelAPI.resetGrid();
      updateGrid();
    } catch (Exception e) {
      SceneUIWidget.createErrorDialog(
          LanguageController.getStringProperty("error-resetGrid").getValue(),
          e.getMessage(), e);
    }
  }

  /**
   * Updates the simulation parameters in the UI by retrieving the parameter values
   * from modelAPI. This method calls the model's resetParameters method (which updates
   * the parameter record based on the current gameLogic), and then iterates over the
   * double and string parameters to register callbacks in the UI.
   *
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   * @throws NoSuchMethodException
   */
  public void resetParameters()
      throws Exception {
    // First, update the model's parameter record.
    myModelAPI.resetParameters();



    // Update double parameters.
    Map<String, Double> doubleParams = myModelAPI.getDoubleParameters();
    for (Map.Entry<String, Double> entry : doubleParams.entrySet()) {
      String paramName = entry.getKey();
      double defaultValue = entry.getValue();
      double[] parameterBounds = myModelAPI.getParameterBounds(paramName);
      // Retrieve min and max using the game logic's methods.
      double min = parameterBounds[0];
      double max = parameterBounds[1];

      // Obtain the consumer from modelAPI.
      Consumer<Double> consumer = myModelAPI.getDoubleParameterConsumer(paramName);

      // Register the parameter in the simulation UI.
      simulationScene.setParameter(min, max, defaultValue,
          paramName + "-label", paramName + "-tooltip", consumer);
    }

    // Update string parameters.
    Map<String, String> stringParams = myModelAPI.getStringParameters();
    for (Map.Entry<String, String> entry : stringParams.entrySet()) {
      String paramName = entry.getKey();
      String defaultValue = entry.getValue();

      // Obtain the consumer from modelAPI.
      Consumer<String> consumer = myModelAPI.getStringParameterConsumer(paramName);

      // Register the parameter in the simulation UI.
      simulationScene.setParameter(defaultValue,
          paramName + "-label", paramName + "-tooltip", consumer);
    }
  }




  /* CONTROLLER APIS */

  /**
   * Starts or pauses the simulation.
   *
   * @param isPaused true to pause the simulation, false to start it
   */
  public void setStartPause(boolean isPaused) {
    this.isPaused = isPaused;
  }

  /**
   * Indicates whether the simulation is loaded.
   *
   * @return true if loaded, false otherwise
   */
  public boolean isLoaded() {
    return isLoaded;
  }

  /**
   * Indicates whether the simulation is paused.
   *
   * @return true if paused, false otherwise
   */
  public boolean isPaused() {
    return this.isPaused;
  }

  /**
   * Initializes the grid on the simulation scene by retrieving the cell states and properties from
   * the model API.
   */
  //TODO fix this
  private void initGrid() {
    numRows = myConfigAPI.getGridHeight();
    numCols = myConfigAPI.getGridWidth();
    simulationScene.setGrid(numRows, numCols);
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numCols; j++) {
        simulationScene.setCell(numCols, i, j, myModelAPI.getCellColor(i, j));
      }
    }
  }

  private void updateGrid() {
    if (!isPaused) {
      if (numRows == 0 || numCols == 0) {
        initGrid();
      }
      for (int i = 0; i < numRows; i++) {
        for (int j = 0; j < numCols; j++) {
          simulationScene.setCell(numCols, i, j, myModelAPI.getCellColor(i, j));
        }
      }
    }
  }



}
