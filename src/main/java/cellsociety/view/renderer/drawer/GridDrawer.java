package cellsociety.view.renderer.drawer;

import java.util.List;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;

public abstract class GridDrawer {

  public static final int DEFAULT_BORDER_SIZE = 1;
  // Constants
  protected static final Color DEFAULT_BACKGROUND_COLOR = Color.DIMGRAY;
  protected static final Color DEFAULT_BORDER_COLOR = Color.LIGHTGRAY;
  protected static final Color DEFAULT_BOUND_COLOR = Color.BLACK;
  protected static final int DEFAULT_CELL_SIZE = 20;
  // Singleton instance
  private static GridDrawer instance;

  private static <T extends GridDrawer> GridDrawer getInstance(Class<T> clazz) {
    if (instance == null || !instance.getClass().equals(clazz)) {
      instance = createInstance(clazz);
    }
    return instance;
  }

  private static <T extends GridDrawer> GridDrawer createInstance(Class<T> clazz) {
    try {
      // Ensure the class is not abstract or an interface
      if (clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
        throw new IllegalArgumentException("Cannot instantiate an interface or abstract class");
      }
      return clazz.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /* PROTECTED HELPER METHODS */

  /**
   * Draw a grid on the given GridPane with the given number of rows and columns.
   *
   * @param pane      The grid to draw on
   * @param numOfRows The number of rows in the grid
   * @param numOfCols The number of columns in the grid
   * @param clazz     The class of the GridDrawer to use
   */
  public static <T extends GridDrawer> void drawGrid(Pane pane, int numOfRows, int numOfCols,
      Class<T> clazz) {
    // Use the instance's class to get the correct type of GridDrawer
    GridDrawer drawer = getInstance(clazz);
    drawer.drawGridContents(pane, numOfRows, numOfCols);
    drawer.drawGridBound(pane, numOfRows, numOfCols);
  }

  /* OVERRIDE METHOD BELOW */

  protected void addBoundary(Pane pane, List<Double> points) {
    Polygon boundary = new Polygon();
    boundary.getPoints().addAll(points);
    boundary.setFill(Color.TRANSPARENT);
    boundary.setStroke(DEFAULT_BOUND_COLOR);
    boundary.setStrokeWidth(3 * DEFAULT_BORDER_SIZE);
    boundary.setStrokeType(StrokeType.OUTSIDE);

    pane.getChildren().add(boundary);
  }

  protected abstract void drawGridContents(Pane pane, int numOfRows, int numOfCols);

  /* API BELOW */

  protected abstract void drawGridBound(Pane pane, int numOfRows, int numOfCols);
}
