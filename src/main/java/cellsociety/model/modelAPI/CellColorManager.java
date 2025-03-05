package cellsociety.model.modelAPI;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * @author Billy McCune
 * Manages cell color configuration for a simulation grid.
 * <p>
 * This class is responsible for determining and managing the colors of individual cells based on their
 * current state and properties. It supports:
 * <ul>
 *   <li>Mapping cell states to default colors via a bundled properties file.</li>
 *   <li>Overriding these colors using user-defined style preferences loaded from a working directory file.</li>
 *   <li>Generating unique colors for cells with a special "coloredId" property.</li>
 * </ul>
 * The color mappings and style preferences allow for flexible configuration and dynamic updates during simulation.
 * </p>
 */
public class CellColorManager {

  private Grid<?> grid;
  // Special property for generating unique colors:
  private static final String PROPERTY_TO_DETECT = "coloredId";
  private static final long GOLDEN_RATIO_HASH_MULTIPLIER = 2654435761L;

  private static final Properties USER_STYLE_PREFERENCES = new Properties();

  // Load the simulation style preferences from the working directory file instead of the bundled resource.
  static {
    File styleFile = new File("SimulationStyle.properties");
    try (InputStream in = new FileInputStream(styleFile)) {
      USER_STYLE_PREFERENCES.load(in);
    } catch (IOException ex) {
      throw new RuntimeException("Error reading SimulationStyle.properties from working directory", ex);
    }
  }

  // Load the color mapping from the bundled properties file.
  private static final Properties COLOR_MAPPING = new Properties();

  static {
    try (InputStream in = CellColorManager.class.getResourceAsStream("/cellsociety/property/CellColor.properties")) {
      if (in == null) {
        throw new RuntimeException("CellColor.properties resource not found");
      }
      COLOR_MAPPING.load(in);
    } catch (IOException ex) {
      throw new RuntimeException("Error getting color mapping", ex);
    }
  }

  /**
   * Constructs a new CellColorManager with the specified simulation grid.
   *
   * @param grid the simulation grid containing cells
   */
  public CellColorManager(Grid<?> grid) {
    this.grid = grid;
  }

  /**
   * Sets a new grid for which cell colors will be managed.
   *
   * @param newGrid the new grid instance
   */
  public void setGrid(Grid<?> newGrid) {
    this.grid = newGrid;
  }

  /**
   * Returns the color for the cell at the specified (row, col) position.
   * <p>
   * The color is determined by first retrieving the color corresponding to the cell's current state.
   * If the state color is "WHITE", it then checks the cell's properties for an override color.
   * </p>
   *
   * @param row              the row index of the cell
   * @param col              the column index of the cell
   * @param wantDefaultColor if {@code true} the default color mapping is used; otherwise user-defined style is used
   * @return the hex color string for the cell, or {@code null} if the specified position is out-of-bounds
   */
  public String getCellColor(int row, int col, boolean wantDefaultColor) {
    if (col >= grid.getNumCols() || row >= grid.getNumRows()) {
      return null;
    }
    Cell<?> cell = grid.getCell(row, col);
    String stateColor = getStateColor(cell, wantDefaultColor);
    if (!"WHITE".equalsIgnoreCase(stateColor)) {
      return stateColor;
    }
    String propertyColor = getPropertyColor(cell);
    return propertyColor != null ? propertyColor : stateColor;
  }

  /**
   * Determines the color associated with the cell's current state.
   * <p>
   * It retrieves the state's value and class name, constructs a key (e.g., "FireState.BURNING"),
   * and uses that key to find a matching color in the properties file.
   * </p>
   *
   * @param cell             the cell whose state color is to be determined
   * @param wantDefaultColor if {@code true} the default color mapping is used; otherwise user-defined style is used
   * @return the hex color string corresponding to the cell's state; defaults to "WHITE" if no mapping is found
   */
  private String getStateColor(Cell<?> cell, boolean wantDefaultColor) {
    // Get the state value (e.g., "TREE" or "BURNING")
    String stateValue = cell.getCurrentState().toString();
    // Get the simple name of the cell state class (e.g., "FireState")
    String statePrefix = cell.getCurrentState().getClass().getSimpleName();
    // Construct the full key (e.g., "FireState.TREE")
    String key = statePrefix + "." + stateValue;
    // If the mapping is not found, default to "WHITE"
    if (!wantDefaultColor) {
      return USER_STYLE_PREFERENCES.getProperty(key, "WHITE");
    }
    return COLOR_MAPPING.getProperty(key, "WHITE");
  }

  /**
   * Checks the cell's properties for a color override.
   * <p>
   * First, it checks if the cell has the special "coloredId" property. If present and nonzero,
   * it generates a unique color using that id. Otherwise, it iterates over the other properties
   * and looks up colors based on a prefix-based key.
   * </p>
   *
   * @param cell the cell whose properties are to be checked
   * @return the first non-white hex color string found from the properties, or {@code null} if none exists
   */
  public String getPropertyColor(Cell<?> cell) {
    if (cell == null) {
      return null;
    }
    Map<String, Double> properties = cell.getAllProperties();

    // Check if the special "coloredId" property exists.
    if (properties.containsKey(PROPERTY_TO_DETECT) && properties.get(PROPERTY_TO_DETECT) != 0) {
      int id = properties.get(PROPERTY_TO_DETECT).intValue();
      return uniqueColorGenerator(id);
    }

    // Otherwise, use the state's prefix to check for any other property-based color.
    String statePrefix = cell.getCurrentState().getClass().getSimpleName();
    for (Map.Entry<String, Double> entry : properties.entrySet()) {
      if (entry.getValue() != 0) {
        // Construct key like "AntState.searchingEntities"
        String propertyKeyForTypeOne = statePrefix + "." + entry.getKey();
        String colorTypeOne = COLOR_MAPPING.getProperty(propertyKeyForTypeOne);
        if (colorTypeOne != null && !"WHITE".equalsIgnoreCase(colorTypeOne)) {
          return colorTypeOne;
        }

        String propertyKeyForTypeTwo = statePrefix + "." + entry.getKey() + "." + entry.getValue().intValue();
        String colorTypeTwo = COLOR_MAPPING.getProperty(propertyKeyForTypeTwo);
        if (colorTypeTwo != null && !"WHITE".equalsIgnoreCase(colorTypeTwo)) {
          return colorTypeTwo;
        }
      }
    }
    return null;
  }

  /**
   * Generates a consistent unique color based on an id number.
   * <p>
   * The method uses the golden ratio multiplier to scramble the id and then converts the result to an HSB color,
   * which is then transformed into an RGB hex string.
   * </p>
   *
   * @param id the id number used to generate the color
   * @return a hex string representing the generated color (e.g., "#A1B2C3")
   */
  private static String uniqueColorGenerator(int id) {
    long scrambled = (GOLDEN_RATIO_HASH_MULTIPLIER * id) & 0xffffffffL;

    // Convert to HSB
    float hue = (scrambled % 360) / 360f;
    float sat = 0.5f + ((scrambled >> 8) % 50) / 100f;
    float bright = 0.5f + ((scrambled >> 16) % 50) / 100f;

    int rgb = Color.HSBtoRGB(hue, sat, bright);
    int r = (rgb >> 16) & 0xFF;
    int g = (rgb >> 8) & 0xFF;
    int b = rgb & 0xFF;
    return String.format("#%02X%02X%02X", r, g, b);
  }

  /**
   * Retrieves the mapping of cell types to their default colors from the bundled properties file.
   * <p>
   * Only keys starting with a prefix corresponding to the simulation type (e.g., "FireState.")
   * will be returned.
   * </p>
   *
   * @param simulationType the simulation type identifier (the prefix before the dot in the state name)
   * @return a map of cell type keys to their default color values; returns an empty map if no matching keys are found
   * @throws NoSuchElementException if the properties file cannot be read or is not found
   */
  public Map<String, String> getCellTypesAndDefaultColors(String simulationType) {
    Map<String, String> possibleStates = new HashMap<>();
    try (InputStream input = getClass().getResourceAsStream("/cellsociety/property/CellColor.properties")) {
      if (input == null) {
        throw new NoSuchElementException("error-getCellTypesAndDefaultColors: resource not found");
      }
      Properties defaultColors = new Properties();
      defaultColors.load(input);
      String prefix = createSimulationPrefixForSearchingColor(simulationType);
      for (String key : defaultColors.stringPropertyNames()) {
        if (key.startsWith(prefix)) {
          possibleStates.put(key, defaultColors.getProperty(key));
        }
      }
    } catch (IOException e) {
      throw new NoSuchElementException("error-getCellTypesAndDefaultColors");
    }
    return possibleStates;
  }

  /**
   * Creates a prefix for searching color mappings based on the simulation type.
   * <p>
   * The simulation type string is capitalized (first letter uppercase, rest lowercase) and appended with "State.".
   * For example, "fire" becomes "FireState.".
   * </p>
   *
   * @param simulationType the simulation type string
   * @return the formatted prefix string for searching color mappings; returns "null" if the input is null
   */
  private String createSimulationPrefixForSearchingColor(String simulationType) {
    if (simulationType == null) {
      return "null";
    }
    simulationType = simulationType.substring(0,1).toUpperCase() + simulationType.substring(1).toLowerCase();
    return simulationType + "State" + ".";
  }

  /**
   * Sets a new color preference for a given cell state.
   * <p>
   * This method updates the user-defined style preferences by modifying the local properties file.
   * It first updates the in-memory preferences and then saves the change to the "SimulationStyle.properties" file.
   * </p>
   *
   * @param stateName the cell state name for which to set the new color
   * @param newColor  the new color value (hex string)
   * @throws NoSuchElementException if the color preference cannot be updated due to an I/O error
   */
  public void setNewColorPreference(String stateName, String newColor) {
    try {
      USER_STYLE_PREFERENCES.put(stateName, newColor);
      Properties simulationStyle = new Properties();
      // Load existing preferences from the working directory file
      File styleFile = new File("SimulationStyle.properties");
      try (InputStream in = new FileInputStream(styleFile)) {
        simulationStyle.load(in);
      }
      simulationStyle.setProperty(stateName, newColor);
      // Save updated preferences to the working directory
      try (OutputStream out = new FileOutputStream(styleFile)) {
        simulationStyle.store(out, "User-defined cell colors");
      }
    } catch (IOException e) {
      throw new NoSuchElementException("error-setNewColorPreference");
    }
  }

  /**
   * Retrieves the color preference for a given cell state from user-defined settings.
   * <p>
   * If no user-defined color exists for the specified state, the default color from the bundled properties
   * file is returned.
   * </p>
   *
   * @param stateName the cell state name
   * @return the hex color string from user preferences, or the default color if not set
   */
  public String getColorFromPreferences(String stateName) {
    File styleFile = new File("SimulationStyle.properties");
    try (InputStream input = new FileInputStream(styleFile)) {
      Properties simulationStyle = new Properties();
      simulationStyle.load(input);
      return simulationStyle.getProperty(stateName, getDefaultColorByState(stateName));
    } catch (IOException e) {
      return getDefaultColorByState(stateName);
    }
  }

  /**
   * Retrieves the default color for a given cell state from the bundled properties file.
   * <p>
   * If the state is not defined in the properties file, "WHITE" is returned as a fallback.
   * </p>
   *
   * @param stateName the cell state name
   * @return the default hex color string for the cell state, or "WHITE" if not defined
   * @throws NoSuchElementException if the properties file cannot be read or is not found
   */
  public String getDefaultColorByState(String stateName) {
    try (InputStream input = getClass().getResourceAsStream("/cellsociety/property/CellColor.properties")) {
      if (input == null) {
        throw new NoSuchElementException("error-getDefaultColorByState: resource not found");
      }
      Properties defaultColors = new Properties();
      defaultColors.load(input);
      return defaultColors.getProperty(stateName, "WHITE"); // fallback to WHITE
    } catch (IOException e) {
      throw new NoSuchElementException("error-getDefaultColorByState");
    }
  }
}

