package cellsociety;

import cellsociety.model.config.ConfigReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

// hi
/**
 * Feel free to completely change this code or delete it entirely.
 */
public class Main extends Application {

  /**
   * @see Application#start(Stage)
   */
  @Override
  public void start(Stage primaryStage) {
    ConfigReader configReader = new ConfigReader();
    ArrayList<Object> objects = configReader.readConfig();
    System.out.println(objects.toString());
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
