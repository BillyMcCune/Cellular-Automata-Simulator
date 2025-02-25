package cellsociety.model.config;

import cellsociety.view.controller.LanguageController;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
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
 * @author Billy McCune
 * A config writer class to create xml files
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
  private String LastFileSaved;
  private String errorMessage;

  public ConfigWriter() {
  }

  /**
   * Saves the current configuration to an XML file at the given path.
   *
   * @param myNewConfigInfo the configuration info to save
   * @param path the directory where the XML file will be saved
   * @throws Exception if an error occurs during saving
   */
  public void saveCurrentConfig(ConfigInfo myNewConfigInfo, String path)
      throws Exception {
    if (myNewConfigInfo == null) {
      errorMessage = LanguageController.getStringProperty("error-nullConfigInfo").getValue();
      throw new NullPointerException(errorMessage);
    }
    if (path == null) {
      errorMessage = LanguageController.getStringProperty("error-nullPath").getValue();
      throw new NullPointerException(errorMessage);
    }
    myConfigInfo = myNewConfigInfo;
    Document xmlDocument = createXMLDocument();
    File outputFile = createOutputFile(path);
    populateXMLDocument(xmlDocument);
    writeXMLDocument(xmlDocument, outputFile);
  }

  /**
   * Returns the name of the last file that was successfully saved.
   *
   * @return the last file saved as a String
   * @throws Error if no file has been saved yet
   */
  public String getLastFileSaved() {
    if (LastFileSaved == null) {
      errorMessage = LanguageController.getStringProperty("error-noLastFileSaved").getValue();
      throw new Error(errorMessage);
    }
    return LastFileSaved;
  }

  /**
   * Creates a new XML Document instance.
   *
   * @return a new Document
   * @throws ParserConfigurationException if a DocumentBuilder cannot be created
   */
  private Document createXMLDocument() throws ParserConfigurationException {
    try {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      return docBuilder.newDocument();
    } catch (ParserConfigurationException e) {
      errorMessage = LanguageController.getStringProperty("error-creatingXMLDocument").getValue();
      throw new ParserConfigurationException(MessageFormat.format(errorMessage, e.getMessage()));
    }
  }

  /**
   * Populates the XML document with the configuration data.
   * @param xmlDocument the XML document to populate
   */
  private void populateXMLDocument(Document xmlDocument) {
    Element rootElement = xmlDocument.createElement("simulation");
    xmlDocument.appendChild(rootElement);

    Element typeElement = xmlDocument.createElement("type");
    typeElement.appendChild(xmlDocument.createTextNode(myConfigInfo.myType().toString()));
    rootElement.appendChild(typeElement);

    Element cellShapeElement = xmlDocument.createElement("cellShapeType");
    cellShapeElement.appendChild(xmlDocument.createTextNode(myConfigInfo.myCellShape().toString()));
    rootElement.appendChild(cellShapeElement);

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
   * Each dx in the grid becomes a <dx> element containing one or more <cell> elements.
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

  /**
   * Creates an output file for saving the XML document.
   * <p>
   * The file name is based on the configuration title with spaces removed and appended with "Save" and ".xml".
   * If a file with the same name exists, a duplicate number is appended.
   *
   * @param path the directory path where the file should be saved
   * @return a File object representing the output file
   * @throws ParserConfigurationException if the output file cannot be created
   */
  private File createOutputFile(String path) throws ParserConfigurationException {
    try {
      String baseFilename = myConfigInfo.myTitle().replaceAll(" ", "") + "Save";
      String fileExtension = ".xml";
      File configDirectory = new File(path);
      LastFileSaved = baseFilename + fileExtension;

      if (configDirectory.exists() && !configDirectory.isDirectory()) {
        errorMessage = LanguageController.getStringProperty("error-notDirectory").getValue();
        throw new ParserConfigurationException(errorMessage);
      }
      if (!configDirectory.exists() && !configDirectory.mkdirs()) {
        errorMessage = LanguageController.getStringProperty("error-failedToCreateConfigDirectory").getValue();
        throw new ParserConfigurationException(MessageFormat.format(errorMessage, path));
      }

      File outputFile = new File(configDirectory, baseFilename + fileExtension);
      int duplicateNumber = 1;
      while (outputFile.exists()) {
        outputFile = new File(configDirectory, baseFilename + "_" + duplicateNumber + fileExtension);
        duplicateNumber++;
      }
      return outputFile;
    } catch (NullPointerException e) {
      errorMessage = LanguageController.getStringProperty("error-couldNotCreateOutputFile").getValue();
      throw new ParserConfigurationException(errorMessage);
    }
  }

  /**
   * Writes the XML Document to the specified output file.
   *
   * @param xmlDocument the XML document to write
   * @param outputFile the file to which the document will be written
   * @throws Exception if an error occurs during file writing or XML transformation
   */
  private void writeXMLDocument(Document xmlDocument, File outputFile) throws Exception {
    if (outputFile == null) {
      errorMessage = LanguageController.getStringProperty("error-outputFileNull").getValue();
      throw new IllegalArgumentException(errorMessage);
    }
    try {
      if (outputFile == null) {
        Log.error("Output file is null. Cannot save XML.");
        errorMessage = LanguageController.getStringProperty("error-outputFileNull").getValue();
        throw new IllegalArgumentException(errorMessage);
      }
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(xmlDocument);

      transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

      try (FileOutputStream fos = new FileOutputStream(outputFile)) {
        StreamResult result = new StreamResult(fos);
        transformer.transform(source, result);
        Log.trace("Config saved to file: " + outputFile.getAbsolutePath());
      }
    } catch (IOException e) {
      errorMessage = LanguageController.getStringProperty("error-writingXMLFile").getValue();
      throw new IOException(errorMessage, e);
    } catch (Exception e) {
      errorMessage = LanguageController.getStringProperty("error-transformingXMLDocument").getValue();
      throw new Exception(errorMessage, e);
    }
  }
}

