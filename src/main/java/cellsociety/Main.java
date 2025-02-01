package cellsociety;

import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigInfo.SimulationType;
import cellsociety.model.config.ConfigReader;
import cellsociety.view.scene.SimulationScene;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Feel free to completely change this code or delete it entirely.
 */
public class Main extends Application {

  public static final int FRAMES_PER_SECOND = 60;

//  @Override
//  public void start(Stage secondaryStage) {
//    ConfigReader configReader = new ConfigReader();
//    List<String> fileNames = configReader.getFileNames();
//    ConfigInfo myInfo = configReader.readConfig(fileNames.getFirst());
//    System.out.println(myInfo.getAuthor());
//  }


  @Override
  public void start(Stage primaryStage) {
    // Create the main scene
    SimulationScene mainScene = new SimulationScene(primaryStage);

    // Set up the primary stage
    primaryStage.setTitle("Game of Life Simulation");
    primaryStage.show();

    // Set up the game loop
    Timeline timeline = new Timeline();
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.getKeyFrames().add(new javafx.animation.KeyFrame(
        javafx.util.Duration.seconds(1.0 / FRAMES_PER_SECOND),
        event -> mainScene.step(1.0 / FRAMES_PER_SECOND)
    ));
    timeline.play();
  }

  /**
   * Start the program, give complete control to JavaFX.
   * <p>
   * Default version of main() is actually included within JavaFX, so this is not technically
   * necessary!
   */
  public static void main(String[] args) {
    launch(args);
  }
}
