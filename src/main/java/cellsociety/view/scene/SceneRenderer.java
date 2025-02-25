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
 * The SceneRenderer class is responsible for rendering the simulation scene.
 * It provides methods to draw the grid and cells on the screen.
 *
 * @author Hsuan-Kai Liao
 */
public class SceneRenderer {

  public static final Color DEFAULT_BACKGROUND_COLOR = Color.DIMGRAY;
  public static final Color DEFAULT_BORDER_COLOR = Color.LIGHTGRAY;
  public static final int DEFAULT_CELL_SIZE = 20;
  public static final int DEFAULT_BORDER_SIZE = 1;

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
   * Draw a cell at the given position in the grid with the given state.
   * The grid is assumed to be a GridPane with rectangles initialized as children.
   * @param grid The grid to draw the cell on
   * @param row The dx of the cell
   * @param col The column of the cell
   * @param state The state of the cell
   */
  public static void drawCell(GridPane grid, int row, int col, Enum<?> state) {
    for (Node node : grid.getChildren()) {
      if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null &&
          GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {

        String stateName = state.getClass().getSimpleName() + "." + state.name();
        ((Rectangle) node).setFill(DEFAULT_CELL_COLORS.get(stateName));
        return;
      }
    }
  }

  /**
   * Draw a cell at the given position in the grid with the given state and properties.
   * @param grid The grid to draw the cell on
   * @param row The row of the cell
   * @param col The column of the cell
   * @param state The state of the cell
   * @param allProperties The properties of the cell
   */
  public static void drawParameters(GridPane grid, int row, int col, Enum<?> state, Map<String, Double> allProperties) {
    for (Node node : grid.getChildren()) {
      if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null &&
          GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {

        List<String> propertyKeys = getPropertyKeys(state.getClass().getSimpleName());
        List<String> propertiesToDraw = new ArrayList<>();
        if (allProperties != null && !allProperties.isEmpty()) {
          for (Map.Entry<String, Double> entry : allProperties.entrySet()) {
            if (propertyKeys.contains(entry.getKey())) {
              if (allProperties.get(entry.getKey()) > 0) {
                propertiesToDraw.add(entry.getKey());
              }
            }
          }
        }
        if (propertiesToDraw.isEmpty()) {
          return;
        }
        for (String property : propertiesToDraw) {
          String stateName = state.getClass().getSimpleName() + "." + property;
          ((Rectangle) node).setFill(DEFAULT_CELL_COLORS.get(stateName));
        }
        return;
      }
    }
  }

  /* PRIVATE HELPER METHODS */

  private static Map<String, Color> loadCellColorProperties() {
    Properties properties = new Properties();
    Map<String, Color> colorMap = new HashMap<>();

    try (InputStream input = SceneRenderer.class.getClassLoader().getResourceAsStream(DEFAULT_CELL_COLORS_FILE)) {
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
   * Returns a list of overlay keys for a given simulation state.
   * An property key is defined as the portion after the dot in a property key (e.g. "AntState.searchingEntities")
   * where the first character of the overlay key is lower-case.
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
}
