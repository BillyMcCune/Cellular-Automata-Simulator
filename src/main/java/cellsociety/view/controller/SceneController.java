package cellsociety.view.controller;

import cellsociety.model.configAPI.configAPI;
import cellsociety.model.modelAPI.modelAPI;
import cellsociety.model.logic.Logic;
import cellsociety.view.scene.SceneUIWidget;
import cellsociety.view.scene.SimulationScene;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class SceneController {
  private modelAPI myModelAPI;
  private configAPI myConfigAPI;
  private final SimulationScene simulationScene;
  private boolean isLoaded;
  private boolean isPaused;

  /**
   * Constructor for the SceneController.
   * Creates the model and configuration APIs and links them together.
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
    myModelAPI.updateSimulation();
    initGrid();
  }

  /* CONFIGURATION IO APIS */

  /**
   * Loads a configuration file using configAPI.
   * On success, resets the model and refreshes the scene.
   * @param filename the configuration file to load
   */
  public void loadConfig(String filename) {
    try {
      myConfigAPI.loadSimulation(filename);
      myModelAPI.resetModel();
      initGrid();
    } catch (ParserConfigurationException | IOException | SAXException ex) {
      SceneUIWidget.createErrorDialog(
          LanguageController.getStringProperty("error-loadConfig").getValue(),
          ex.getMessage(), ex);
    } catch (Exception e) {
      SceneUIWidget.createErrorDialog(
          LanguageController.getStringProperty("error-loadConfig").getValue(),
          e.getMessage(), e);
    }
  }

  /**
   * Saves the current simulation configuration using configAPI.
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
   * @return a list of configuration file names
   */
  public List<String> getAllConfigFileNames() {
    return myConfigAPI.getFileNames();
  }

  /**
   * Retrieves simulation information from the configuration.
   * @return a formatted string containing author, title, type, and description.
   */
  public String getConfigInformation() {
    try {
      Map<String, String> simulationInfo = myConfigAPI.getSimulationInformation();
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

  /**
   * Retrieves the simulation tick speed from the configuration.
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
      simulationScene.refresh();
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
      simulationScene.refresh();
    } catch (Exception e) {
      SceneUIWidget.createErrorDialog(
          LanguageController.getStringProperty("error-resetGrid").getValue(),
          e.getMessage(), e);
    }
  }

  /**
   * Resets simulation parameters via the model API.
   */
  public void resetParameters() {
    try {
      myModelAPI.resetParameters();
    } catch (Exception e) {
      SceneUIWidget.createErrorDialog(
          LanguageController.getStringProperty("error-resetParameters").getValue(),
          e.getMessage(), e);
    }
  }

  /* CONTROLLER APIS */

  /**
   * Starts or pauses the simulation.
   * @param isPaused true to pause the simulation, false to start it
   */
  public void setStartPause(boolean isPaused) {
    this.isPaused = isPaused;
  }

  /**
   * Indicates whether the simulation is loaded.
   * @return true if loaded, false otherwise
   */
  public boolean isLoaded() {
    return isLoaded;
  }

  /**
   * Indicates whether the simulation is paused.
   * @return true if paused, false otherwise
   */
  public boolean isPaused() {
    return isPaused;
  }

  /**
   * Initializes the grid on the simulation scene by retrieving the cell states and properties
   * from the model API.
   */
  private void initGrid() {
    List<List<Integer>> cellStates = myModelAPI.getCellStates();
    List<List<Map<String, Double>>> cellProperties = myModelAPI.getCellProperties();
    if (cellStates == null || cellStates.isEmpty()) {
      return;
    }
    int numRows = cellStates.size();
    int numCols = cellStates.get(0).size();
    simulationScene.setGrid(numRows, numCols);
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numCols; j++) {
        simulationScene.setCell(numCols, i, j, cellStates.get(i).get(j));
        simulationScene.setParameters(i, j, cellStates.get(i).get(j), cellProperties.get(i).get(j));
      }
    }
  }
}
}
