package cellsociety;

import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigInfo.SimulationType;
import cellsociety.model.config.ConfigReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
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

  /**
   * @see Application#start(Stage)
   */
  @Override
  public void start(Stage secondaryStage) {
    ConfigReader configReader = new ConfigReader();
    List<String> fileNames = configReader.getFileNames();
    ConfigInfo myInfo = configReader.readConfig(fileNames.getFirst());
    System.out.println(myInfo.getAuthor());
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
