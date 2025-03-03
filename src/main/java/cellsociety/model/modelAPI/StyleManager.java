package cellsociety.model.modelAPI;

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

  private static final String STYLE_PROPERTIES_FILE = "SimulationStyle.properties";
  private static final String NEIGHBOR_ARRANGEMENT_PROPERTY = "NEIGHBORARRANGEMENT.PREFERENCE";
  private static final String EDGEPOLICY_PROPERTY = "EDGEPOLICY.PREFERENCE";
  private static final String CELLSHAPE_PROPERTY = "CELLSHAPE.PREFERENCE";
  private static final String GRIDOUTLINE_PROPERTY = "GRIDOUTLINE.PREFERENCE";

  /**
   * Retrieves a list of possible neighbor arrangement values from the style properties file.
   *
   * @return a list of possible neighbor arrangement values
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public List<String> getPossibleNeighborArrangements() {
    List<String> arrangements = new ArrayList<>();
    Properties simulationStyle = loadProperties(STYLE_PROPERTIES_FILE);
    for (String key : simulationStyle.stringPropertyNames()) {
      if (key.startsWith("NEIGHBORARRANGEMENT.") && !key.equals(NEIGHBOR_ARRANGEMENT_PROPERTY)) {
        String value = simulationStyle.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
          arrangements.add(value.trim());
        }
      }
    }
    return arrangements;
  }

  /**
   * Retrieves a list of possible edge policy values from the style properties file.
   *
   * @return a list of possible edge policy values
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public List<String> getPossibleEdgePolicies() {
    List<String> edgePolicies = new ArrayList<>();
    Properties simulationStyle = loadProperties(STYLE_PROPERTIES_FILE);
    for (String key : simulationStyle.stringPropertyNames()) {
      if (key.startsWith("EDGEPOLICY.") && !key.equals(EDGEPOLICY_PROPERTY)) {
        String value = simulationStyle.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
          edgePolicies.add(value.trim());
        }
      }
    }
    return edgePolicies;
  }

  /**
   * Retrieves a list of possible cell shape values from the style properties file.
   *
   * @return a list of possible cell shape values
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public List<String> getPossibleCellShapes() {
    List<String> cellShapes = new ArrayList<>();
    Properties simulationStyle = loadProperties(STYLE_PROPERTIES_FILE);
    for (String key : simulationStyle.stringPropertyNames()) {
      if (key.startsWith("CELLSHAPE.") && !key.equals(CELLSHAPE_PROPERTY)) {
        String value = simulationStyle.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
          cellShapes.add(value.trim());
        }
      }
    }
    return cellShapes;
  }

  /**
   * Sets the edge policy preference in the style properties file.
   *
   * @param edgePolicy the new edge policy value
   * @throws NoSuchElementException if the properties file cannot be updated due to an I/O error
   */
  public void setEdgePolicy(String edgePolicy) {
    Properties simulationStyle = loadProperties(STYLE_PROPERTIES_FILE);
    simulationStyle.setProperty(EDGEPOLICY_PROPERTY, edgePolicy);
    storeProperties(simulationStyle, STYLE_PROPERTIES_FILE, "User-defined edge policy");
  }

  /**
   * Sets the neighbor arrangement preference in the style properties file.
   *
   * @param neighborArrangement the new neighbor arrangement value
   * @throws NoSuchElementException if the properties file cannot be updated due to an I/O error
   */
  public void setNeighborArrangement(String neighborArrangement) {
    Properties simulationStyle = loadProperties(STYLE_PROPERTIES_FILE);
    simulationStyle.setProperty(NEIGHBOR_ARRANGEMENT_PROPERTY, neighborArrangement);
    storeProperties(simulationStyle, STYLE_PROPERTIES_FILE, "User-defined neighbor arrangement");
  }

  /**
   * Sets the cell shape preference in the style properties file.
   *
   * @param cellShape the new cell shape value (e.g., "SQUARE", "HEXAGON")
   * @throws NoSuchElementException if the properties file cannot be updated due to an I/O error
   */
  public void setCellShape(String cellShape) {
    Properties simulationStyle = loadProperties(STYLE_PROPERTIES_FILE);
    simulationStyle.setProperty(CELLSHAPE_PROPERTY, cellShape);
    storeProperties(simulationStyle, STYLE_PROPERTIES_FILE, "User-defined cell shape");
  }

  /**
   * Sets the grid outline preference in the style properties file.
   *
   * @param wantsGridOutline true if the grid outline should be displayed; false otherwise
   * @throws NoSuchElementException if the properties file cannot be updated due to an I/O error
   */
  public void setGridOutlinePreference(boolean wantsGridOutline) {
    Properties simulationStyle = loadProperties(STYLE_PROPERTIES_FILE);
    simulationStyle.setProperty(GRIDOUTLINE_PROPERTY, String.valueOf(wantsGridOutline));
    storeProperties(simulationStyle, STYLE_PROPERTIES_FILE, "User-defined grid outline preference");
  }

  /**
   * Loads properties from the specified file.
   *
   * @param filePath the path of the properties file
   * @return a Properties object loaded with the file's content
   * @throws NoSuchElementException if the file cannot be read
   */
  private Properties loadProperties(String filePath) {
    Properties properties = new Properties();
    File file = new File(filePath);
    if (file.exists()) {
      try (InputStream input = new FileInputStream(file)) {
        properties.load(input);
      } catch (IOException e) {
        throw new NoSuchElementException("error-loading-properties:" + "," + filePath);
      }
    }
    return properties;
  }

  /**
   * Stores the provided properties to the specified file.
   *
   * @param properties the Properties object to store
   * @param filePath   the file path where the properties should be saved
   * @param comments   comments to include in the properties file
   * @throws NoSuchElementException if the properties file cannot be written
   */
  private void storeProperties(Properties properties, String filePath, String comments) {
    try (OutputStream output = new FileOutputStream(new File(filePath))) {
      properties.store(output, comments);
    } catch (IOException e) {
      throw new NoSuchElementException("error-storing-properties:" + "," + filePath);
    }
  }
}

