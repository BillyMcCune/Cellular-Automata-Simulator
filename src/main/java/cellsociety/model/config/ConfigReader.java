package cellsociety.model.config;

import cellsociety.logging.Log;
import cellsociety.model.config.ConfigInfo.SimulationType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
 * The ConfigReader class is responsible for reading and parsing XML configuration files
 * for the simulation. It supports different ways of defining the initial grid, parameters, and accepted states.
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
   * @throws IOException if an I/O error occurs.
   * @throws SAXException if a SAX parsing error occurs.
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
   * Parses the XML file and creates a new {@code ConfigInfo} record.
   *
   * @param xmlFile the XML file containing configuration information.
   * @param fileName the name of the configuration file.
   * @return a {@code ConfigInfo} object with all parsed configuration data.
   * @throws ParserConfigurationException if a parser configuration error occurs.
   * @throws SAXException if a SAX parsing error occurs.
   * @throws IOException if an I/O error occurs.
   */
  public ConfigInfo getConfigInformation(File xmlFile, String fileName)
      throws ParserConfigurationException, SAXException, IOException {

    if (xmlFile.length() == 0) {
      throw new IOException("Error: XML file is empty - " + xmlFile.getAbsolutePath());
    }

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
    List<List<CellRecord>> initialGrid = getInitialGrid(root);

    ParameterRecord parameters = parseForParameters(root);
    Set<Integer> acceptedStates = parseForAcceptedStates(root);

    checkForInvalidInformation(width, height, acceptedStates, initialGrid);

    return new ConfigInfo(
        SimulationType.valueOf(type.toUpperCase()),
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
  }

  /**
   * Retrieves the initial grid configuration from the XML root element. Determines whether the grid
   * is specified using {@code initialCells}, {@code initialStates}, or {@code initialProportions}.
   *
   * @param root the root XML element.
   * @return a 2D list of {@code CellRecord} representing the initial grid.
   * @throws ParserConfigurationException if multiple grid configuration elements are found or if the grid configuration is missing.
   */
  private List<List<CellRecord>> getInitialGrid(Element root) throws ParserConfigurationException {
    int initialCellsCount = root.getElementsByTagName("initialCells").getLength();
    int initialStatesCount = root.getElementsByTagName("initialStates").getLength();
    int initialProportionsCount = root.getElementsByTagName("initialProportions").getLength();

    int providedCount = 0;
    providedCount += (initialCellsCount > 0 ? 1 : 0);
    providedCount += (initialStatesCount > 0 ? 1 : 0);
    providedCount += (initialProportionsCount > 0 ? 1 : 0);

    if (providedCount > 1) {
      throw new ParserConfigurationException("Multiple grid configuration elements found. Please specify only one of initialCells, initialStates, or initialProportions.");
    }

    if (initialCellsCount > 0) {
      return parseInitialCells(root);
    } else if (initialStatesCount > 0) {
      return createCellsByRandomTotalStates(root);
    } else if (initialProportionsCount > 0) {
      return createCellsByRandomProportions(root);
    } else {
      throw new ParserConfigurationException("Missing grid configuration element. Please specify one of initialCells, initialStates, or initialProportions.");
    }
  }


  /**
   * Creates the initial grid by assigning cell states based on random proportions specified in the XML.
   *
   * @param root the root XML element.
   * @return a 2D list of {@code CellRecord} representing the grid.
   */
  private List<List<CellRecord>> createCellsByRandomProportions(Element root) {
    int width = Integer.parseInt(getTextValue(root, "width"));
    int height = Integer.parseInt(getTextValue(root, "height"));
    int totalCells = width * height;

    Set<Integer> acceptedStates = parseForAcceptedStates(root);
    Map<Integer,Integer> stateCounts = parseInitialProportions(root, acceptedStates, totalCells);
    List<Integer> randomizedStates = generateRandomizedStateList(stateCounts, totalCells);
    return createGrid(randomizedStates, width, height);
  }


  /**
   * Creates the initial grid by assigning cell states based on total state counts specified in the XML.
   *
   * @param root the root XML element.
   * @return a 2D list of {@code CellRecord} representing the grid.
   */
  private List<List<CellRecord>> createCellsByRandomTotalStates(Element root) {
    int width = Integer.parseInt(getTextValue(root, "width"));
    int height = Integer.parseInt(getTextValue(root, "height"));
    int totalCells = width * height;

    Set<Integer> acceptedStates = parseForAcceptedStates(root);
    Map<Integer, Integer> stateCounts = parseInitialStates(root, acceptedStates, totalCells);
    List<Integer> randomizedStates = generateRandomizedStateList(stateCounts, totalCells);
    return createGrid(randomizedStates, width, height);
  }


  /**
   * Generates a randomized list of cell states based on the provided state counts.
   *
   * @param stateCounts a map where the key is the state and the value is the number of cells in that state.
   * @param totalCells the total number of cells in the grid.
   * @return a shuffled list of cell state integers.
   * @throws IllegalStateException if the total number of states does not match the total cell count.
   */
  private List<Integer> generateRandomizedStateList(Map<Integer, Integer> stateCounts, int totalCells) {
    List<Integer> statesList = new ArrayList<>();

    for (Map.Entry<Integer, Integer> entry : stateCounts.entrySet()) {
      int state = entry.getKey();
      int count = entry.getValue();
      for (int i = 0; i < count; i++) {
        statesList.add(state);
      }
    }

    if (statesList.size() != totalCells) {
      throw new IllegalStateException("Mismatch between total cells and states list size.");
    }

    Collections.shuffle(statesList, new Random());
    return statesList;
  }

  /**
   * Parses the {@code initialStates} element from the XML and maps each state to its specified count.
   *
   * @param root the root XML element.
   * @param acceptedStates the set of accepted cell states.
   * @param totalCells the total number of cells in the grid.
   * @return a map where the key is the cell state and the value is the count for that state.
   * @throws IllegalArgumentException if the specified states or counts are invalid.
   */
  private Map<Integer, Integer> parseInitialStates(Element root, Set<Integer> acceptedStates, int totalCells) {
    Element initialStatesElement = (Element) root.getElementsByTagName("initialStates").item(0);

    Map<Integer, Integer> stateCounts = new HashMap<>();
    NodeList stateNodes = initialStatesElement.getChildNodes();
    int specifiedSum = 0;

    for (int i = 0; i < stateNodes.getLength(); i++) {
      Node node = stateNodes.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE) continue;

      Element stateElement = (Element) node;
      String tagName = stateElement.getTagName();

      if (!tagName.startsWith("state")) continue;

      String numberPart = tagName.substring("state".length());
      int state;
      try {
        state = Integer.parseInt(numberPart);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid state tag name: " + tagName);
      }

      if (!acceptedStates.contains(state)) {
        throw new IllegalArgumentException("State " + state + " is not among accepted states.");
      }

      int count;
      try {
        count = Integer.parseInt(stateElement.getTextContent().trim());
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid cell count for state " + state + " in <initialStates>.");
      }

      stateCounts.put(state, count);
      specifiedSum += count;
    }

    if (specifiedSum > totalCells) {
      throw new IllegalArgumentException("Total specified cells (" + specifiedSum +
          ") exceeds grid size (" + totalCells + ").");
    }

    if (!stateCounts.containsKey(0)) {
      if (!acceptedStates.contains(0)) {
        throw new IllegalArgumentException("Default state 0 is not in acceptedStates.");
      }
      int remaining = totalCells - specifiedSum;
      stateCounts.put(0, remaining);
    } else {
      if (specifiedSum != totalCells) {
        throw new IllegalArgumentException("When default state (0) is explicitly specified, " +
            "the sum of counts must equal grid size (" + totalCells + ").");
      }
    }
    return stateCounts;
  }

  /**
   * Parses the {@code initialProportions} element from the XML and calculates cell counts based on the specified proportions.
   *
   * @param root the root XML element.
   * @param acceptedStates the set of accepted cell states.
   * @param totalCells the total number of cells in the grid.
   * @return a map where the key is the cell state and the value is the calculated count for that state.
   * @throws IllegalArgumentException if any proportion values are invalid or the total proportions exceed 100%.
   * @throws IllegalStateException if the total assigned cell count does not match the grid size.
   */
  private Map<Integer, Integer> parseInitialProportions(Element root, Set<Integer> acceptedStates, int totalCells) {
    List<String> errorMessages = new ArrayList<>();
    Element proportionsElement = (Element) root.getElementsByTagName("initialProportions").item(0);

    Map<Integer, Double> proportions = new HashMap<>();
    double totalSpecifiedProportion = 0.0;

    if (proportionsElement != null) {
      NodeList nodes = proportionsElement.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        if (node.getNodeType() != Node.ELEMENT_NODE) continue;
        Element elem = (Element) node;
        String tagName = elem.getTagName();
        if (!tagName.startsWith("state")) continue;

        String numberPart = tagName.substring("state".length());
        int state;
        try {
          state = Integer.parseInt(numberPart);
        } catch (NumberFormatException e) {
          errorMessages.add("Invalid state tag name: " + tagName);
          continue;
        }

        if (!acceptedStates.contains(state)) {
          errorMessages.add("State " + state + " is not among accepted states.");
          continue;
        }

        double proportion;

        try {
          proportion = Double.parseDouble(elem.getTextContent().trim());
        } catch (NumberFormatException e) {
          errorMessages.add("Invalid proportion value for state " + state + " in <initialProportions>.");
          continue;
        }
        proportions.put(state, proportion);
        totalSpecifiedProportion += proportion;
      }
    }

    if (!proportions.containsKey(0)) {
      double remainder = 100.0 - totalSpecifiedProportion;
      if (remainder < 0) {
        errorMessages.add("Specified proportions exceed 100%.");
      } else {
        proportions.put(0, remainder);
        totalSpecifiedProportion = 100.0;
      }
    } else {
      if (Math.abs(totalSpecifiedProportion - 100.0) > 0.001) {
        errorMessages.add("When state 0 is provided, the sum of proportions must equal 100%. Found: " + totalSpecifiedProportion);
      }
    }

    if (!errorMessages.isEmpty()) {
      throw new IllegalArgumentException(String.join("\n", errorMessages));
    }

    Map<Integer, Integer> stateCounts = new HashMap<>();
    int sumCounts = 0;
    for (Map.Entry<Integer, Double> entry : proportions.entrySet()) {
      int state = entry.getKey();
      double percent = entry.getValue();
      if (state != 0) {
        int count = (int) Math.round(totalCells * (percent / 100.0));
        stateCounts.put(state, count);
        sumCounts += count;
      }
    }
    stateCounts.put(0, totalCells - sumCounts);

    int totalAssigned = stateCounts.values().stream().mapToInt(Integer::intValue).sum();
    if (totalAssigned != totalCells) {
      throw new IllegalStateException("Total assigned cells (" + totalAssigned + ") does not equal grid size (" + totalCells + ").");
    }

    return stateCounts;
  }

  /**
   * Creates a 2D grid of {@code CellRecord} objects from a flat list of states.
   *
   * @param statesList a list of cell state integers.
   * @param width the expected number of columns in the grid.
   * @param height the expected number of rows in the grid.
   * @return a 2D list of {@code CellRecord} representing the grid.
   */
  private List<List<CellRecord>> createGrid(List<Integer> statesList, int width, int height) {
    List<List<CellRecord>> grid = new ArrayList<>();
    int index = 0;
    for (int r = 0; r < height; r++) {
      List<CellRecord> row = new ArrayList<>();
      for (int c = 0; c < width; c++) {
        int state = statesList.get(index++);
        row.add(new CellRecord(state, new HashMap<>()));
      }
      grid.add(row);
    }
    return grid;
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

  /**
   * Returns a list of configuration file names available in the configuration folder.
   *
   * @return a list of file name strings.
   */
  public List<String> getFileNames(){
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
   * Retrieves the text content of the first occurrence of a given tag within an element.
   *
   * @param e the XML element.
   * @param tagName the name of the tag whose value is to be retrieved.
   * @return the text content of the tag.
   * @throws IllegalArgumentException if the tag does not exist.
   */
  private String getTextValue(Element e, String tagName) {
    NodeList nodeList = e.getElementsByTagName(tagName);
    if (nodeList.getLength() > 0) {
      return nodeList.item(0).getTextContent();
    } else {
      throw new IllegalArgumentException(tagName + " parameter in XML file does not exist.");
    }
  }

  /**
   * Parses the grid defined in the XML file from the {@code <initialCells>} format.
   * Each row contains multiple {@code <cell>} elements, which are converted to {@code CellRecord} objects.
   *
   * @param root the root XML element.
   * @return a 2D list of {@code CellRecord} representing the grid.
   * @throws IllegalArgumentException if required elements or attributes are missing or invalid.
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
                throw new IllegalArgumentException("Invalid cell state at row " + i + ", column " + j + ": " + attrValue, e);
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
   * <pre>
   *   &lt;parameters&gt;
   *     &lt;doubleParameter name="param1"&gt;1.23&lt;/doubleParameter&gt;
   *     &lt;stringParameter name="param2"&gt;value&lt;/stringParameter&gt;
   *   &lt;/parameters&gt;
   * </pre>
   *
   * @param root the root XML element.
   * @return a {@code ParameterRecord} containing maps of double and string parameters.
   * @throws IllegalArgumentException if parameter elements are missing required attributes or contain invalid values.
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

        }
      }
    }
    return new ParameterRecord(doubleParams, stringParams);
  }


  /**
   * Parses accepted states from a single {@code <acceptedStates>} element containing a space‐separated list.
   *
   * @param root the root XML element.
   * @return a set of accepted state integers.
   * @throws IllegalArgumentException if the accepted states element is missing or contains invalid values.
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
        throw new IllegalArgumentException("Invalid accepted state value: " + token);
      }
    }
    return acceptedStates;
  }

  /**
   * Checks the grid bounds and validates that every cell has an accepted state.
   *
   * @param gridWidth the expected grid width.
   * @param gridHeight the expected grid height.
   * @param acceptedStates the set of accepted states.
   * @param grid the 2D grid of {@code CellRecord} to be validated.
   * @throws IllegalArgumentException if the grid dimensions or cell states are invalid.
   */
  private void checkForInvalidInformation(int gridWidth, int gridHeight, Set<Integer> acceptedStates, List<List<CellRecord>> grid) {
    checkGridBounds(gridWidth, gridHeight, grid);
    checkInvalidStates(acceptedStates, grid);
  }

  /**
   * Validates that the grid dimensions match the expected width and height.
   *
   * @param width the expected number of columns.
   * @param height the expected number of rows.
   * @param grid the 2D grid of {@code CellRecord}.
   * @throws IllegalArgumentException if the grid dimensions do not match.
   */
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

  /**
   * Validates that every cell in the grid has a state that is among the accepted states.
   *
   * @param acceptedStates the set of accepted states.
   * @param grid the 2D grid of {@code CellRecord} to be validated.
   * @throws IllegalArgumentException if any cell contains an invalid state.
   */
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
