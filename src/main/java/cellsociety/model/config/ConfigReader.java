package cellsociety.model.config;

import cellsociety.model.config.ConfigInfo.SimulationType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
 * TODO GET RID OF JAVAFX STUFF
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
  public ConfigInfo readConfig(String fileName) {
    if (!fileMap.containsKey(fileName)) {
      createListOfConfigFiles();
    }
    File dataFile = fileMap.get(fileName);
    System.out.println("Looking for file at: " + System.getProperty("user.dir") + DATA_FILE_FOLDER);
    ConfigInfo configInformation = getConfigInformation(dataFile);
    System.out.println(configInformation.getParameters());
    if (!configInformation.isValid()) {
      System.err.println("Configuration file not found or is empty");
    }
    return configInformation;
  }

  /**
   * Purpose: Returns number of blocks needed to cover the width and height given in the data file.
   * Assumptions: Parameters: Exceptions: return value:
   */
  public ConfigInfo getConfigInformation(File xmlFile) {
    ConfigInfo configInformation = ConfigInfo.createInstance();
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
      Map<String,Double> parameters = parseForParameters(root);


      configInformation.setConfig(new ArrayList<>(List.of(
          type,
          title,
          author,
          description,
          width,
          height,
          defaultSpeed,
          initialStatesForGrid,
          parameters
      )));

      System.out.println("Configuration file:" + xmlFile.getName());
      System.out.println("Configuration info:" + configInformation.getAllConfigInfo());
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



  public void createListOfConfigFiles() {
    File folder = new File(System.getProperty("user.dir") + DATA_FILE_FOLDER);

    if (folder.exists() && folder.isDirectory()) {
      File[] fileList = folder.listFiles();

      if (fileList != null) {
        for (File file : fileList) {
          if (file.isFile()) {
            fileMap.put(file.getName(), file);
          }
        }
      }
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
      // FIXME: empty string or exception? In some cases it may be an error to not find any text
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

  /**
   * Purpose: A method to test getting internal resources.
   * Assumptions:
   * Parameters:
   * Exceptions:
   * return value:
   */
  private Map<String, Double> parseForParameters(Element root) {
    Map<String, Double> parametersMap = new HashMap<>();
    Element parametersElement = (Element) root.getElementsByTagName("parameters").item(0);
    if (parametersElement == null) {
      return parametersMap;
    }

    NodeList params = parametersElement.getChildNodes();
    for (int i = 0; i < params.getLength(); i++) {
      Node child = params.item(i);

      if (child.getNodeType() == Node.ELEMENT_NODE) {
        Element paramElement = (Element) child;
        String paramName = paramElement.getNodeName();
        String paramValueStr = paramElement.getTextContent().trim();

        if (!paramValueStr.isEmpty()) {
          try {
            double paramValue = Double.parseDouble(paramValueStr);
            parametersMap.put(paramName, paramValue);
          } catch (NumberFormatException e) {
            System.err.println("Could not parse parameter '" + paramName
                + "' with value: '" + paramValueStr + "'");
          }
        }
      }
    }
    return parametersMap;
  }
}
