package cellsociety.model.modelAPI;

import cellsociety.model.data.Grid;
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

  private Grid<?> myGrid;
  private final File propertiesFile;

  public StyleManager() {
    // The external properties file in the working directory.
    propertiesFile = new File("SimulationStyle.properties");


    // If the external file doesn't exist, copy the default resource to this location.
    if (!propertiesFile.exists()) {
      try (InputStream input = getClass().getResourceAsStream("/cellsociety/property/SimulationStyle.properties");
          OutputStream output = new FileOutputStream(propertiesFile)) {
        if (input == null) {
          throw new NoSuchElementException("Default properties file not found in resources at /cellsociety/property/SimulationStyle.properties");
        }
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
          output.write(buffer, 0, bytesRead);
        }
      } catch (IOException e) {
        throw new NoSuchElementException("Error copying default properties file to working directory: " + e.getMessage());
      }
    }
  }

  /**
   * Loads the simulation style properties from the external file.
   *
   * @return the loaded Properties object
   * @throws NoSuchElementException if the properties file cannot be loaded
   */
  private Properties loadProperties() throws NoSuchElementException {
    Properties simulationStyle = new Properties();
    try (InputStream input = new FileInputStream(propertiesFile)) {
      simulationStyle.load(input);
    } catch (IOException e) {
      throw new NoSuchElementException("Error loading properties file from " + propertiesFile.getAbsolutePath() + ": " + e.getMessage());
    }
    return simulationStyle;
  }

  /**
   * Saves the simulation style properties to the external file.
   *
   * @param simulationStyle the Properties object to save
   * @throws NoSuchElementException if the properties file cannot be saved
   */
  private void saveProperties(Properties simulationStyle) throws NoSuchElementException {
    try (OutputStream output = new FileOutputStream(propertiesFile)) {
      simulationStyle.store(output, "User-defined cell colors");
    } catch (IOException e) {
      throw new NoSuchElementException("Error saving properties file to " + propertiesFile.getAbsolutePath() + ": " + e.getMessage());
    }
  }

  /**
   * Sets the neighbor arrangement preference.
   *
   * @param neighborArrangement the new neighbor arrangement value
   * @throws NoSuchElementException if the neighbor arrangement cannot be updated due to an I/O error
   */
  public void setNeighborArrangement(String neighborArrangement) throws NoSuchElementException {
    Properties simulationStyle = loadProperties();
    simulationStyle.setProperty(neighborArrangementProperty, neighborArrangement);
    saveProperties(simulationStyle);
  }

  /**
   * Retrieves a list of possible neighbor arrangements defined in the simulation style properties.
   *
   * @return a list of possible neighbor arrangement values
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public List<String> getPossibleNeighborArrangements() throws NoSuchElementException {
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
  public void setEdgePolicy(String edgePolicy) throws NoSuchElementException {
    Properties simulationStyle = loadProperties();
    simulationStyle.setProperty(edgePolicyProperty, edgePolicy);
    saveProperties(simulationStyle);
  }

  /**
   * Retrieves a list of possible edge policies defined in the simulation style properties.
   *
   * @return a list of possible edge policy values
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public List<String> getPossibleEdgePolicies() throws NoSuchElementException {
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
  public void setCellShape(String cellShape) throws NoSuchElementException {
    Properties simulationStyle = loadProperties();
    // Convert to uppercase before saving if that's the intended behavior.
    cellShape = cellShape.toUpperCase();
    simulationStyle.setProperty(cellShapeProperty, cellShape);
    saveProperties(simulationStyle);
  }

  /**
   * Sets the grid outline preference.
   *
   * @param wantsGridOutline true if the grid outline should be displayed, false otherwise
   * @throws NoSuchElementException if the preference cannot be updated due to an I/O error
   */
  public void setGridOutlinePreference(boolean wantsGridOutline) throws NoSuchElementException {
    Properties simulationStyle = loadProperties();
    simulationStyle.setProperty(gridOutlineProperty, String.valueOf(wantsGridOutline));
    saveProperties(simulationStyle);
  }

  /**
   * Retrieves the current grid outline preference.
   *
   * @return true if grid outline is preferred, false otherwise
   */
  public boolean getGridOutlinePreference() {
    Properties simulationStyle = loadProperties();
    boolean pref = Boolean.parseBoolean(simulationStyle.getProperty(gridOutlineProperty));
    return pref;
  }

  /**
   * Retrieves a list of possible cell shapes defined in the simulation style properties.
   *
   * @return a list of possible cell shape values
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public List<String> getPossibleCellShapes() throws NoSuchElementException {
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

  /**
   * Retrieves the current neighbor arrangement preference.
   *
   * @return the current neighbor arrangement value
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public String getNeighborArrangementPreference() throws NoSuchElementException {
    Properties simulationStyle = loadProperties();
    String pref = simulationStyle.getProperty(neighborArrangementProperty);
    return pref;
  }

  /**
   * Retrieves the current edge policy preference.
   *
   * @return the current edge policy value
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public String getEdgePolicyPreference() throws NoSuchElementException {
    Properties simulationStyle = loadProperties();
    String pref = simulationStyle.getProperty(edgePolicyProperty);
    return pref;
  }

  /**
   * Retrieves the current cell shape preference.
   *
   * @return the current cell shape value
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public String getCellShapePreference() throws NoSuchElementException {
    Properties simulationStyle = loadProperties();
    String pref = simulationStyle.getProperty(cellShapeProperty);
    return pref;
  }
}
