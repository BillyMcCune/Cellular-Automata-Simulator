package cellsociety.view.scene;

import cellsociety.model.data.states.FireState;
import cellsociety.model.data.states.LifeState;
import cellsociety.model.data.states.PercolationState;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SceneRenderer {

  public static final Color DEFAULT_BACKGROUND_COLOR = Color.DIMGRAY;
  public static final Color DEFAULT_BORDER_COLOR = Color.BLACK;

  /// The default colors for each cell state
  public static final Map<Enum<?>, Color> DEFAULT_CELL_COLORS = Map.of(
      FireState.EMPTY, Color.WHITE,
      FireState.TREE, Color.GREEN,
      FireState.BURNING, Color.RED,

      LifeState.DEAD, Color.WHITE,
      LifeState.ALIVE, Color.LIGHTBLUE,

      PercolationState.BLOCKED, Color.BLACK,
      PercolationState.OPEN, Color.WHITE,
      PercolationState.PERCOLATED, Color.LIGHTBLUE
  );

  /**
   * Draw a grid on the given GridPane with the given number of rows and columns.
   * @param grid The grid to draw on
   * @param numOfRows The number of rows in the grid
   * @param numOfCols The number of columns in the grid
   */
  public static void drawGrid(GridPane grid, int numOfRows, int numOfCols) {
    grid.getChildren().clear();

    double cellWidth = Math.floor(grid.getWidth() / numOfCols - 3);
    double cellHeight = Math.floor(grid.getHeight() / numOfRows - 3);

    for (int i = 0; i < numOfCols; i++) {
      for (int j = 0; j < numOfRows; j++) {
        Rectangle square = new Rectangle(cellWidth, cellHeight);
        square.setFill(DEFAULT_BACKGROUND_COLOR);
        square.setStroke(DEFAULT_BORDER_COLOR);
        square.setStrokeWidth(3);
        grid.add(square, i, j);
      }
    }
  }

  /**
   * Draw a cell at the given position in the grid with the given state.
   * The grid is assumed to be a GridPane with rectangles initialized as children.
   * @param grid The grid to draw the cell on
   * @param row The row of the cell
   * @param col The column of the cell
   * @param state The state of the cell
   */
  public static void drawCell(GridPane grid, int row, int col, Enum<?> state) {
    for (Node node : grid.getChildren()) {
      if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null &&
          GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
        ((Rectangle) node).setFill(DEFAULT_CELL_COLORS.get(state));
        return;
      }
    }
  }


}
