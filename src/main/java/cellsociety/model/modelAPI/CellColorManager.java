package cellsociety.model.modelAPI;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

public class CellColorManager {

  private Grid<?> grid;
  // Special property for generating unique colors:
  private static final String PROPERTY_TO_DETECT = "coloredId";
  private static final long GOLDEN_RATIO_HASH_MULTIPLIER = 2654435761L;

  private static final Properties USER_STYLE_PREFERENCES = new Properties();

  static {
    try (InputStream in = ModelApi.class.getResourceAsStream(
        "/cellsociety/property/SimulationStyle.properties")) {
      if (in == null) {
        throw new RuntimeException("SimulationStyle.properties resource not found");
      }
      USER_STYLE_PREFERENCES.load(in);
    } catch (IOException ex) {
      throw new RuntimeException("error-getting-user-defined-color-mapping");
    }
  }

  // Load the color mapping from the properties file once (assumes the file is in your resources folder).
  private static final Properties COLOR_MAPPING = new Properties();

  static {
    try (InputStream in = ModelApi.class.getResourceAsStream(
        "/cellsociety/property/CellColor.properties")) {
      if (in == null) {
        throw new RuntimeException("CellColor.properties resource not found");
      }
      COLOR_MAPPING.load(in);
    } catch (IOException ex) {
      throw new RuntimeException("error-getting-color-mapping");
    }
  }

  public CellColorManager(Grid<?> grid) {
    this.grid = grid;
  }

  public void setGrid(Grid<?> newGrid) {
    this.grid = newGrid;
  }

  /**
   * Returns the color for the cell at (row, col). First, the color is determined from the cell's
   * current state. If that color is WHITE, the method will check if any of the cell’s property
   * values (if nonzero) have an associated color.
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
   * Determines the color from the cell's current state. It uses the cell's state (for example,
   * "AntState.EMPTY" or "FireState.BURNING") as a key in the properties file.
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
   * Checks the cell's properties for a non-white color override.
   * <p>
   * First, it checks if the cell has the "coloredId" property. If so, it uses the id value to
   * generate a unique color. If not, it iterates through the remaining properties using a prefix-
   * based lookup.
   *
   * @param cell the cell whose properties are checked.
   * @return the first non-white color found from the properties; returns null if none exists.
   */
  public String getPropertyColor(Cell<?> cell) {
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
        String propertyKey = statePrefix + "." + entry.getKey();
        String color = COLOR_MAPPING.getProperty(propertyKey);
        if (color != null && !"WHITE".equalsIgnoreCase(color)) {
          return color;
        }
      }
    }
    return null;
  }

  /**
   * Given an id number, generates a consistent unique color.
   *
   * @param id The id number.
   * @return A hex string representing a color.
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
   * Only keys starting with simulationType + "." will be returned.
   *
   * @param simulationType the simulation type identifier (the prefix before the dot in the state name)
   * @return a map of cell type keys to their default color values; returns an empty map if none match
   * @throws NoSuchElementException if the properties file cannot be read
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

  private String createSimulationPrefixForSearchingColor(String simulationType) {
    if (simulationType == null) {
      return "null";
    }
    simulationType = simulationType.substring(0,1).toUpperCase() + simulationType.substring(1).toLowerCase();
    return simulationType + "State" + ".";
  }

  /**
   * Sets a new color preference for a given cell state.
   *
   * @param stateName the state name for which to set the new color
   * @param newColor  the new color value (hex string)
   * @throws NoSuchElementException if the color preference cannot be updated due to an I/O error
   */
  public void setNewColorPreference(String stateName, String newColor) {
    try {
      USER_STYLE_PREFERENCES.put(stateName, newColor);
      Properties simulationStyle = new Properties();
      // Load existing preferences from the SimulationStyle resource
      try (InputStream in = getClass().getResourceAsStream("/cellsociety/property/SimulationStyle.properties")) {
        if (in == null) {
          throw new NoSuchElementException("error-setNewColorPreference: SimulationStyle resource not found");
        }
        simulationStyle.load(in);
      }
      simulationStyle.setProperty(stateName, newColor);
      // Save updated preferences to the working directory
      File file = new File("SimulationStyle.properties");
      try (OutputStream out = new FileOutputStream(file)) {
        simulationStyle.store(out, "User-defined cell colors");
      }
    } catch (IOException e) {
      throw new NoSuchElementException("error-setNewColorPreference");
    }
  }

  /**
   * Retrieves the color preference for a given cell state from user-defined settings.
   *
   * @param stateName the cell state name
   * @return the color value as a hex string, or the default color if not set
   */
  public String getColorFromPreferences(String stateName) {
    try (InputStream input = getClass().getResourceAsStream("/cellsociety/property/SimulationStyle.properties")) {
      if (input == null) {
        return getDefaultColorByState(stateName);
      }
      Properties simulationStyle = new Properties();
      simulationStyle.load(input);
      return simulationStyle.getProperty(stateName, getDefaultColorByState(stateName));
    } catch (IOException e) {
      return getDefaultColorByState(stateName);
    }
  }

  /**
   * Retrieves the default color for a given cell state from the bundled properties file.
   *
   * @param stateName the cell state name
   * @return the default color as a hex string, or "WHITE" if not defined
   * @throws NoSuchElementException if the properties file cannot be read
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
