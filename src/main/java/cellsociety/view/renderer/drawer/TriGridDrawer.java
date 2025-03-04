package cellsociety.view.renderer.drawer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;

/**
 * A class that draws a triangular grid.
 *
 * @author Hsuan-Kai Liao
 */
public class TriGridDrawer extends GridDrawer {

  @Override
  protected void drawGridContents(Pane pane, int numOfRows, int numOfCols) {
    pane.getChildren().clear();

    // Get the side length and height of the triangle
    double sideLength = DEFAULT_CELL_SIZE;
    double height = Math.sqrt(3) * DEFAULT_CELL_SIZE / 2;

    // Center the grid
    double offsetX = sideLength / 2;
    double offsetY = height / 2;

    for (int i = 0; i < numOfRows; i++) {
      for (int j = 0; j < numOfCols; j++) {
        boolean isFacingUp = (i + j) % 2 == 0;
        drawTriangle(pane, j, i, sideLength, height, offsetX, offsetY, isFacingUp);
      }
    }
  }

  @Override
  protected void drawGridBound(Pane pane, int numOfRows, int numOfCols) {
    List<Double> points = new ArrayList<>();
    double sideLength = DEFAULT_CELL_SIZE;
    double height = Math.sqrt(3) * sideLength / 2;
    double totalWidth = numOfCols * sideLength / 2 + sideLength / 2;
    double totalHeight = numOfRows * height;
    double offsetX = sideLength / 2;
    double offsetY = height / 2;
    boolean isOddCols = (numOfCols % 2 == 1);

    // Helper method to add points
    BiConsumer<Double, Double> addPoint = (x, y) -> {
      points.add(x);
      points.add(y);
    };

    // Top border
    addPoint.accept(offsetX, offsetY);
    double newOffsetX = offsetX + totalWidth - (isOddCols ? 0 : sideLength / 2);
    addPoint.accept(newOffsetX, offsetY);

    // Right border
    for (int i = 0; i < numOfRows; i++) {
      double x = offsetX + totalWidth - (isOddCols ? sideLength / 2 : 0);
      double y = offsetY + i * height;
      if (i % 2 == 1) {
        x += isOddCols ? sideLength / 2 : -sideLength / 2;
      }
      addPoint.accept(x, y + height);
    }

    // Bottom border
    addPoint.accept(newOffsetX - (isOddCols ? sideLength / 2 : 0), offsetY + totalHeight);
    addPoint.accept(offsetX + (isOddCols ? sideLength / 2 : 0), offsetY + totalHeight);

    // Left border
    for (int i = numOfRows - 1; i > 0; i--) {
      double x = offsetX + (i % 2 == 1 ? sideLength / 2 : 0);
      double y = offsetY + i * height;
      addPoint.accept(x, y);
    }

    // Create boundary polygon
    addBoundary(pane, points);
  }

  /* PRIVATE HELPER METHODS */

  private void drawTriangle(Pane pane, int col, int row, double sideLength, double height,
      double offsetX, double offsetY, boolean isFacingUp) {
    double x1, y1, x2, y2, x3, y3;

    if (isFacingUp) {
      // Triangle (facing up)
      x1 = col * sideLength / 2;
      y1 = row * height;
      x2 = x1 + sideLength / 2;
      y2 = y1 + height;
      x3 = x1 + sideLength;
      y3 = y1;
    } else {
      // Triangle (facing down)
      x1 = col * sideLength / 2 + sideLength / 2;
      y1 = row * height;
      x2 = x1 - sideLength / 2;
      y2 = y1 + height;
      x3 = x1 + sideLength / 2;
      y3 = y1 + height;
    }

    // Offset the triangle
    x1 += offsetX;
    y1 += offsetY;
    x2 += offsetX;
    y2 += offsetY;
    x3 += offsetX;
    y3 += offsetY;

    // Create triangle
    Polygon triangle = new Polygon(x1, y1, x2, y2, x3, y3);
    triangle.setFill(DEFAULT_BACKGROUND_COLOR);
    triangle.setStrokeType(StrokeType.INSIDE);
    triangle.setStroke(DEFAULT_BORDER_COLOR);
    triangle.setStrokeWidth(DEFAULT_BORDER_SIZE);

    // Add triangle to the pane
    pane.getChildren().add(triangle);
  }

}
