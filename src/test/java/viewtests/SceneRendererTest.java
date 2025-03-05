package viewtests;

import cellsociety.view.renderer.SceneRenderer;
import cellsociety.view.renderer.drawer.HexGridDrawer;
import cellsociety.view.renderer.drawer.SquareGridDrawer;
import cellsociety.view.renderer.drawer.TriGridDrawer;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SceneRendererTest {

  @Test
  public void drawGrid_UseAllShapeDrawerClasses() {
    Pane pane = new Pane();
    int boundSize = 1;

    SceneRenderer.drawGrid(pane, 10, 10, SquareGridDrawer.class);
    assertEquals(100, pane.getChildren().size() - boundSize, "Grid should have 100 cells");

    SceneRenderer.drawGrid(pane, 10, 10, TriGridDrawer.class);
    assertEquals(100, pane.getChildren().size() - boundSize, "Grid should have 100 cells");

    SceneRenderer.drawGrid(pane, 10, 10, HexGridDrawer.class);
    assertEquals(100, pane.getChildren().size() - boundSize, "Grid should have 100 cells");

  }

  @Test
  public void drawCell_DrawAllCell() {
    Pane pane = new Pane();
    for (int i = 0; i < 100; i++) {
      Rectangle cell = new Rectangle(10, 10);
      cell.setFill(Color.RED);
      pane.getChildren().add(cell);
    }

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        SceneRenderer.drawCell(pane, 10, i, j, "BLUE");
      }
    }
  }

  @Test
  public void drawCell_DrawEmptyCell() {
    Pane pane = new Pane();
    SceneRenderer.drawCell(pane, 0, 0, 0, "BLUE");
  }
}