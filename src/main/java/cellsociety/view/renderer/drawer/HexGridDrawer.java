package cellsociety.view.renderer.drawer;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;

/**
 * A class that draws a hexagonal grid.
 *
 * @author Hsuan-Kai Liao
 */
public class HexGridDrawer extends GridDrawer {
  private static final double DEFAULT_HEX_CELL_SIZE = DEFAULT_CELL_SIZE / 1.5;

  @Override
  protected void drawGridContents(Pane pane, int numOfRows, int numOfCols) {
    pane.getChildren().clear();
    double sideLength = DEFAULT_HEX_CELL_SIZE;
    double width = 2 * sideLength;
    double height = Math.sqrt(3) * sideLength;
    double offsetX = sideLength;
    double offsetY = height / 2;

    for (int row = 0; row < numOfRows; row++) {
      for (int col = 0; col < numOfCols; col++) {
        double x = col * (width * 3 / 4);
        double y = row * height + (col % 2 == 1 ? height / 2 : 0);
        drawHexagon(pane, x + offsetX, y + offsetY, sideLength);
      }
    }
  }

  @Override
  protected void drawGridBound(Pane pane, int numOfRows, int numOfCols) {
    double sideLength = DEFAULT_HEX_CELL_SIZE;
    boolean isColOdd = numOfCols % 2 == 1;
    boolean isRowOdd = numOfRows % 2 == 1;

    List<Double> points = new ArrayList<>();

    // Top border
    for (int i = 0; i < numOfCols; i++) {
      double y = i % 2 == 0 ? 0.0 : sideLength * Math.sqrt(3) / 2;
      points.add((i * 1.5 + 0.5) * sideLength);
      points.add(y);
      points.add((i * 1.5 + 1.5) * sideLength);
      points.add(y);
    }

    // Right border
    double prevX = (numOfCols * 1.5) * sideLength;
    double prevY = (numOfCols - 1) % 2 == 0 ? 0.0 : sideLength * Math.sqrt(3) / 2;
    for (int i = 0; i < numOfRows; i++) {
      points.add(prevX);
      points.add(prevY + i * sideLength * Math.sqrt(3));
      points.add(prevX + sideLength / 2);
      points.add(prevY + (i + 0.5) * sideLength * Math.sqrt(3));
    }

    // Bottom border;
    prevY = prevY + numOfRows * sideLength * Math.sqrt(3);
    points.add(prevX);
    points.add(prevY);
    double offsetY = isRowOdd ? sideLength * Math.sqrt(3) / 2 : -sideLength * Math.sqrt(3) / 2;
    for (int i = 0; i < numOfCols; i++) {
      double y = i % 2 == 0 ? prevY : prevY + offsetY;
      points.add(prevX - (i * 1.5) * sideLength);
      points.add(y);
      points.add(prevX - (i * 1.5 + 1.0) * sideLength);
      points.add(y);
    }

    // Left border
    prevX = 0.5 * sideLength;
    prevY = isColOdd ? prevY : prevY + offsetY;
    for (int i = 0; i < numOfRows; i++) {
      points.add(prevX);
      points.add(prevY - i * sideLength * Math.sqrt(3));
      points.add(prevX - sideLength / 2);
      points.add(prevY - (i + 0.5) * sideLength * Math.sqrt(3));
    }

    addBoundary(pane, points);
  }

  /* PRIVATE HELPER METHODS */

  private void drawHexagon(Pane pane, double centerX, double centerY, double sideLength) {
    double[] points = new double[12];
    for (int i = 0; i < 6; i++) {
      double angle = Math.toRadians(60 * i);
      points[2 * i] = centerX + sideLength * Math.cos(angle);
      points[2 * i + 1] = centerY + sideLength * Math.sin(angle);
    }
    Polygon hexagon = new Polygon(points);
    hexagon.setFill(DEFAULT_BACKGROUND_COLOR);
    hexagon.setStroke(DEFAULT_BORDER_COLOR);
    hexagon.setStrokeWidth(DEFAULT_BORDER_SIZE);
    hexagon.setStrokeType(StrokeType.INSIDE);
    pane.getChildren().add(hexagon);
  }
}
