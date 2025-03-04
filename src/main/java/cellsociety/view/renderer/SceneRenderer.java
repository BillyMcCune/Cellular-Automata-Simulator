package cellsociety.view.renderer;

import cellsociety.view.renderer.drawer.GridDrawer;
import cellsociety.view.renderer.drawer.SquareGridDrawer;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

/**
 * The SceneRenderer class is responsible for drawing the grid and cells on screen. It now relies on
 * the model (modelAPI) to determine the appropriate cell color.
 */
public class SceneRenderer {

  /**
   * Draw the grid on the given pane with the given number of rows and columns.
   *
   * @param pane      The pane to draw the grid on.
   * @param numOfRows The number of rows in the grid.
   * @param numOfCols The number of columns in the grid.
   * @param gridDrawerClass The class of the GridDrawer to use.
   */
  public static <T extends GridDrawer> void drawGrid(Pane pane, int numOfRows, int numOfCols, Class<T> gridDrawerClass) {
    GridDrawer.drawGrid(pane, numOfRows, numOfCols, gridDrawerClass);
  }

  /**
   * Draws a cell at the given position in the grid using the provided color. This method assumes
   * that the caller (for example, modelAPI) has determined the appropriate color for the cell
   * (e.g., via getCellColor).
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

