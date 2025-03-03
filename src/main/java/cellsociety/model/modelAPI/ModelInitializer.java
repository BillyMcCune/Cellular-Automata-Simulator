package cellsociety.model.modelAPI;

import cellsociety.model.config.CellRecord;
import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigInfo.SimulationType;
import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.logic.Logic;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * The ModelInitializer class encapsulates the logic required to initialize the simulation model.
 * It dynamically loads the required classes, creates a deep copy of the configuration grid, and
 * instantiates the cell factory, neighbor calculator, and game logic.
 */
public class ModelInitializer {

  private ConfigInfo configInfo;
  private ParameterRecord parameterRecord;

  private Grid<?> grid;
  private CellFactory<?> cellFactory;
  private Logic<?> gameLogic;
  private NeighborCalculator<?> neighborCalculator;

  private static final String LOGIC_PACKAGE = "cellsociety.model.logic";
  private static final String STATE_PACKAGE = "cellsociety.model.data.states";
  private static final String NEIGHBOR_PACKAGE = "cellsociety.model.data.neighbors";

  /**
   * Constructs a new ModelInitializer with the given configuration information.
   *
   * @param configInfo the simulation configuration information
   */
  public ModelInitializer(ConfigInfo configInfo) {
    this.configInfo = configInfo;
    this.parameterRecord = configInfo.myParameters();
  }

  /**
   * Initializes the simulation model by dynamically loading the required classes for logic,
   * state, and neighbor calculation, creating a deep copy of the grid, and instantiating the game logic.
   *
   * @return the initialized game logic instance
   * @throws ClassNotFoundException    if a required class cannot be found
   * @throws NoSuchMethodException     if a required constructor or method is missing
   * @throws InvocationTargetException if instantiation fails due to a constructor throwing an exception
   * @throws InstantiationException    if an object cannot be instantiated
   * @throws IllegalAccessException    if there is an illegal access during instantiation
   */
  public Logic<?> initializeModel() throws ClassNotFoundException, NoSuchMethodException,
      InvocationTargetException, InstantiationException, IllegalAccessException {
    SimulationType type = configInfo.myType();
    String simulationName = formatSimulationName(type);

    // Dynamically load the Logic, State, and NeighborCalculator classes.
    Class<?> logicClass = Class.forName(LOGIC_PACKAGE + "." + simulationName + "Logic");
    Class<?> stateClass = Class.forName(STATE_PACKAGE + "." + simulationName + "State");
    Class<?> neighborClass = Class.forName(NEIGHBOR_PACKAGE + "." + simulationName + "NeighborCalculator");

    // Instantiate the cell factory using the state class.
    Constructor<?> cellFactoryConstructor = CellFactory.class.getConstructor(Class.class);
    cellFactory = (CellFactory<?>) cellFactoryConstructor.newInstance(stateClass);

    // Instantiate the neighbor calculator.
    neighborCalculator = (NeighborCalculator<?>) neighborClass.getDeclaredConstructor().newInstance();

    // Create a deep copy of the grid from the configuration.
    List<List<CellRecord>> gridCopy = deepCopyGrid(configInfo.myGrid());
    grid = new Grid<>(gridCopy, cellFactory, neighborCalculator);

    // Instantiate the game logic using the grid and parameter record.
    gameLogic = (Logic<?>) logicClass.getDeclaredConstructor(Grid.class, ParameterRecord.class)
        .newInstance(grid, parameterRecord);

    return gameLogic;
  }

  /**
   * Creates a deep copy of the given grid.
   *
   * @param grid the original grid as a list of lists of CellRecord
   * @return a deep copy of the grid
   */
  private List<List<CellRecord>> deepCopyGrid(List<List<CellRecord>> grid) {
    List<List<CellRecord>> copy = new ArrayList<>();
    for (List<CellRecord> row : grid) {
      copy.add(new ArrayList<>(row));
    }
    return copy;
  }

  /**
   * Formats the simulation name based on the SimulationType. Converts the enum name to lowercase and then
   * capitalizes the first letter.
   *
   * @param type the SimulationType from the configuration
   * @return the formatted simulation name (e.g., "Fire" for FIRE)
   */
  private String formatSimulationName(SimulationType type) {
    String name = type.name().toLowerCase();
    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }
}

