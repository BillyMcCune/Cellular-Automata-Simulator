package cellsociety.view.controller;

import cellsociety.logging.Log;
import cellsociety.model.configAPI.configAPI;
import cellsociety.model.modelAPI.ModelApi;
import cellsociety.view.renderer.drawer.GridDrawer;
import cellsociety.view.renderer.drawer.HexGridDrawer;
import cellsociety.view.renderer.drawer.SquareGridDrawer;
import cellsociety.view.scene.SceneUIWidgetFactory;
import cellsociety.view.scene.SimulationScene;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class SceneController {

  // Constants
  public static final double MAX_SPEED = 100;
  public static final double MIN_SPEED = 0;
  public static final double SPEED_MULTIPLIER = 3;
  public static final String GRIDDRAWER_PACKAGE = "cellsociety.view.renderer.drawer.";
  // TODO: Change this to the default configuration file.
  public static final String DEFAULT_LOADED_SIMULATION_CONFIG = "GameOfLifeGlider.xml";

  // Controller instance variables
  private final ModelApi myModelApi;
  private final configAPI myConfigAPI;
  private final SimulationScene simulationScene;

  // Simulation state variables
  private boolean isLoaded;
  private boolean isPaused;

  // Grid variables
  private int numRows;
  private int numCols;
  private int numIterations;
  private Class<? extends GridDrawer> gridDrawerClass;

  // Frame update variables
  private double updateInterval;
  private double timeSinceLastUpdate;

  /**
   * Constructor for the SceneController. Creates the model and configuration APIs and links them
   * together.
   *
   * @param scene the simulation scene to control
   */
  public SceneController(SimulationScene scene) {
    // Initialize the model and configuration APIs.
    // Connect the config API to the model API so that configuration updates propagate.
    myModelApi = new ModelApi();
    myConfigAPI = new configAPI();
    myConfigAPI.setModelAPI(myModelApi);

    // Initialize the simulation controller
    this.simulationScene = scene;
    this.isPaused = true;
    this.isLoaded = false;
    this.updateInterval = 2.0 / (MAX_SPEED + MIN_SPEED);
    this.timeSinceLastUpdate = 0.0;
    this.numIterations = 0;

    // Initialize the styles
    initSimulationStyle();
  }

  /**
   * Updates the simulation by delegating to the model API and then refreshing the scene.
   *
   * @param elapsedTime the time elapsed since the last update
   */
  public void update(double elapsedTime) {
    if (!isPaused) {
      timeSinceLastUpdate += elapsedTime;
      if (timeSinceLastUpdate >= updateInterval) {
        myModelApi.updateSimulation();
        updateViewGrid();
        updateViewInfo();

        timeSinceLastUpdate = 0.0;
        numIterations++;
      }
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
      resetModel();
      initViewGrid();
      resetParameters();
      isLoaded = true;
    } catch (ParserConfigurationException | IOException | SAXException | NoSuchMethodException |
             InvocationTargetException | IllegalAccessException | NullPointerException ex) {
      SceneUIWidgetFactory.createErrorDialog(
          LanguageController.getStringProperty("error-loadConfig").getValue(),
          ex.getMessage(), ex);
    }
  }

  /**
   * Saves the current simulation configuration using configAPI.
   *
   * @param path the path to save the configuration file
   */
  public void saveConfig(String path) {
    try {
      // TODO: Make User Change the Simulation Details
      String savedFile = myConfigAPI.saveSimulation(path);
      String message = String.format(
          LanguageController.getStringProperty("success-saveConfigMessage").getValue(), path);
      SceneUIWidgetFactory.createSuccessSaveDialog(
          LanguageController.getStringProperty("success-saveConfigTitle").getValue(),
          message, savedFile);
    } catch (Exception e) {
      SceneUIWidgetFactory.createErrorDialog(
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
   * Retrieves a list of all available cell shape names.
   * @return a list of cell shape names
   */
  public List<String> getAllCellShapeNames() {
    return myModelApi.getPossibleCellShapes().stream().map(shape -> shape.substring(0, 1).toUpperCase() + shape.substring(1).toLowerCase()).collect(Collectors.toList());
  }

  /**
   * Retrieves a list of all available edge policy names.
   * @return a list of edge policy names
   */
  public List<String> getALlEdgePolicies() {
    return myModelApi.getPossibleEdgePolicies().stream().map(shape -> shape.substring(0, 1).toUpperCase() + shape.substring(1).toLowerCase()).collect(Collectors.toList());
  }

  /**
   * Retrieves a list of all available neighbor arrangement names.
   * @return a list of neighbor arrangement names
   */
  public List<String> getAllNeighborArrangements() {
    return myModelApi.getPossibleNeighborArrangements().stream().map(shape -> shape.substring(0, 1).toUpperCase() + shape.substring(1).toLowerCase()).collect(Collectors.toList());
  }

  /**
   * Retrieves simulation information from the configuration.
   *
   * @return a formatted string containing author, title, type, and description.
   */
  public String getConfigInformation() {
    try {
      Map<String, String> simulationInfo = myConfigAPI.getSimulationInformation();
      return String.format("Author: %s\nTitle: %s\nType: %s\nIterations: %d\nDescription: %s",
          simulationInfo.get("author"),
          simulationInfo.get("title"),
          simulationInfo.get("type"),
          numIterations,
          simulationInfo.get("description")
      );
    } catch (NullPointerException ex) {
      // TODO: Handle missing configuration appropriately.
    }
    return "";
  }

  /**
   * Retrieves the title of the simulation from the configuration.
   *
   * @return the title of the simulation
   */
  public String getSimulationTitle() {
    return myConfigAPI.getSimulationInformation().get("title");
  }

  /* MODEL APIS */

  /**
   * Resets the entire model using the model API and refreshes the scene.
   */
  public void resetModel() {
    try {
      myModelApi.resetModel();
      resetParameters();
      updateViewGrid();

      numIterations = 0;
      updateViewInfo();
    } catch (Exception e) {
      SceneUIWidgetFactory.createErrorDialog(
          LanguageController.getStringProperty("error-resetModel").getValue(),
          e.getMessage(), e);
    }
  }

  /**
   * Resets only the grid in the model using the model API and refreshes the scene.
   */
  public void resetGrid() {
    try {
      myModelApi.resetGrid();
      initViewGrid();

      numIterations = 0;
      updateViewInfo();
    } catch (Exception e) {
      SceneUIWidgetFactory.createErrorDialog(
          LanguageController.getStringProperty("error-resetGrid").getValue(),
          e.getMessage(), e);
    }
  }

  /**
   * Updates the simulation parameters in the UI by retrieving the parameter values from modelAPI.
   * This method calls the model's resetParameters method (which updates the parameter record based
   * on the current gameLogic), and then iterates over the double and string parameters to register
   * callbacks in the UI.
   */
  public void resetParameters()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    // Update the model's parameter record.
    myModelApi.resetParameters();

    // clear the parameters in the simulation UI
    simulationScene.clearParameters();

    // Update the speed parameter.
    simulationScene.setParameter(MIN_SPEED, MAX_SPEED, myConfigAPI.getConfigSpeed(), "speed-label",
        "speed-tooltip", speed -> {
          // Change the speed of the simulation
          updateInterval = 10 / (speed * SPEED_MULTIPLIER);
        });

    // Update double parameters.
    Map<String, Double> doubleParams = myModelApi.getDoubleParameters();
    for (Map.Entry<String, Double> entry : doubleParams.entrySet()) {
      String paramName = entry.getKey();
      double defaultValue = entry.getValue();
      double[] parameterBounds = myModelApi.getParameterBounds(paramName);
      // Retrieve min and max using the game logic's methods.
      double min = parameterBounds[0];
      double max = parameterBounds[1];

      // Obtain the consumer from modelAPI.
      Consumer<Double> consumer = myModelApi.getDoubleParameterConsumer(paramName);

      // Register the parameter in the simulation UI.
      simulationScene.setParameter(min, max, defaultValue,
          paramName + "-label", paramName + "-tooltip", consumer);
    }

    // Update string parameters.
    Map<String, String> stringParams = myModelApi.getStringParameters();
    for (Map.Entry<String, String> entry : stringParams.entrySet()) {
      String paramName = entry.getKey();
      String defaultValue = entry.getValue();

      // Obtain the consumer from modelAPI.
      Consumer<String> consumer = myModelApi.getStringParameterConsumer(paramName);

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
   * Sets the cell shape of the simulation.
   * @param cellShape the cell shape to set, the first letter should be capitalized
   *                  (e.g. "Square", "Triangle", "Hex")
   */
  public void setCellShape(String cellShape) {
    try {
      Class<?> clazz = Class.forName(GRIDDRAWER_PACKAGE + cellShape + "GridDrawer");
      if (GridDrawer.class.isAssignableFrom(clazz)) {
        gridDrawerClass = clazz.asSubclass(GridDrawer.class);
        myModelApi.setCellShape(cellShape.toUpperCase());

        initViewGrid();
        updateViewGrid();
      } else {
        throw new ClassCastException("Class " + cellShape + "GridDrawer does not extend GridDrawer.");
      }
    } catch (ClassNotFoundException | ClassCastException e) {
      SceneUIWidgetFactory.createErrorDialog(
          // TODO: Add error message to the language controller
          LanguageController.getStringProperty("error-setCellShape").getValue(),
          e.getMessage(), e);
    }
  }

  /**
   * Sets the edge policy of the simulation.
   * @param edgePolicy the edge policy to set, the first letter should be capitalized
   *                  (e.g. "Finite", "Toroidal", "Infinite")
   */
  public void setEdgePolicy(String edgePolicy) {
    try {
      myModelApi.setEdgePolicy(edgePolicy.toUpperCase());
    } catch (Exception e) {
      SceneUIWidgetFactory.createErrorDialog(
          // TODO: Add error message to the language controller
          LanguageController.getStringProperty("error-setEdgePolicy").getValue(),
          e.getMessage(), e);
    }
  }

  /**
   * Sets the neighbor arrangement of the simulation.
   * @param neighborArrangement the neighbor arrangement to set, the first letter should be capitalized
   *                  (e.g. "Cardinal", "Diagonal", "Both")
   */
  public void setNeighborArrangement(String neighborArrangement) {
    try {
      myModelApi.setNeighborArrangement(neighborArrangement.toUpperCase());
    } catch (Exception e) {
      SceneUIWidgetFactory.createErrorDialog(
          // TODO: Add error message to the language controller
          LanguageController.getStringProperty("error-setNeighborArrangement").getValue(),
          e.getMessage(), e);
    }
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

  /* PRIVATE HELPER METHODS */

  private void initSimulationStyle() {
    // TODO: Initialize the Simulation Style as the preference ones.
    // Cell shape
    gridDrawerClass = SquareGridDrawer.class;

    // Edge type
  }

  private void initViewGrid() throws NullPointerException {
    numRows = myConfigAPI.getGridHeight();
    numCols = myConfigAPI.getGridWidth();
    simulationScene.setGrid(numRows, numCols, gridDrawerClass);

    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numCols; j++) {
        // TODO: Add support for wantDefaultColor.
        // NOTES: I'm not sure what wantDefaultColor is supposed to do.
        //    BY: Hsuan-Kai Liao
        simulationScene.setCell(numCols, i, j, myModelApi.getCellColor(i, j, true));
      }
    }
  }

  private void updateViewGrid() {
    if (!isPaused) {
      if (numRows == 0 || numCols == 0) {
        initViewGrid();
      }
      for (int i = 0; i < numRows; i++) {
        for (int j = 0; j < numCols; j++) {
          // TODO: Add support for wantDefaultColor.
          // NOTES: I'm not sure what wantDefaultColor is supposed to do.
          //    BY: Hsuan-Kai Liao
          simulationScene.setCell(numCols, i, j, myModelApi.getCellColor(i, j, true));
        }
      }
    }
  }

  private void updateViewInfo() {
    if (isLoaded) {
      simulationScene.setInfo(getConfigInformation());
    }
  }

  private int getTickSpeed() {
    return (int) (10 / updateInterval / SPEED_MULTIPLIER);
  }

}
