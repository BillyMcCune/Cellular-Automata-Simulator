package viewtests;

import cellsociety.model.data.states.FireState;
import cellsociety.model.data.states.LifeState;
import cellsociety.view.scene.SceneRenderer;
import java.util.Map;
import javafx.scene.layout.GridPane;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link cellsociety.view.scene.SceneRenderer} class.
 *
 * @author Hsuan-Kai Liao
 */
public class SceneRendererTest {

  @Test
  public void testDrawGrid() {
    GridPane gridPane = new GridPane();
    SceneRenderer.drawGrid(gridPane, 10, 10);
  }

  @Test
  public void testDrawCell() {
    GridPane gridPane = new GridPane();
    SceneRenderer.drawGrid(gridPane, 10, 10);
    SceneRenderer.drawCell(gridPane, 0, 0, LifeState.ALIVE);
  }

  @Test
  public void testDrawParameters() {
    GridPane gridPane = new GridPane();
    SceneRenderer.drawGrid(gridPane, 10, 10);
    Map<String, Double> parameters = Map.of(
        "probCatch", 0.5,
        "probIgnite", 0.5,
        "probTree", 0.5
    );

    SceneRenderer.drawParameters(gridPane, 0, 0, FireState.BURNING, parameters);
  }

}
