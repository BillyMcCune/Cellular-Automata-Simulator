package cellsociety.model.config;

import cellsociety.log.Log;
import cellsociety.model.config.ConfigInfo.SimulationType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConfigReader {

  // File configuration constants.
  private static final String DATA_FILE_EXTENSION = "*.xml";
  private static final String DATA_FILE_FOLDER = "/src/main/resources/cellsociety/configdata";
  private static final String INTERNAL_CONFIGURATION = "cellsociety.Version";
  private final Map<String, File> fileMap = new HashMap<>();

  /**
   * Loads and parses the configuration file data.
   */
  public ConfigInfo readConfig(String fileName)
      throws ParserConfigurationException, IOException, SAXException {
    if (!fileMap.containsKey(fileName)) {
      createListOfConfigFiles();
    }
    File dataFile = fileMap.get(fileName);
    Log.trace("Looking for file at: " + System.getProperty("user.dir") + DATA_FILE_FOLDER);
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

    // Parse the grid of initial cells (using cellRecord.Cell).
    List<List<CellRecord>> initialCells = parseInitialCells(root);
    // Parse parameters using the new parameterRecord.
    ParameterRecord parameters = parseForParameters(root);
    // Parse accepted states (assumed to be provided as a space‐separated list in one element).
    Set<Integer> acceptedStates = parseForAcceptedStates(root);

    // Validate grid bounds and cell states.
    checkForInvalidInformation(width, height, acceptedStates, initialCells);

    return new ConfigInfo(
        SimulationType.valueOf(type.toUpperCase()), // Convert type string to enum.
        title,
        author,
        description,
        width,
        height,
        defaultSpeed,
        initialCells,
        parameters,
        acceptedStates,
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
   * Retrieves the version from internal resources.
   */
  public double getVersion() {
    ResourceBundle resources = ResourceBundle.getBundle(INTERNAL_CONFIGURATION);
    return Double.parseDouble(resources.getString("Version"));
  }

  // Helper method: gets the text value of the given tag.
  private String getTextValue(Element e, String tagName) {
    NodeList nodeList = e.getElementsByTagName(tagName);
    if (nodeList.getLength() > 0) {
      return nodeList.item(0).getTextContent();
    } else {
      return "";
    }
  }

  /**
   * Parses the grid defined in the XML file from the new <initialCells> format.
   * Each row contains multiple <cell> elements, which are converted to cellRecord.Cell.
   */
  private List<List<CellRecord>> parseInitialCells(Element root) {
    Element initialCellsElement = (Element) root.getElementsByTagName("initialCells").item(0);
    if (initialCellsElement == null) {
      throw new IllegalArgumentException("Missing 'initialCells' element.");
    }
    NodeList rows = initialCellsElement.getElementsByTagName("row");
    if (rows == null || rows.getLength() == 0) {
      throw new IllegalArgumentException("No 'row' elements found inside 'initialCells'.");
    }

    List<List<CellRecord>> grid = new ArrayList<>();
    for (int i = 0; i < rows.getLength(); i++) {
      Node rowNode = rows.item(i);
      if (rowNode.getNodeType() != Node.ELEMENT_NODE) continue; // Skip non-element nodes.
      Element rowElement = (Element) rowNode;
      // Get all <cell> elements in this row.
      NodeList cellNodes = rowElement.getElementsByTagName("cell");
      List<CellRecord> rowCells = new ArrayList<>();
      for (int j = 0; j < cellNodes.getLength(); j++) {
        Node cellNode = cellNodes.item(j);
        if (cellNode.getNodeType() == Node.ELEMENT_NODE) {
          Element cellElement = (Element) cellNode;
          String stateStr = cellElement.getAttribute("state");
          if (stateStr == null || stateStr.isEmpty()) {
            throw new IllegalArgumentException("Cell missing 'state' attribute at row " + i + ", column " + j);
          }
          int state;
          try {
            state = Integer.parseInt(stateStr);
          } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid cell state at row " + i + ", column " + j + ": " + stateStr, ex);
          }
          // Parse additional attributes as properties (excluding "state").
          Map<String, Double> properties = new HashMap<>();
          for (int k = 0; k < cellElement.getAttributes().getLength(); k++) {
            Node attr = cellElement.getAttributes().item(k);
            String attrName = attr.getNodeName();
            if (!attrName.equals("state")) {
              String attrValue = attr.getNodeValue();
              try {
                double value = Double.parseDouble(attrValue);
                properties.put(attrName, value);
              } catch (NumberFormatException e) {
                // If not a double, ignore the property.
              }
            }
          }
          rowCells.add(new CellRecord(state, properties));
        }
      }
      if (rowCells.isEmpty()) {
        throw new IllegalArgumentException("Empty row encountered in 'initialCells' at index " + i);
      }
      grid.add(rowCells);
    }
    return grid;
  }

  /**
   * Parses the parameters from the XML file.
   * Expected XML format:
   * <parameters>
   *   <doubleParameter name="param1">1.23</doubleParameter>
   *   <stringParameter name="param2">value</stringParameter>
   * </parameters>
   */
  private ParameterRecord parseForParameters(Element root) {
    Element parametersElement = (Element) root.getElementsByTagName("parameters").item(0);
    // If no parameters element exists, return empty maps.
    if (parametersElement == null) {
      return new ParameterRecord(new HashMap<>(), new HashMap<>());
    }
    Map<String, Double> doubleParams = new HashMap<>();
    Map<String, String> stringParams = new HashMap<>();
    NodeList paramNodes = parametersElement.getChildNodes();
    for (int i = 0; i < paramNodes.getLength(); i++) {
      Node node = paramNodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element paramElement = (Element) node;
        String tagName = paramElement.getTagName();
        String name = paramElement.getAttribute("name");
        if (name == null || name.isEmpty()) {
          throw new IllegalArgumentException("Parameter element missing 'name' attribute.");
        }
        String textContent = paramElement.getTextContent().trim();
        if (tagName.equals("doubleParameter")) {
          try {
            double value = Double.parseDouble(textContent);
            doubleParams.put(name, value);
          } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid double value for parameter " + name + ": " + textContent, e);
          }
        } else if (tagName.equals("stringParameter")) {
          stringParams.put(name, textContent);
        } else {
          // Optionally ignore or handle unknown parameter types.
        }
      }
    }
    return new ParameterRecord(doubleParams, stringParams);
  }

  /**
   * Parses accepted states from a single <acceptedStates> element containing a space‐separated list.
   */
  private Set<Integer> parseForAcceptedStates(Element root) {
    Element acceptedStatesElement = (Element) root.getElementsByTagName("acceptedStates").item(0);
    if (acceptedStatesElement == null) {
      throw new IllegalArgumentException("Missing 'acceptedStates' element.");
    }
    String statesText = acceptedStatesElement.getTextContent().trim();
    if (statesText.isEmpty()) {
      throw new IllegalArgumentException("'acceptedStates' element is empty.");
    }
    Set<Integer> acceptedStates = new HashSet<>();
    String[] tokens = statesText.split("\\s+");
    for (String token : tokens) {
      try {
        acceptedStates.add(Integer.parseInt(token));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid accepted state value: " + token, e);
      }
    }
    return acceptedStates;
  }

  /**
   * Checks grid bounds and validates that every cell has an accepted state.
   */
  private void checkForInvalidInformation(int gridWidth, int gridHeight, Set<Integer> acceptedStates, List<List<CellRecord>> grid) {
    checkGridBounds(gridWidth, gridHeight, grid);
    checkInvalidStates(acceptedStates, grid);
  }

  private void checkGridBounds(int width, int height, List<List<CellRecord>> grid) {
    if (grid.size() != height) {
      throw new IllegalArgumentException(
          "Grid in file has wrong number of rows. Expected " + height + " but found " + grid.size()
      );
    }
    for (List<CellRecord> row : grid) {
      if (row.size() != width) {
        throw new IllegalArgumentException(
            "Grid in file has wrong number of columns. Expected " + width + " but found " + row.size()
        );
      }
    }
  }

  private void checkInvalidStates(Set<Integer> acceptedStates, List<List<CellRecord>> grid) {
    for (List<CellRecord> row : grid) {
      for (CellRecord cell : row) {
        if (!acceptedStates.contains(cell.state())) {
          throw new IllegalArgumentException(
              "Grid in file contains an invalid state: " + cell.state()
          );
        }
      }
    }
  }
}
