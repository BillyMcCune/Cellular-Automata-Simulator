package cellsociety.model.config;

import cellsociety.logging.Log;
import cellsociety.model.config.ConfigInfo.SimulationType;
import cellsociety.model.config.ConfigInfo.cellShapeType;
import cellsociety.model.config.ConfigInfo.gridEdgeType;
import cellsociety.model.config.ConfigInfo.neighborArrangementType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

/**
 * @author Billy McCune
 * The GridReader class is responsible for parsing the grid configuration from
 * an XML document. It supports reading grid information from various XML elements such as
 * <code>initialCells</code>,
 * <code>initialStates</code>, and <code>initialProportions</code>. In addition, it provides helper
 * methods to parse individual rows and cells.
 */
public class ConfigReader {

  private static final String DATA_FILE_EXTENSION = "*.xml";
  private static final String DATA_FILE_FOLDER = "/src/main/resources/cellsociety/configdata";
  private static final String INTERNAL_CONFIGURATION = "cellsociety.Version";
  private final Map<String, File> fileMap = new HashMap<>();

  /**
   * Loads and parses the configuration file data.
   *
   * @param fileName the name of the configuration file to be read.
   * @return a {@code ConfigInfo} object representing the parsed configuration.
   * @throws ParserConfigurationException if a parser configuration error occurs.
   * @throws IOException                  if an I/O error occurs.
   * @throws SAXException                 if a SAX parsing error occurs.
   */
  public ConfigInfo readConfig(String fileName)
      throws ParserConfigurationException, IOException, SAXException, IllegalArgumentException {
    if (!fileMap.containsKey(fileName)) {
      createListOfConfigFiles();
    }
    try {
      File dataFile = fileMap.get(fileName);
      Log.trace("Looking for file at: " + System.getProperty("user.dir") + DATA_FILE_FOLDER);
      return getConfigInformation(dataFile, fileName);
    } catch (ParserConfigurationException | SAXException | IOException | IllegalArgumentException e) {
      throw new ParserConfigurationException(e.getMessage());
    }
  }

  /**
   * Parses the XML file and creates a new {@code ConfigInfo} record.
   *
   * @param xmlFile  the XML file containing configuration information.
   * @param fileName the name of the configuration file.
   * @return a {@code ConfigInfo} object with all parsed configuration data.
   * @throws ParserConfigurationException if a parser configuration error occurs.
   * @throws SAXException                 if a SAX parsing error occurs.
   * @throws IOException                  if an I/O error occurs.
   */
  private ConfigInfo getConfigInformation(File xmlFile, String fileName)
      throws ParserConfigurationException, SAXException, IOException, IllegalArgumentException{

    if (xmlFile.length() == 0) {
      throw new IOException("error-xmlFile-isEmpty");
    }
  try {
    Document xmlDocument =
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
    Element root = xmlDocument.getDocumentElement();

    String simType = getTextValue(root, "type");
    String cellShape = getTextValue(root, "cellShapeType");
    String gridType = getTextValue(root, "gridEdgeType");
    String neighborArrangement = getTextValue(root, "neighborArrangementType");
    String title = getTextValue(root, "title");
    String author = getTextValue(root, "author");
    String description = getTextValue(root, "description");
    int width = Integer.parseInt(getTextValue(root, "width"));
    int height = Integer.parseInt(getTextValue(root, "height"));
    int defaultSpeed = Integer.parseInt(getTextValue(root, "defaultSpeed"));
    int neighborRadius = Integer.parseInt(getTextValue(root, "neighborRadius"));

    // Delegate grid parsing and validation to GridReader
    List<List<CellRecord>> initialGrid = GridReader.readInitialGrid(root);
    Set<Integer> acceptedStates = GridReader.readAcceptedStates(root);
    checkForInvalidInformation(width, height, acceptedStates, initialGrid);

    ParameterRecord parameters = parseForParameters(root);

    return new ConfigInfo(
        SimulationType.valueOf(simType.toUpperCase()),
        cellShapeType.valueOf(cellShape.toUpperCase()),
        gridEdgeType.valueOf(gridType.toUpperCase()),
        neighborArrangementType.valueOf(neighborArrangement.toUpperCase()),
        neighborRadius,
        title,
        author,
        description,
        width,
        height,
        defaultSpeed,
        initialGrid,
        parameters,
        acceptedStates,
        fileName
    );
  } catch (ParserConfigurationException e) {
    throw new ParserConfigurationException(e.getMessage());
  } catch (SAXException e) {
    throw new SAXException(e.getMessage());
  } catch (IOException e) {
    throw new IOException(e.getMessage());
  } catch (IllegalArgumentException e) {
    throw new IllegalArgumentException(e.getMessage());
  }
  }

  /**
   * Parses the parameters from the XML file. Expected XML format:
   * <pre>
   *   &lt;parameters&gt;
   *     &lt;doubleParameter name="param1"&gt;1.23&lt;/doubleParameter&gt;
   *     &lt;stringParameter name="param2"&gt;value&lt;/stringParameter&gt;
   *   &lt;/parameters&gt;
   * </pre>
   *
   * @param root the root XML element.
   * @return a {@code ParameterRecord} containing maps of double and string parameters.
   * @throws IllegalArgumentException if parameter elements are missing required attributes or
   *                                  contain invalid values.
   */
  private ParameterRecord parseForParameters(Element root) throws ParserConfigurationException, IOException, SAXException, IllegalArgumentException {
    try {
      Element parametersElement = getParametersElement(root);
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
          processParameterElement(paramElement, doubleParams, stringParams);
        }
      }
      return new ParameterRecord(doubleParams, stringParams);
    } catch (NullPointerException e) {
      throw new NullPointerException(e.getMessage());
    } catch (NumberFormatException e) {
      throw new NumberFormatException(e.getMessage());
    } catch (IllegalArgumentException | IllegalStateException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * Retrieves the <parameters> element from the given root element.
   *
   * @param root the root XML element.
   * @return the <parameters> element or null if not present.
   */
  private Element getParametersElement(Element root) {
    return (Element) root.getElementsByTagName("parameters").item(0);
  }

  /**
   * Processes an individual parameter element by checking its tag and adding its content to the
   * appropriate map.
   *
   * @param paramElement the parameter element.
   * @param doubleParams the map for double parameters.
   * @param stringParams the map for string parameters.
   * @throws IllegalArgumentException if the parameter element is missing a name or contains invalid
   *                                  values.
   */
  private void processParameterElement(Element paramElement, Map<String, Double> doubleParams,
      Map<String, String> stringParams) throws IllegalArgumentException {
    String tagName = paramElement.getTagName();
    String name = getParameterName(paramElement);
    String textContent = paramElement.getTextContent().trim();

    if (tagName.equals("doubleParameter")) {
      try {
        double value = Double.parseDouble(textContent);
        doubleParams.put(name, value);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
            "error-InvalidParameterValue," + name + "," + textContent);
      }
    } else if (tagName.equals("stringParameter")) {
      stringParams.put(name, textContent);
    }
  }

  private String getParameterName(Element paramElement) {
    String name = paramElement.getAttribute("name");
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("error-missingParameterName");
    }
    return name;
  }


  /**
   * Retrieves the text content of the first occurrence of a given tag within an element.
   *
   * @param e       the XML element.
   * @param tagName the name of the tag whose value is to be retrieved.
   * @return the text content of the tag.
   * @throws IllegalArgumentException if the tag does not exist.
   */
  private String getTextValue(Element e, String tagName) throws IllegalArgumentException {
    org.w3c.dom.NodeList nodeList = e.getElementsByTagName(tagName);
    if (nodeList.getLength() > 0) {
      return nodeList.item(0).getTextContent();
    } else {
      throw new IllegalArgumentException("error-parameterDoesNotExist," + tagName);
    }
  }

  /**
   * Creates a mapping from file names to configuration files found in the designated folder.
   */
  private void createListOfConfigFiles() throws IllegalArgumentException, IllegalStateException {
    try {
      File folder = new File(System.getProperty("user.dir") + DATA_FILE_FOLDER);
      File[] fileList = folder.listFiles();
      Arrays.stream(fileList)
          .filter(File::isFile)
          .forEach(file -> fileMap.put(file.getName(), file));
    } catch (NullPointerException e) {
      throw new IllegalStateException(
          "error-configDirectoryNotFound," + System.getProperty("user.dir") + ","
              + DATA_FILE_FOLDER);
    }
  }

  /**
   * Returns a list of configuration file names available in the configuration folder.
   *
   * @return a list of file name strings.
   */
  public List<String> getFileNames() {
    if (fileMap.isEmpty()) {
      createListOfConfigFiles();
    }
    return new ArrayList<>(fileMap.keySet());
  }

  /**
   * Retrieves the version number from internal resources.
   *
   * @return the version as a double.
   */
  public double getVersion() {
    ResourceBundle resources = ResourceBundle.getBundle(INTERNAL_CONFIGURATION);
    return Double.parseDouble(resources.getString("Version"));
  }


  /**
   * Checks the grid bounds and validates that every cell has an accepted state.
   *
   * @param gridWidth      the expected grid width.
   * @param gridHeight     the expected grid height.
   * @param acceptedStates the set of accepted states.
   * @param grid           the 2D grid of {@code CellRecord} to be validated.
   * @throws IllegalArgumentException if the grid dimensions or cell states are invalid.
   */
  private void checkForInvalidInformation(int gridWidth, int gridHeight,
      Set<Integer> acceptedStates, List<List<CellRecord>> grid)  throws IllegalArgumentException {
    try {
      checkGridBounds(gridWidth, gridHeight, grid);
      checkInvalidStates(acceptedStates, grid);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * Validates that the grid dimensions match the expected width and height.
   *
   * @param width  the expected number of columns.
   * @param height the expected number of rows.
   * @param grid   the 2D grid of {@code CellRecord}.
   * @throws IllegalArgumentException if the grid dimensions do not match.
   */
  private void checkGridBounds(int width, int height, List<List<CellRecord>> grid) throws IllegalArgumentException {
    if (grid.size() != height) {
      throw new IllegalArgumentException(
          "error-wrongNumberOfRows" + "," + height + "," + grid.size()
      );
    }
    for (List<CellRecord> row : grid) {
      if (row.size() != width) {
        throw new IllegalArgumentException(
            "error-wrongNumberOfColumns" + "," + width + "," + row.size());
      }
    }
  }

  /**
   * Validates that every cell in the grid has a state that is among the accepted states.
   *
   * @param acceptedStates the set of accepted states.
   * @param grid           the 2D grid of {@code CellRecord} to be validated.
   * @throws IllegalArgumentException if any cell contains an invalid state.
   */
  private void checkInvalidStates(Set<Integer> acceptedStates, List<List<CellRecord>> grid) throws IllegalArgumentException {
    for (List<CellRecord> row : grid) {
      for (CellRecord cell : row) {
        if (!acceptedStates.contains(cell.state())) {
          throw new IllegalArgumentException("error-GridHasInvalidState" + "," + cell.state());
        }
      }
    }
  }


}
