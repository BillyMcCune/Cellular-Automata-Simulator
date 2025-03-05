package cellsociety.view.renderer.drawer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

/**
 * A class that draws a square grid.
 *
 * @author Hsuan-Kai Liao
 */
public class SquareGridDrawer extends GridDrawer {

  @Override
  protected void drawGridContents(Pane pane, int numOfRows, int numOfCols) {
    pane.getChildren().clear();
    double cellSize = DEFAULT_CELL_SIZE;

    for (int i = 0; i < numOfRows; i++) {
      for (int j = 0; j < numOfCols; j++) {
        double x = j * cellSize;
        double y = i * cellSize;

        Rectangle cell = new Rectangle(x, y, cellSize, cellSize);
        cell.setFill(DEFAULT_BACKGROUND_COLOR);
        cell.setStroke(DEFAULT_BORDER_COLOR);
        cell.setStrokeWidth(DEFAULT_BORDER_SIZE);
        cell.setStrokeType(StrokeType.INSIDE);

        pane.getChildren().add(cell);
      }
    }
  }

  @Override
  protected void drawGridBound(Pane pane, int numOfRows, int numOfCols) {
    List<Double> points = new ArrayList<>();
    double cellSize = DEFAULT_CELL_SIZE;
    double totalWidth = numOfCols * cellSize;
    double totalHeight = numOfRows * cellSize;

    // Helper function to add points
    BiConsumer<Double, Double> addPoint = (x, y) -> {
      points.add(x);
      points.add(y);
    };

    // Define the four corners
    addPoint.accept(0.0, 0.0);
    addPoint.accept(totalWidth, 0.0);
    addPoint.accept(totalWidth, totalHeight);
    addPoint.accept(0.0, totalHeight);
    addPoint.accept(0.0, 0.0); // Close the boundary

    // Add the boundary to the pane
    addBoundary(pane, points);
  }
}
