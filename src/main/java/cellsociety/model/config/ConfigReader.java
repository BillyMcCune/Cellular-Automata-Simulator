package cellsociety.model.config;

import cellsociety.model.config.ConfigInfo.SimulationType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Billy McCune Purpose: Assumptions: Dependecies (classes or packages): How to Use: Any
 * Other Details:
 * TODO GET RID OF JAVAFX STUFF
 */
public class ConfigReader {

  // kind of data files to look for
  public static final String DATA_FILE_EXTENSION = "*.xml";
  // starts in the resources folder
  public static final String DATA_FILE_FOLDER = System.getProperty("user.dir") +
      "/src/main/resources/cellsociety/SimulationConfigurationData";
  // NOTE: make ONE chooser since generally accepted behavior is that it remembers where user left it last
  //private final static FileChooser FILE_CHOOSER = makeChooser(DATA_FILE_EXTENSION);
  // internal configuration file
  public static final String INTERNAL_CONFIGURATION = "cellsociety.Version";


  /**
   * Purpose: Loads and parses config file data Assumptions: The DATA_FILE_FOLDER is where our
   * desired configurations are Parameters: None Exceptions: if after reading the configuration file
   * the array of objects is empty then an alert is mode return value: ArrayList<Object> which
   * contains the data from the configuration file
   */
  public ArrayList<Object> readConfig(File file) {
    System.out.println("Looking for file at: " + DATA_FILE_FOLDER);
    ArrayList<Object> configInformation = new ArrayList<>();
    //showMessage(AlertType.INFORMATION, String.format("Version: %s", getVersion()));
    //File dataFile = FILE_CHOOSER.showOpenDialog(primaryStage);
    File dataFile = file;
    if (dataFile != null) {
      configInformation = getConfigInformation(dataFile);
      if (configInformation.isEmpty()) {
        //showMessage(AlertType.INFORMATION,
            System.err.println("Configuration file not found or is empty");
      }
    }
    return configInformation;
  }

  /**
   * Purpose: Returns number of blocks needed to cover the width and height given in the data file.
   * Assumptions: Parameters: Exceptions: return value:
   */
  public ArrayList<Object> getConfigInformation(File xmlFile) {
    ArrayList<Object> configInformation = new ArrayList<>();
    try {
      Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder()
          .parse(xmlFile);
      Element root = xmlDocument.getDocumentElement();
      String type = getTextValue(root, "type");
      String title = getTextValue(root, "title");
      String author = getTextValue(root, "author");
      String description = getTextValue(root, "description");
      int width = Integer.parseInt(getTextValue(root, "width"));
      int height = Integer.parseInt(getTextValue(root, "height"));
      int defaultSpeed = Integer.parseInt(getTextValue(root, "defaultSpeed"));

      List<List<Integer>> initialStatesForGrid = parseInitialGrid(root);
      configInformation.add(type);
      configInformation.add(title);
      configInformation.add(author);
      configInformation.add(description);
      configInformation.add(width);
      configInformation.add(height);
      configInformation.add(defaultSpeed);
      configInformation.add(initialStatesForGrid);
      //showMessage(AlertType.INFORMATION,
          String.format("Configuration file: %s", xmlFile.getName()));
      //showMessage(AlertType.INFORMATION, String.format("Version: %s", getVersion()));
      //showMessage(AlertType.INFORMATION, configInformation.toString());
      return configInformation;
    } catch (NumberFormatException e) {
      System.err.println("Invalid number given in data");
      return configInformation;
    } catch (ParserConfigurationException e) {
      System.err.println("Invalid XML Configuration");
      return configInformation;
    } catch (SAXException | IOException e) {
      System.err.println("Invalid XML Data");
      return configInformation;
    }
  }

  /**
   * Purpose: A method to test getting internal resources.
   * Assumptions:
   * Parameters:
   * Exceptions:
   * return value:
   */
  public double getVersion() {
    ResourceBundle resources = ResourceBundle.getBundle(INTERNAL_CONFIGURATION);
    return Double.parseDouble(resources.getString("Version"));
  }

  // get value of Element's text
  private String getTextValue(Element e, String tagName) {
    NodeList nodeList = e.getElementsByTagName(tagName);
    if (nodeList.getLength() > 0) {
      return nodeList.item(0).getTextContent();
    } else {
      // FIXME: empty string or exception? In some cases it may be an error to not find any text
      return "";
    }
  }

  // display given message to user using the given type of Alert dialog box
  //void showMessage(AlertType type, String message) {
   // new Alert(type, message).showAndWait();
//  }

  // set some sensible defaults when the FileChooser is created
  //private static FileChooser makeChooser(String extensionAccepted) {
  //  FileChooser result = new FileChooser();
  //  result.setTitle("Open Data File");
    // pick a reasonable place to start searching for files
 //   result.setInitialDirectory(new File(DATA_FILE_FOLDER));
  //  result.getExtensionFilters()
//      .setAll(new FileChooser.ExtensionFilter("Data Files", extensionAccepted));
   // return result;
//  }

  /**
   * Purpose: A method for parsing the grid in the xml configuration file
   * Assumptions: The initial state node is in the xml configuration and the data for the grid is contained in row nodes
   * Parameters: the root node of the xml file
   * Exceptions: the rows of the grid must be children of the initials States node in the xml file
   * return value: List<List<Integer>> which contains the data from each of the cells defined in the configuration file
   */
  private List<List<Integer>> parseInitialGrid(Element root) {
    List<List<Integer>> initialStatesForGrid = new ArrayList<>();

    Element initialStatesElement = (Element) root.getElementsByTagName("initialStates").item(0);

    if (initialStatesElement == null) {
      System.err.println("Missing <initialStates> section in XML");
      return initialStatesForGrid;
    }

    NodeList rows = initialStatesElement.getElementsByTagName("row");

    for (int i = 0; i < rows.getLength(); i++) {
      String initialState = rows.item(i).getTextContent().trim();
      String[] values = initialState.split("\\s+");
      List<Integer> row = new ArrayList<>();

      for (String value : values) {
        row.add(Integer.parseInt(value));
      }

      initialStatesForGrid.add(row);
    }
    return initialStatesForGrid;
  }
}