package cellsociety.model.modelAPI;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
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

public class CellColorManager {

  private Grid<?> grid;
  //crazy color stuff:
  private static final String PROPERTY_TO_DETECT = "coloredId";
  private static final long GOLDEN_RATIO_HASH_MULTIPLIER = 2654435761L;

  private static final Properties USER_STYLE_PREFERENCES = new Properties();

  static {
    try (InputStream in = ModelApi.class.getResourceAsStream(
        "/cellsociety/property/SimulationStyle.properties")) {
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
      // Convert the generated Color to a hex string.
      return uniqueColorGenerator(id);
    }

    // Otherwise, use the state's prefix to check for any other property-based color.
    String stateString = cell.getCurrentState().toString();
    String prefix =
        stateString.contains(".") ? stateString.substring(0, stateString.indexOf('.')) : "";
    for (Map.Entry<String, Double> entry : properties.entrySet()) {
      if (entry.getValue() != 0) {
        // Construct key like "AntState.searchingEntities"
        String propertyKey = prefix + "." + entry.getKey();
        String color = COLOR_MAPPING.getProperty(propertyKey);
        if (color != null && !"WHITE".equalsIgnoreCase(color)) {
          return color;
        }
      }
    }
    return null;
  }

  /**
   * Given a random id number, generates a random color. Uses a large prime number to ensure
   * scrambling and very few overlapping colors, while ensuring an id is the same color every single
   * time through simulations.
   *
   * @param id The id number.
   * @return A random Color based off of the id.
   */
  private static String uniqueColorGenerator(int id) {
    long scrambled = (GOLDEN_RATIO_HASH_MULTIPLIER * id) & 0xffffffffL;

    // Convert to HSB
    float hue = (scrambled % 360) / 360f;
    float sat = 0.5f + ((scrambled >> 8) % 50) / 100f;
    float bright = 0.5f + ((scrambled >> 16) % 50) / 100f;

    // Convert to RGB
    int rgb = java.awt.Color.HSBtoRGB(hue, sat, bright);
    int r = (rgb >> 16) & 0xFF;
    int g = (rgb >> 8) & 0xFF;
    int b = rgb & 0xFF;

    // Return the color as a hex string
    return String.format("#%02X%02X%02X", r, g, b);
  }

  /**
   * Retrieves the mapping of cell types to their default colors from a properties file.
   *
   * @param SimulationType the simulation type identifier
   * @return a map of cell type keys to their default color values
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public Map<String, String> getCellTypesAndDefaultColors(String SimulationType) {
    Map<String, String> possibleStates = new HashMap<>();
    try (InputStream input = new FileInputStream("CellColor.properties")) {
      Properties defaultColors = new Properties();
      defaultColors.load(input);
      for (String key : defaultColors.stringPropertyNames()) {
        possibleStates.put(key, defaultColors.getProperty(key));
      }
    } catch (IOException e) {
      throw new NoSuchElementException("error-getCellTypesAndDefaultColors");
    }
    return possibleStates;
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
      Properties simulationStyle = new Properties();
      File file = new File("SimulationStyle.properties");
      if (file.exists()) {
        try (InputStream input = new FileInputStream(file)) {
          simulationStyle.load(input);
        }
      }
      simulationStyle.setProperty(stateName, newColor);
      try (OutputStream output = new FileOutputStream(file)) {
        simulationStyle.store(output, "User-defined cell colors");
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
    try (InputStream input = new FileInputStream("SimulationStyle.properties")) {
      Properties simulationStyle = new Properties();
      simulationStyle.load(input);
      return simulationStyle.getProperty(stateName, getDefaultColorByState(stateName));
    } catch (IOException e) {
      return getDefaultColorByState(stateName);
    }
  }

  /**
   * Retrieves the default color for a given cell state from the properties file.
   *
   * @param stateName the cell state name
   * @return the default color as a hex string, or "WHITE" if not defined
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public String getDefaultColorByState(String stateName) {
    try (InputStream input = new FileInputStream("CellColor.properties")) {
      Properties defaultColors = new Properties();
      defaultColors.load(input);
      return defaultColors.getProperty(stateName, "WHITE"); // fallback to WHITE
    } catch (IOException e) {
      throw new NoSuchElementException("error-getDefaultColorByState");
    }
  }
}
