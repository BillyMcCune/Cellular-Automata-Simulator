package cellsociety.view.scene;

import cellsociety.model.data.states.FireState;
import cellsociety.model.data.states.LifeState;
import cellsociety.model.data.states.PercolationState;
import cellsociety.model.data.states.SegregationState;
import java.util.Map;
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

  /// The default colors for each cell state
  public static final Map<Enum<?>, Color> DEFAULT_CELL_COLORS = Map.ofEntries(
      Map.entry(FireState.EMPTY, Color.WHITE),
      Map.entry(FireState.TREE, Color.GREEN),
      Map.entry(FireState.BURNING, Color.RED),

      Map.entry(LifeState.DEAD, Color.WHITE),
      Map.entry(LifeState.ALIVE, Color.LIGHTBLUE),

      Map.entry(PercolationState.BLOCKED, Color.BLACK),
      Map.entry(PercolationState.OPEN, Color.WHITE),
      Map.entry(PercolationState.PERCOLATED, Color.LIGHTBLUE),

      Map.entry(SegregationState.OPEN, Color.WHITE),
      Map.entry(SegregationState.RED, Color.RED),
<<<<<<< HEAD
      Map.entry(SegregationState.BLUE, Color.BLUE)
=======
      Map.entry(SegregationState.BLUE, Color.BLUE),

      // Wator
      Map.entry(WatorState.OPEN, Color.WHITE),
      Map.entry(WatorState.FISH, Color.LIMEGREEN),
      Map.entry(WatorState.SHARK, Color.BLUE)
>>>>>>> logic
  );

  /**
   * Draw a grid on the given GridPane with the given number of rows and columns.
   * @param grid The grid to draw on
   * @param numOfRows The number of rows in the grid
   * @param numOfCols The number of columns in the grid
   */
  public static void drawGrid(GridPane grid, int numOfRows, int numOfCols) {
    grid.getChildren().clear();
    grid.setStyle("-fx-border-color: black; -fx-border-width: " + 3 * DEFAULT_BORDER_SIZE + "; -fx-border-style: solid;");

    for (int i = 0; i < numOfCols; i++) {
      for (int j = 0; j < numOfRows; j++) {
        Rectangle square = new Rectangle(DEFAULT_CELL_SIZE, DEFAULT_CELL_SIZE);
        square.setFill(DEFAULT_BACKGROUND_COLOR);
        square.setStroke(DEFAULT_BORDER_COLOR);
        square.setStrokeType(StrokeType.INSIDE);
        square.setStrokeWidth(DEFAULT_BORDER_SIZE);
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
