package cellsociety.model.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import cellsociety.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Updated ConfigWriter that outputs the new XML format.
 * <p>
 * The format includes:
 * <ul>
 *   <li><code>&lt;simulation&gt;</code> as the root element</li>
 *   <li><code>&lt;type&gt;</code>, <code>&lt;title&gt;</code>, <code>&lt;author&gt;</code>, and <code>&lt;description&gt;</code></li>
 *   <li><code>&lt;parameters&gt;</code> with nested <code>&lt;doubleParameter&gt;</code> and <code>&lt;stringParameter&gt;</code> elements</li>
 *   <li><code>&lt;width&gt;</code> and <code>&lt;height&gt;</code> for grid dimensions</li>
 *   <li><code>&lt;defaultSpeed&gt;</code></li>
 *   <li><code>&lt;initialCells&gt;</code> containing rows of <code>&lt;cell&gt;</code> elements with attributes</li>
 *   <li><code>&lt;acceptedStates&gt;</code> as a space–separated list</li>
 * </ul>
 */
public class ConfigWriter {

  private static final String DEFAULT_CONFIG_FOLDER = System.getProperty("user.dir")
      + "/src/main/resources/cellsociety/SimulationConfigurationData";
  private ConfigInfo myConfigInfo;
  private Document myCurrentXmlDocument;

  public ConfigWriter() {
  }

  /**
   * Saves the current configuration to an XML file at the given path.
   *
   * @param myNewConfigInfo the configuration info to save
   * @param path the directory where the XML file will be saved
   * @throws ParserConfigurationException if an error occurs during document creation
   */
  public void saveCurrentConfig(ConfigInfo myNewConfigInfo, String path)
      throws Exception {
    myConfigInfo = myNewConfigInfo;
    Document xmlDocument = createXMLDocument();
    File outputFile = createOutputFile(path);
    populateXMLDocument(xmlDocument);
    writeXMLDocument(xmlDocument, outputFile);
  }

  private Document createXMLDocument() throws ParserConfigurationException {
    try {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      return docBuilder.newDocument();
    } catch (ParserConfigurationException e) {
      throw new ParserConfigurationException("Error creating XML document: " + e.getMessage());
    }
  }

  /**
   * Populates the XML document with the configuration data.
   */
  private void populateXMLDocument(Document xmlDocument) {
    Element rootElement = xmlDocument.createElement("simulation");
    xmlDocument.appendChild(rootElement);

    Element typeElement = xmlDocument.createElement("type");
    typeElement.appendChild(xmlDocument.createTextNode(myConfigInfo.myType().toString()));
    rootElement.appendChild(typeElement);

    Element titleElement = xmlDocument.createElement("title");
    titleElement.appendChild(xmlDocument.createTextNode(myConfigInfo.myTitle()));
    rootElement.appendChild(titleElement);

    Element authorElement = xmlDocument.createElement("author");
    authorElement.appendChild(xmlDocument.createTextNode(myConfigInfo.myAuthor()));
    rootElement.appendChild(authorElement);

    Element descriptionElement = xmlDocument.createElement("description");
    descriptionElement.appendChild(xmlDocument.createTextNode(myConfigInfo.myDescription()));
    rootElement.appendChild(descriptionElement);

    Element parametersElement = xmlDocument.createElement("parameters");
    addParametersElements(parametersElement, myConfigInfo.myParameters(), xmlDocument);
    rootElement.appendChild(parametersElement);

    Element widthElement = xmlDocument.createElement("width");
    widthElement.appendChild(xmlDocument.createTextNode(String.valueOf(myConfigInfo.myGridWidth())));
    rootElement.appendChild(widthElement);

    Element heightElement = xmlDocument.createElement("height");
    heightElement.appendChild(xmlDocument.createTextNode(String.valueOf(myConfigInfo.myGridHeight())));
    rootElement.appendChild(heightElement);

    Element defaultSpeedElement = xmlDocument.createElement("defaultSpeed");
    defaultSpeedElement.appendChild(xmlDocument.createTextNode(String.valueOf(myConfigInfo.myTickSpeed())));
    rootElement.appendChild(defaultSpeedElement);

    Element initialCellsElement = xmlDocument.createElement("initialCells");
    addInitialCellsElements(initialCellsElement, myConfigInfo.myGrid(), xmlDocument);
    rootElement.appendChild(initialCellsElement);

    Element acceptedStatesElement = xmlDocument.createElement("acceptedStates");
    String acceptedStatesText = myConfigInfo.acceptedStates().stream()
        .map(String::valueOf)
        .collect(Collectors.joining(" "));
    acceptedStatesElement.appendChild(xmlDocument.createTextNode(acceptedStatesText));
    rootElement.appendChild(acceptedStatesElement);
  }

  /**
   * Adds parameter elements as children of the given parametersElement.
   * For each double parameter, creates a <doubleParameter name="...">value</doubleParameter> element,
   * and for each string parameter, creates a <stringParameter name="...">value</stringParameter> element.
   *
   * @param parametersElement the XML element to which parameter elements are added
   * @param params the ParameterRecord holding the parameters
   * @param xmlDocument the XML document being built
   */
  private void addParametersElements(Element parametersElement,
      ParameterRecord params, Document xmlDocument) {
    Map<String, Double> doubleParams = params.myDoubleParameters();
    Map<String, String> stringParams = params.myStringParameters();

    for (Map.Entry<String, Double> entry : doubleParams.entrySet()) {
      Element doubleParamElement = xmlDocument.createElement("doubleParameter");
      doubleParamElement.setAttribute("name", entry.getKey());
      doubleParamElement.appendChild(xmlDocument.createTextNode(String.valueOf(entry.getValue())));
      parametersElement.appendChild(doubleParamElement);
    }
    for (Map.Entry<String, String> entry : stringParams.entrySet()) {
      Element stringParamElement = xmlDocument.createElement("stringParameter");
      stringParamElement.setAttribute("name", entry.getKey());
      stringParamElement.appendChild(xmlDocument.createTextNode(entry.getValue()));
      parametersElement.appendChild(stringParamElement);
    }
  }

  /**
   * Adds the grid (initial cells) to the document.
   * Each row in the grid becomes a <row> element containing one or more <cell> elements.
   * Each <cell> element has a required "state" attribute and additional properties if available.
   *
   * @param initialCellsElement the XML element to which rows are added
   * @param grid the grid of cells (List of List of CellRecord)
   * @param xmlDocument the XML document being built
   */
  private void addInitialCellsElements(Element initialCellsElement,
      List<List<CellRecord>> grid, Document xmlDocument) {
    for (List<CellRecord> row : grid) {
      Element rowElement = xmlDocument.createElement("row");
      for (CellRecord cell : row) {
        Element cellElement = xmlDocument.createElement("cell");
        cellElement.setAttribute("state", String.valueOf(cell.state()));

        for (Map.Entry<String, Double> property : cell.properties().entrySet()) {
          cellElement.setAttribute(property.getKey(), String.valueOf(property.getValue()));
        }
        rowElement.appendChild(cellElement);
      }
      initialCellsElement.appendChild(rowElement);
    }
  }

  private File createOutputFile(String path) throws ParserConfigurationException {
    try {
      String baseFilename = myConfigInfo.myTitle().replaceAll(" ", "") + "Save";
      String fileExtension = ".xml";
      File configDirectory = new File(path);

      if (!configDirectory.exists() && !configDirectory.mkdirs()) {
        Log.error("Failed to create config directory: " + DEFAULT_CONFIG_FOLDER);
        return null;
      }

      File outputFile = new File(configDirectory, baseFilename + fileExtension);
      int duplicateNumber = 1;
      while (outputFile.exists()) {
        outputFile = new File(configDirectory, baseFilename + "_" + duplicateNumber + fileExtension);
        duplicateNumber++;
      }
      return outputFile;
    } catch (NullPointerException e) {
      throw new ParserConfigurationException("Could not create output file");
    }
  }

  private void writeXMLDocument(Document xmlDocument, File outputFile) throws Exception {
    if (outputFile == null) {
      Log.error("Output file is null. Cannot save XML.");
      return;
    }
    try {
      if (!outputFile.exists() && !outputFile.createNewFile()) {
        Log.error("Failed to create new XML file: " + outputFile.getAbsolutePath());
        return;
      }
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(xmlDocument);

      try (FileOutputStream fos = new FileOutputStream(outputFile)) {
        StreamResult result = new StreamResult(fos);
        transformer.transform(source, result);
        Log.error("Config saved to file: " + outputFile.getAbsolutePath());
      }
    } catch (IOException e) {
      throw new IOException(" ");
    } catch (Exception e) {
      throw new Exception(" ");
    }
  }
}

