package cellsociety.model.modelAPI;

import cellsociety.logging.Log;
import cellsociety.model.config.CellRecord;
import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigInfo.SimulationType;
import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.constants.EdgeType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.logic.Logic;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * @author Billy McCune
 */
public class ModelApi {

  private static final String LOGIC_PACKAGE = "cellsociety.model.logic";
  private static final String STATE_PACKAGE = "cellsociety.model.data.states";
  private ParameterRecord myParameterRecord;
  private ConfigInfo configInfo;

  ParameterManager myParameterManager;
  CellColorManager myCellColorManager;
  StyleManager myStyleManager;

  // Model
  private Grid<?> grid;
  private CellFactory<?> cellFactory;
  private Logic<?> gameLogic;
  private NeighborCalculator<?> myNeighborCalculator;


  public ModelApi() {
  }

  public void setConfigInfo(ConfigInfo configInfo) {
    this.configInfo = configInfo;
    this.myParameterRecord = configInfo.myParameters();
  }


  /**
   * Updates the simulation by invoking the game logic update method.
   */
  public void updateSimulation() {
    if (grid == null || gameLogic == null) {
      return;
    }
    try {
      gameLogic.update();
      myCellColorManager.setGrid(grid);
    } catch (NullPointerException e) {
      throw new NoSuchElementException(e.getMessage());
    }
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
      String name = getSimulationName();

      // Dynamically load the Logic class.
      Class<?> logicClass = Class.forName(LOGIC_PACKAGE + "." + name + "Logic");
      // Initialize the internal grid using the configuration.
      List<List<CellRecord>> gridCopy = deepCopyGrid(configInfo.myGrid());

      grid = new Grid<>(gridCopy, cellFactory, getGridShape(), getNeighborType(), getEdgeType());
      myNeighborCalculator = grid.getNeighborCalculator();
      // Initialize the game logic instance using the grid and parameters.
      gameLogic = (Logic<?>) logicClass.getDeclaredConstructor(Grid.class, ParameterRecord.class)
          .newInstance(grid, myParameterRecord);
      myCellColorManager.setGrid(grid);
    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
             InstantiationException | IllegalAccessException e) {
      throw new ClassNotFoundException(e.getMessage());
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
      return myParameterRecord.myDoubleParameters();
    } catch (NullPointerException e) {
      throw new NullPointerException(e.getMessage());
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
      throw new NullPointerException(e.getMessage());
    }
  }

  /**
   * Returns the color for the cell at (row, col). First, the color is determined from the cell's
   * current state. If that color is WHITE, the method will check if any of the cell’s property
   * values (if nonzero) have an associated color.
   */
  public String getCellColor(int row, int col, boolean wantDefaultColor) throws NullPointerException {
    if (myCellColorManager == null) {
      myCellColorManager = new CellColorManager(grid);
    }
    try {
      return myCellColorManager.getCellColor(row, col, wantDefaultColor);
    } catch (NullPointerException e) {
      throw new NullPointerException(e.getMessage());
    }
  }


  /**
   * Resets the simulation parameters by iterating over the public setter methods of the currently
   * loaded gameLogic. For each setter, the corresponding getter, getMinParam, and getMaxParam
   * methods are used to obtain the default and bounds values, which are then stored in the
   * parameter record.
   *
   * @throws IllegalArgumentException if the game logic is not initialized.
   * @throws NullPointerException     if the game logic is not initialized.
   * @throws IllegalStateException    if the game logic is not initialized.
   * @throws NoSuchMethodException    if a required method is not found.
   */
  public void resetParameters()
      throws IllegalArgumentException, NullPointerException, IllegalStateException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    try {
      myParameterManager = new ParameterManager(gameLogic, myParameterRecord);
      myParameterManager.resetParameters();
    } catch (IllegalArgumentException| NullPointerException | IllegalStateException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
      throw new NoSuchMethodException(e.getMessage());
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
      String name = getSimulationName();

      // Dynamically load the Logic and State classes.
      Class<?> logicClass = Class.forName(LOGIC_PACKAGE + "." + name + "Logic");
      Class<?> stateClass = Class.forName(STATE_PACKAGE + "." + name + "State");

      // Dynamically create cell factory, grid, and logic.
      Constructor<?> cellFactoryConstructor = CellFactory.class.getConstructor(Class.class);
      cellFactory = (CellFactory<?>) cellFactoryConstructor.newInstance(stateClass);

      grid = new Grid<>(configInfo.myGrid(), cellFactory, getGridShape(), getNeighborType(),
          getEdgeType());
      myNeighborCalculator = grid.getNeighborCalculator();
      gameLogic = (Logic<?>) logicClass.getDeclaredConstructor(Grid.class, ParameterRecord.class)
          .newInstance(grid, configInfo.myParameters());
      if (myCellColorManager == null) {
        myCellColorManager = new CellColorManager(grid);
      }
      myCellColorManager.setGrid(grid);
    } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
             IllegalAccessException e) {
      throw new RuntimeException(e.getMessage());
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
    try{
    for (int i = 0; i < grid.getNumRows(); i++) {
      List<Integer> rowStates = new ArrayList<>();
      for (int j = 0; j < grid.getNumCols(); j++) {
        Cell<?> cell = grid.getCell(i, j);
        rowStates.add(cell.getCurrentState().getValue());
      }
      cellStates.add(rowStates);
    }
    return cellStates;
  } catch (NullPointerException e) {
      throw new NullPointerException(e.getMessage());
    }
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
    if (myParameterManager == null) {
      myParameterManager = new ParameterManager(gameLogic, myParameterRecord);
    }
    return myParameterManager.getDoubleParameterConsumer(paramName);
  }

  /**
   * Returns a Consumer that will invoke the appropriate setter for a String parameter.
   *
   * @param paramName the name of the parameter
   * @return a Consumer<String> that calls setStringParameter(paramName, newValue)
   * @throws NoSuchElementException if the corresponding setter is not found.
   */
  public Consumer<String> getStringParameterConsumer(String paramName) {
    if (myParameterManager == null) {
      myParameterManager = new ParameterManager(gameLogic, myParameterRecord);
    }
    return myParameterManager.getStringParameterConsumer(paramName);
  }

  /**
   * Retrieves the minimum and maximum bounds for a given parameter from the game logic.
   *
   * @param paramName the parameter name (e.g., "probCatch")
   * @return a double array where index 0 is the minimum value and index 1 is the maximum value
   * @throws NoSuchMethodException     if the corresponding getter methods are not found
   * @throws InvocationTargetException if a getter method cannot be invoked
   * @throws IllegalAccessException    if a getter method cannot be accessed
   */
  public double[] getParameterBounds(String paramName)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    if (myParameterManager == null) {
      myParameterManager = new ParameterManager(gameLogic, myParameterRecord);
    }
    try {
      return myParameterManager.getParameterBounds(paramName);
    } catch (NullPointerException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new NoSuchMethodException(e.getMessage());
    }
  }

  /**
   * Retrieves the mapping of cell types to their default colors from a properties file.
   *
   * @param SimulationType the simulation type identifier
   * @return a map of cell type keys to their default color values
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public Map<String, String> getCellTypesAndDefaultColors(String SimulationType) {
    if (myCellColorManager == null) {
      myCellColorManager = new CellColorManager(grid);
    }
    return myCellColorManager.getCellTypesAndDefaultColors(SimulationType);
  }

  /**
   * Sets a new color preference for a given cell state.
   *
   * @param stateName the state name for which to set the new color
   * @param newColor  the new color value (hex string)
   * @throws NoSuchElementException if the color preference cannot be updated due to an I/O error
   */
  public void setNewColorPreference(String stateName, String newColor) {
    if (myCellColorManager == null) {
      myCellColorManager = new CellColorManager(grid);
    }
    myCellColorManager.setNewColorPreference(stateName, newColor);
  }

  /**
   * Retrieves the color preference for a given cell state from user-defined settings.
   *
   * @param stateName the cell state name
   * @return the color value as a hex string, or the default color if not set
   */
  public String getColorFromPreferences(String stateName) {
    if (myCellColorManager == null) {
      myCellColorManager = new CellColorManager(grid);
    }
    return myCellColorManager.getColorFromPreferences(stateName);
  }

  /**
   * Retrieves the default color for a given cell state from the properties file.
   *
   * @param stateName the cell state name
   * @return the default color as a hex string, or "WHITE" if not defined
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public String getDefaultColorByState(String stateName) {
    if (myCellColorManager == null) {
      myCellColorManager = new CellColorManager(grid);
    }
    try {
      return myCellColorManager.getDefaultColorByState(stateName);
    } catch (NullPointerException e) {
      throw new NullPointerException(e.getMessage());
    }
  }

  /**
   * Sets the neighbor arrangement preference.
   *
   * @param neighborArrangement the new neighbor arrangement value
   * @throws NoSuchElementException if the neighbor arrangement cannot be updated due to an I/O
   *                                error
   */
  public void setNeighborArrangement(String neighborArrangement) throws NullPointerException {
    if (myStyleManager == null) {
      myStyleManager = new StyleManager(myNeighborCalculator);
    }
    try {
      myStyleManager.setNeighborArrangement(neighborArrangement);
    } catch (NullPointerException e) {
      throw new NullPointerException(e.getMessage());
    }
  }


  /**
   * Sets the edge policy for the simulation.
   *
   * @param edgePolicy the new edge policy value
   * @throws NoSuchElementException if the edge policy cannot be updated due to an I/O error
   */
  public void setEdgePolicy(String edgePolicy) throws NullPointerException {
    if (myStyleManager == null) {
      myStyleManager = new StyleManager(myNeighborCalculator);
    }
    try {
      myStyleManager.setEdgePolicy(edgePolicy);
    } catch (NullPointerException e) {
      throw new NullPointerException(e.getMessage());
    }
  }


  /**
   * Sets the cell shape preference.
   *
   * @param cellShape the new cell shape value (e.g., "SQUARE", "HEXAGON")
   * @throws NoSuchElementException if the cell shape cannot be updated due to an I/O error
   */
  public void setCellShape(String cellShape) throws NullPointerException {
    if (myStyleManager == null) {
      myStyleManager = new StyleManager(myNeighborCalculator);
    }
    myStyleManager.setCellShape(cellShape);
  }

  /**
   * Sets the grid outline preference.
   *
   * @param wantsGridOutline true if the grid outline should be displayed, false otherwise
   * @throws NoSuchElementException if the preference cannot be updated due to an I/O error
   */
  public void setGridOutlinePreference(boolean wantsGridOutline) {
    if (myStyleManager == null) {
      myStyleManager = new StyleManager(myNeighborCalculator);
    }
    myStyleManager.setGridOutlinePreference(wantsGridOutline);
  }

  public boolean getGridOutlinePreference() {
    if (myStyleManager == null) {
      myStyleManager = new StyleManager(myNeighborCalculator);
    }
    return myStyleManager.getGridOutlinePreference();
  }

  /**
   * Retrieves a list of possible neighbor arrangements defined in the simulation style properties.
   *
   * @return a list of possible neighbor arrangement values
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public List<String> getPossibleNeighborArrangements() {
    if (myStyleManager == null) {
      myStyleManager = new StyleManager(myNeighborCalculator);
    }
    return myStyleManager.getPossibleNeighborArrangements();
  }

  /**
   * Retrieves a list of possible edge policies defined in the simulation style properties.
   *
   * @return a list of possible edge policy values
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public List<String> getPossibleEdgePolicies() {
    if (myStyleManager == null) {
      myStyleManager = new StyleManager(myNeighborCalculator);
    }
    return myStyleManager.getPossibleEdgePolicies();
  }

  /**
   * Retrieves a list of possible cell shapes defined in the simulation style properties.
   *
   * @return a list of possible cell shape values
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public List<String> getPossibleCellShapes() {
    if (myStyleManager == null) {
      myStyleManager = new StyleManager(myNeighborCalculator);
    }
    return myStyleManager.getPossibleCellShapes();
  }

  public String getDefaultNeighborArrangement() {
    return configInfo.myneighborArrangementType().name();
  }

  public String getDefaultEdgePolicy() {
    return configInfo.myGridEdgeType().name();
  }

  public String getDefaultCellShape() {
    return configInfo.myCellShapeType().name();
  }

  private GridShape getGridShape() {
    return GridShape.valueOf(configInfo.myCellShapeType().name());
  }

  private NeighborType getNeighborType() {
    return NeighborType.valueOf(configInfo.myneighborArrangementType().name());
  }

  private EdgeType getEdgeType() {
    return EdgeType.valueOf(configInfo.myGridEdgeType().name());
  }

  private String getSimulationName() {
    SimulationType type = configInfo.myType();
    return type.name().charAt(0) + type.name().substring(1).toLowerCase();
  }

}

