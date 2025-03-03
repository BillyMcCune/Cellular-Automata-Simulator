package cellsociety.model.modelAPI;

import cellsociety.model.data.constants.EdgeType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.neighbors.NeighborCalculator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * The StyleManager class is responsible for managing simulation style preferences.
 * <p>
 * It provides methods to retrieve possible values for neighbor arrangements, edge policies,
 * and cell shapes from the style properties file. It also supports updating style preferences
 * such as edge policy, neighbor arrangement, cell shape, and grid outline preference.
 * </p>
 */
public class StyleManager {

  // Style property keys:
  private final String gridOutlineProperty = "GRIDOUTLINE.PREFERENCE";
  private final String edgePolicyProperty = "EDGEPOLICY.PREFERENCE";
  private final String neighborArrangementProperty = "NEIGHBORARRANGEMENT.PREFERENCE";
  private final String cellShapeProperty = "CELLSHAPE.PREFERENCE";

  private NeighborCalculator<?> myNeighborCalculator;

  public StyleManager(NeighborCalculator neighborCalculator) {
    this.myNeighborCalculator = neighborCalculator;
  }

  /**
   * Loads the simulation style properties. If a file named "SimulationStyle.properties" exists
   * in the working directory, it is loaded from there. Otherwise, the default properties bundled
   * in the classpath at "/cellsociety/property/SimulationStyle.properties" are loaded.
   *
   * @return the loaded Properties object
   * @throws NoSuchElementException if the properties cannot be loaded
   */
  private Properties loadProperties() {
    Properties simulationStyle = new Properties();
    File file = new File("SimulationStyle.properties");
    if (file.exists()) {
      try (InputStream input = new FileInputStream(file)) {
        simulationStyle.load(input);
      } catch (IOException e) {
        throw new NoSuchElementException("error-loading properties file from file system");
      }
    } else {
      try (InputStream input = getClass().getResourceAsStream("/cellsociety/property/SimulationStyle.properties")) {
        if (input == null) {
          throw new NoSuchElementException("error-loading properties file from resource");
        }
        simulationStyle.load(input);
      } catch (IOException e) {
        throw new NoSuchElementException("error-loading properties file from resource");
      }
    }
    return simulationStyle;
  }

  /**
   * Saves the simulation style properties to a file named "SimulationStyle.properties" in the working directory.
   *
   * @param simulationStyle the Properties object to save
   * @throws NoSuchElementException if the properties cannot be saved
   */
  private void saveProperties(Properties simulationStyle) {
    File file = new File("SimulationStyle.properties");
    try (OutputStream output = new FileOutputStream(file)) {
      simulationStyle.store(output, "User-defined cell colors");
    } catch (IOException e) {
      throw new NoSuchElementException("error-saving properties file");
    }
  }

  /**
   * Sets the neighbor arrangement preference.
   *
   * @param neighborArrangement the new neighbor arrangement value
   * @throws NoSuchElementException if the neighbor arrangement cannot be updated due to an I/O error
   */
  public void setNeighborArrangement(String neighborArrangement) {
    Properties simulationStyle = loadProperties();
    simulationStyle.setProperty(neighborArrangementProperty, neighborArrangement);
    myNeighborCalculator.setNeighborType(NeighborType.valueOf(neighborArrangement));
    saveProperties(simulationStyle);
  }

  /**
   * Retrieves a list of possible neighbor arrangements defined in the simulation style properties.
   *
   * @return a list of possible neighbor arrangement values
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public List<String> getPossibleNeighborArrangements() {
    List<String> arrangements = new ArrayList<>();
    Properties simulationStyle = loadProperties();
    for (String key : simulationStyle.stringPropertyNames()) {
      if (key.startsWith("NEIGHBORARRANGEMENT.") && !key.equals("NEIGHBORARRANGEMENT.PREFERENCE")) {
        String value = simulationStyle.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
          arrangements.add(value.trim());
        }
      }
    }
    return arrangements;
  }

  /**
   * Sets the edge policy for the simulation.
   *
   * @param edgePolicy the new edge policy value
   * @throws NoSuchElementException if the edge policy cannot be updated due to an I/O error
   */
  public void setEdgePolicy(String edgePolicy) {
    Properties simulationStyle = loadProperties();
    simulationStyle.setProperty(edgePolicyProperty, edgePolicy);
    myNeighborCalculator.setEdgeType(EdgeType.valueOf(edgePolicy));
    saveProperties(simulationStyle);
  }

  /**
   * Retrieves a list of possible edge policies defined in the simulation style properties.
   *
   * @return a list of possible edge policy values
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public List<String> getPossibleEdgePolicies() {
    List<String> edgePolicies = new ArrayList<>();
    Properties simulationStyle = loadProperties();
    for (String key : simulationStyle.stringPropertyNames()) {
      if (key.startsWith("EDGEPOLICY.") && !key.equals("EDGEPOLICY.PREFERENCE")) {
        String value = simulationStyle.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
          edgePolicies.add(value.trim());
        }
      }
    }
    return edgePolicies;
  }

  /**
   * Sets the cell shape preference.
   *
   * @param cellShape the new cell shape value (e.g., "SQUARE", "HEXAGON")
   * @throws NoSuchElementException if the cell shape cannot be updated due to an I/O error
   */
  public void setCellShape(String cellShape) {
    Properties simulationStyle = loadProperties();
    simulationStyle.setProperty(cellShapeProperty, cellShape);
    cellShape = cellShape.toUpperCase();
    myNeighborCalculator.setShape(GridShape.valueOf(cellShape));
    saveProperties(simulationStyle);
  }

  /**
   * Sets the grid outline preference.
   *
   * @param wantsGridOutline true if the grid outline should be displayed, false otherwise
   * @throws NoSuchElementException if the preference cannot be updated due to an I/O error
   */
  public void setGridOutlinePreference(boolean wantsGridOutline) {
    Properties simulationStyle = loadProperties();
    simulationStyle.setProperty(gridOutlineProperty, String.valueOf(wantsGridOutline));
    saveProperties(simulationStyle);
  }

  /**
   * Retrieves a list of possible cell shapes defined in the simulation style properties.
   *
   * @return a list of possible cell shape values
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public List<String> getPossibleCellShapes() {
    List<String> cellShapes = new ArrayList<>();
    Properties simulationStyle = loadProperties();
    for (String key : simulationStyle.stringPropertyNames()) {
      if (key.startsWith("CELLSHAPE.") && !key.equals("CELLSHAPE.PREFERENCE")) {
        String value = simulationStyle.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
          cellShapes.add(value.trim());
        }
      }
    }
    return cellShapes;
  }
}
