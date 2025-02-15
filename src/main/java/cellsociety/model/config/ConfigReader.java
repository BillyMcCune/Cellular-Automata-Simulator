package cellsociety.model.config;

import cellsociety.model.config.ConfigInfo.SimulationType;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.util.Map;
import org.w3c.dom.Node;

/**
 * @author Billy McCune Purpose: Assumptions: Dependecies (classes or packages): How to Use: Any
 * Other Details:
 * TODO Shorten
 * TODO pass in the valid states
 */
public class ConfigReader {

  // kind of data files to look for
  private static final String DATA_FILE_EXTENSION = "*.xml";
  // starts in the resources folder
  private static final String DATA_FILE_FOLDER = "/src/main/resources/cellsociety/configdata";
  private static final String INTERNAL_CONFIGURATION = "cellsociety.Version";
  private final Map<String,File> fileMap = new HashMap<>();

  /**
   * Purpose: Loads and parses config file data Assumptions: The DATA_FILE_FOLDER is where our
   * desired configurations are Parameters: None Exceptions: if after reading the configuration file
   * the array of objects is empty then an alert is mode return value: ArrayList<Object> which
   * contains the data from the configuration file
   */
  public ConfigInfo readConfig(String fileName)
      throws ParserConfigurationException, IOException, SAXException {
    if (!fileMap.containsKey(fileName)) {
      createListOfConfigFiles();
    }
    File dataFile = fileMap.get(fileName);
    System.out.println("Looking for file at: " + System.getProperty("user.dir") + DATA_FILE_FOLDER);
    return getConfigInformation(dataFile, fileName);
  }

  /**
   * Parses the XML file and creates a new ConfigInfo record.
   */
  public ConfigInfo getConfigInformation(File xmlFile, String fileName)
      throws ParserConfigurationException, SAXException, IOException {
    Document xmlDocument =
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
    Element root = xmlDocument.getDocumentElement();

    String type = getTextValue(root, "type");
    String title = getTextValue(root, "title");
    String author = getTextValue(root, "author");
    String description = getTextValue(root, "description");
    int width = Integer.parseInt(getTextValue(root, "width"));
    int height = Integer.parseInt(getTextValue(root, "height"));
    int defaultSpeed = Integer.parseInt(getTextValue(root, "defaultSpeed"));
    List<List<Integer>> initialStatesForGrid = parseInitialGrid(root);
    Map<String, Double> parameters = parseForParameters(root);


    return new ConfigInfo(
        SimulationType.valueOf(type.toUpperCase()), // Convert type string to enum.
        title,
        author,
        description,
        width,
        height,
        defaultSpeed,
        initialStatesForGrid,
        parameters,
        fileName
    );
  }



  /**
   * Creates a mapping from file names to configuration files found in the designated folder.
   */
  public void createListOfConfigFiles() {
    try {
    File folder = new File(System.getProperty("user.dir") + DATA_FILE_FOLDER);
    File[] fileList = folder.listFiles();
      Arrays.stream(fileList)
          .filter(File::isFile)
          .forEach(file -> fileMap.put(file.getName(), file));
    } catch (NullPointerException e) {
      throw new IllegalStateException(
          "Configuration directory not found: " + System.getProperty("user.dir") + DATA_FILE_FOLDER);
    }
  }

  public List<String> getFileNames(){
    if (fileMap.isEmpty()) {
      createListOfConfigFiles();
    }
    return new ArrayList<>(fileMap.keySet());
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
      return "";
    }
  }

  /**
   * Purpose: A method for parsing the grid in the xml configuration file
   * Assumptions: The initial state node is in the xml configuration and the data for the grid is contained in row nodes
   * Parameters: the root node of the xml file
   * Exceptions: the rows of the grid must be children of the initials States node in the xml file
   * return value: List<List<Integer>> which contains the data from each of the cells defined in the configuration file
   */
  private List<List<Integer>> parseInitialGrid(Element root) {
    Element initialStatesElement = (Element) root.getElementsByTagName("initialStates").item(0);
    if (initialStatesElement == null) {
      throw new IllegalArgumentException("Missing 'initialStates' element.");
    }
    NodeList rows = initialStatesElement.getElementsByTagName("row");
    if (rows == null || rows.getLength() == 0) {
      throw new IllegalArgumentException("No 'row' elements found inside 'initialStates'.");
    }

    List<List<Integer>> grid = new ArrayList<>();
    for (int i = 0; i < rows.getLength(); i++) {
      String rowText = rows.item(i).getTextContent().trim();
      if (rowText.isEmpty()) {
        throw new IllegalArgumentException("Empty row encountered in 'initialStates' at index " + i);
      }
      String[] values = rowText.split("\\s+");
      List<Integer> row = new ArrayList<>();
      for (String value : values) {
        try {
          row.add(Integer.parseInt(value));
        } catch (NumberFormatException ex) {
          throw new IllegalArgumentException("Invalid integer value in row " + i + ": " + value, ex);
        }
      }
      grid.add(row);
    }
    return grid;
  }


  /**
   * Purpose: A method to test getting internal resources.
   * Assumptions:
   * Parameters:
   * Exceptions:
   * return value:
   */
  private Map<String, Double> parseForParameters(Element root) {
    try {
      Map<String, Double> parametersMap = new HashMap<>();
      Element parametersElement = (Element) root.getElementsByTagName("parameters").item(0);
      NodeList params = parametersElement.getChildNodes();
      for (int i = 0; i < params.getLength(); i++) {
        Node child = params.item(i);

        if (child.getNodeType() == Node.ELEMENT_NODE) {
          Element paramElement = (Element) child;
          String paramName = paramElement.getNodeName();
          String paramValueStr = paramElement.getTextContent().trim();
            try {
              double paramValue = Double.parseDouble(paramValueStr);
              parametersMap.put(paramName, paramValue);
            } catch (NumberFormatException e) {
              System.err.println("Could not parse parameter '" + paramName
                  + "' with value: '" + paramValueStr + "'");
            }
        }
      }
      return parametersMap;
    }
    catch (Exception e) {
      throw new AssertionError();
    }
  }
}
