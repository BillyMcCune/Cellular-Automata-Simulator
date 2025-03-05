package viewtests.gridDrawertests;

import cellsociety.view.renderer.drawer.GridDrawer;
import cellsociety.view.renderer.drawer.HexGridDrawer;
import cellsociety.view.renderer.drawer.SquareGridDrawer;
import cellsociety.view.renderer.drawer.TriGridDrawer;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the GridDrawer class.
 *
 * @author Hsuan-Kai Liao
 */
public class GridDrawerTest {

  @Test
  public void drawGrid_DrawsAllGridContents() {
    Pane pane = new Pane();
    int numOfRows = 5;
    int numOfCols = 5;
    int boundarySize = 1; // This is the size of the boundary drawn around the grid

    GridDrawer.drawGrid(pane, numOfRows, numOfCols, TriGridDrawer.class);
    Assertions.assertEquals(numOfRows * numOfCols + boundarySize, pane.getChildren().size());

    GridDrawer.drawGrid(pane, numOfRows, numOfCols, HexGridDrawer.class);
    Assertions.assertEquals(numOfRows * numOfCols + boundarySize, pane.getChildren().size());

    GridDrawer.drawGrid(pane, numOfRows, numOfCols, SquareGridDrawer.class);
    Assertions.assertEquals(numOfRows * numOfCols + boundarySize, pane.getChildren().size());
  }

  @Test
  public void drawGrid_DrawsGridContentsEmpty() {
    Pane pane = new Pane();
    int numOfRows = 0;
    int numOfCols = 0;
    int boundarySize = 1; // This is the size of the boundary drawn around the grid

    GridDrawer.drawGrid(pane, numOfRows, numOfCols, TriGridDrawer.class);
    Assertions.assertEquals(boundarySize, pane.getChildren().size());

    GridDrawer.drawGrid(pane, numOfRows, numOfCols, HexGridDrawer.class);
    Assertions.assertEquals(boundarySize, pane.getChildren().size());

    GridDrawer.drawGrid(pane, numOfRows, numOfCols, SquareGridDrawer.class);
    Assertions.assertEquals(boundarySize, pane.getChildren().size());
  }

  @Test
  public void drawGrid_DrawsSpecificSquareGridContents() {
    Pane pane = new Pane();
    int numOfRows = 5;
    int numOfCols = 5;
    int boundarySize = 1; // This is the size of the boundary drawn around the grid

    GridDrawer.drawGrid(pane, numOfRows, numOfCols, SquareGridDrawer.class);
    Assertions.assertEquals(numOfRows * numOfCols + boundarySize, pane.getChildren().size());
  }

  @Test
  public void drawGrid_DrawsSpecificTriGridContents() {
    Pane pane = new Pane();
    int numOfRows = 5;
    int numOfCols = 5;
    int boundarySize = 1; // This is the size of the boundary drawn around the grid

    GridDrawer.drawGrid(pane, numOfRows, numOfCols, TriGridDrawer.class);
    Assertions.assertEquals(numOfRows * numOfCols + boundarySize, pane.getChildren().size());
  }

  @Test
  public void drawGrid_DrawsSpecificHexGridContents() {
    Pane pane = new Pane();
    int numOfRows = 5;
    int numOfCols = 5;
    int boundarySize = 1; // This is the size of the boundary drawn around the grid

    GridDrawer.drawGrid(pane, numOfRows, numOfCols, HexGridDrawer.class);
    Assertions.assertEquals(numOfRows * numOfCols + boundarySize, pane.getChildren().size());
  }
}
