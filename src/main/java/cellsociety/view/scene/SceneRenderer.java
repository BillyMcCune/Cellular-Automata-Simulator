package cellsociety.view.scene;

import cellsociety.logging.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

/**
 * The SceneRenderer class is responsible for rendering the simulation scene. It provides methods to
 * draw the grid and cells on the screen.
 *
 * @author Hsuan-Kai Liao
 */
public class SceneRenderer {

  public static final Color DEFAULT_BACKGROUND_COLOR = Color.DIMGRAY;
  public static final Color DEFAULT_BORDER_COLOR = Color.LIGHTGRAY;
  public static final int DEFAULT_CELL_SIZE = 20;
  public static final int DEFAULT_BORDER_SIZE = 1;
  public static final String PROPERTY_TO_DETECT = "coloredId";
  private static final long GOLDEN_RATIO_HASH_MULTIPLIER = 2654435761L;

  // Load cell colors from properties file
  public static final String DEFAULT_CELL_COLORS_FILE = "cellsociety/property/CellColor.properties";
  public static final Map<String, Color> DEFAULT_CELL_COLORS = loadCellColorProperties();

  /* PUBLIC METHODS */

  /**
   * Draw a grid on the given GridPane with the given number of rows and columns.
   * @param grid The grid to draw on
   * @param numOfRows The number of rows in the grid
   * @param numOfCols The number of columns in the grid
   */
  public static void drawGrid(GridPane grid, int numOfRows, int numOfCols) {
    grid.getChildren().clear();

    for (int i = 0; i < numOfCols; i++) {
      for (int j = 0; j < numOfRows; j++) {
        Rectangle square = new Rectangle(DEFAULT_CELL_SIZE, DEFAULT_CELL_SIZE);
        square.setFill(DEFAULT_BACKGROUND_COLOR);
        square.setStroke(DEFAULT_BORDER_COLOR);
        square.setStrokeType(StrokeType.INSIDE);
        square.setStrokeWidth(DEFAULT_BORDER_SIZE);
        GridPane.setMargin(square, new Insets(0));
        grid.add(square, i, j);
      }
    }
  }

  /**
   * Draw a cell at the given position in the grid with the given state. The grid is assumed to be a
   * GridPane with rectangles initialized as children.
   *
   * @param grid  The grid to draw the cell on
   * @param row   The dx of the cell
   * @param col   The column of the cell
   * @param state The state of the cell
   */
  public static void drawCell(GridPane grid, int row, int col, Enum<?> state) {
    for (Node node : grid.getChildren()) {
      if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null &&
          GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {

        String stateName = state.getClass().getSimpleName() + "." + state.name();
        Color cellColor = DEFAULT_CELL_COLORS.get(stateName);
        if (cellColor == null) {
          return;
        }
        ((Rectangle) node).setFill(cellColor);
        return;
      }
    }
  }

  /**
   * Draw a cell at the given position in the grid with the given state and properties.
   *
   * @param grid          The grid to draw the cell on
   * @param row           The row of the cell
   * @param col           The column of the cell
   * @param state         The state of the cell
   * @param allProperties The properties of the cell
   */
  public static void drawParameters(GridPane grid, int row, int col, Enum<?> state,
      Map<String, Double> allProperties) {
    for (Node node : grid.getChildren()) {

      if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null &&
          GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {

        if (allProperties != null && !allProperties.isEmpty()) {
          if (allProperties.containsKey(PROPERTY_TO_DETECT)) {
            ((Rectangle) node).setFill(
                uniqueColorGenerator(allProperties.get(PROPERTY_TO_DETECT).intValue()));
            continue;
          }
        }

        List<String> propertyKeys = getPropertyKeys(state.getClass().getSimpleName());
        if (allProperties != null && !allProperties.isEmpty()) {
          for (Map.Entry<String, Double> entry : allProperties.entrySet()) {
            if (propertyKeys.contains(entry.getKey()) && allProperties.get(entry.getKey()) > 0) {
              String stateName = state.getClass().getSimpleName() + "." + entry.getKey();
              ((Rectangle) node).setFill(DEFAULT_CELL_COLORS.get(stateName));
            }
          }
        }
      }
    }
  }

  /* PRIVATE HELPER METHODS */

  private static Map<String, Color> loadCellColorProperties() {
    Properties properties = new Properties();
    Map<String, Color> colorMap = new HashMap<>();

    try (InputStream input = SceneRenderer.class.getClassLoader()
        .getResourceAsStream(DEFAULT_CELL_COLORS_FILE)) {
      properties.load(input);
      for (String key : properties.stringPropertyNames()) {
        String colorValue = properties.getProperty(key);
        try {
          colorMap.put(key, Color.valueOf(colorValue));
        } catch (IllegalArgumentException e) {
          colorMap.put(key, Color.web(colorValue));
        }
      }
    } catch (IOException e) {
      Log.error("Failed to load cell color properties file: " + DEFAULT_CELL_COLORS_FILE);
      colorMap.put("DEFAULT", Color.BLACK);
    }

    return colorMap;
  }

  /**
   * Returns a list of overlay keys for a given simulation state. An property key is defined as the
   * portion after the dot in a property key (e.g. "AntState.searchingEntities") where the first
   * character of the overlay key is lower-case.
   */
  private static List<String> getPropertyKeys(String simulationStateName) {
    List<String> propertyKeys = new ArrayList<>();
    String prefix = simulationStateName + ".";
    for (String key : DEFAULT_CELL_COLORS.keySet()) {
      if (key.startsWith(prefix)) {
        String suffix = key.substring(prefix.length());
        if (!suffix.isEmpty() && Character.isLowerCase(suffix.charAt(0))) {
          propertyKeys.add(suffix);
        }
      }
    }
    return propertyKeys;
  }

  /**
   * Given a random id number, generates a random color. Uses a large prime number to ensure
   * scrambling and very few overlapping colors, while ensuring an id is the same color every single
   * time through simulations.
   *
   * @param id The id number
   * @return A random Color based off of the id
   */
  public static Color uniqueColorGenerator(int id) {
    long scrambled = (GOLDEN_RATIO_HASH_MULTIPLIER * id) & 0xffffffffL;
    double hue = scrambled % 360;
    // Randomly generate values for saturation and brightness, with range 0.5 - 1
    double sat = 0.5 + ((scrambled >> 8) % 50) / 100.0;
    double bright = 0.5 + ((scrambled >> 16) % 50) / 100.0;
    return Color.hsb(hue, sat, bright);
  }
}
