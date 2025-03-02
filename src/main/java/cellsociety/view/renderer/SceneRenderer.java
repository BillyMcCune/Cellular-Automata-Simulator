package cellsociety.view.renderer;

import cellsociety.view.renderer.drawer.GridDrawer;
import cellsociety.view.renderer.drawer.HexagonGridDrawer;
import cellsociety.view.renderer.drawer.TriangleGridDrawer;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

/**
 * The SceneRenderer class is responsible for drawing the grid and cells on screen.
 * It now relies on the model (modelAPI) to determine the appropriate cell color.
 */
public class SceneRenderer {

  // Private: configure which GridDrawer to use (this can be made configurable if needed)
  private static Class<? extends GridDrawer> gridDrawerClass = SquareGridDrawer.class;

  /**
   * Draw the grid on the given pane with the given number of rows and columns.
   *
   * @param pane      The pane to draw the grid on.
   * @param numOfRows The number of rows in the grid.
   * @param numOfCols The number of columns in the grid.
   */
  public static void drawGrid(Pane pane, int numOfRows, int numOfCols) {
    GridDrawer.drawGrid(pane, numOfRows, numOfCols, gridDrawerClass);
  }

  /**
   * Draws a cell at the given position in the grid using the provided color.
   * This method assumes that the caller (for example, modelAPI) has determined the
   * appropriate color for the cell (e.g., via getCellColor).
   *
   * @param grid      The pane representing the grid.
   * @param rowCount  The number of columns in the grid (used to calculate index in a flat list).
   * @param row       The row index of the cell.
   * @param col       The column index of the cell.
   * @param colorName The color of the cell as a string (e.g., "RED" or "#FF0000").
   */
  public static void drawCell(Pane grid, int rowCount, int row, int col, String colorName) {
    // Calculate index based on row-major order
    int index = row * rowCount + col;
    if (index < 0 || index >= grid.getChildren().size()) {
      return;
    }
    Node node = grid.getChildren().get(index);
    Color cellColor;
    try {
      cellColor = Color.valueOf(colorName);
    } catch (IllegalArgumentException e) {
      cellColor = Color.WHITE;  // default to white if the provided colorName is invalid
    }
    if (node instanceof Shape) {
      ((Shape) node).setFill(cellColor);
    }
  }


}

