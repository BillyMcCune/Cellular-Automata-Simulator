package cellsociety;

import cellsociety.view.scene.SimulationScene;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Thew entry point of the program, which sets up the main scene and game loop.
 */
public class Main extends Application {
  public static final int FRAMES_PER_SECOND = 60;

  @Override
  public void start(Stage primaryStage) {
    // Set up the title of the primary stage
    primaryStage.setTitle("Cell Society Simulation");

    // Create the main scene
    SimulationScene mainScene = new SimulationScene(primaryStage);
    mainScene.start(FRAMES_PER_SECOND, true);
  }

  /**
   * Start the program, give complete control to JavaFX.
   * @param args command line arguments
   */
  public static void main(String[] args) {
    launch(args);
  }
}
