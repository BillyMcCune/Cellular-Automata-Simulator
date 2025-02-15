package cellsociety.model.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Billy McCune Purpose: Assumptions: Dependecies (classes or packages): How to Use: Any
 * Other Details:
 */
public class ConfigWriter {

  private static final String DEFAULT_CONFIG_FOLDER = System.getProperty("user.dir") +
      "/src/main/resources/cellsociety/SimulationConfigurationData";
  private ConfigInfo myConfigInfo;
  private Document myCurrentxmlDocument;

  public ConfigWriter() {
  }

  public void saveCurrentConfig(ConfigInfo myNewConfigInfo)throws ParserConfigurationException {
    myConfigInfo = myNewConfigInfo;
    Document xmlDocument = createXMLDocument();
    File outputFile = createOutputFile();
    assert xmlDocument != null;
    populateXMLDocument(xmlDocument);
    writeXMLDocument(xmlDocument, outputFile);
  }


  private Document createXMLDocument() throws ParserConfigurationException {
    Document xmlDocument;
    try {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      xmlDocument = docBuilder.newDocument();
    } catch (ParserConfigurationException e) {
      // handle the exception more robustly in real code
      throw new ParserConfigurationException();
    }
    return xmlDocument;
  }

  private void populateXMLDocument(Document xmlDocument) {
    Element rootElement = xmlDocument.createElement("simulation");
    xmlDocument.appendChild(rootElement);  // Ensure the root element is added

    Element descriptionElement = xmlDocument.createElement("description");
    Element titleElement = xmlDocument.createElement("title");
    Element authorElement = xmlDocument.createElement("author");
    Element gridWidthElement = xmlDocument.createElement("gridWidth");
    Element gridHeightElement = xmlDocument.createElement("gridHeight");
    Element defaultSpeedElement = xmlDocument.createElement("defaultSpeed");
    Element initialStatesElement = xmlDocument.createElement("initialStates");

    titleElement.appendChild(xmlDocument.createTextNode(myConfigInfo.myTitle()));
    authorElement.appendChild(xmlDocument.createTextNode(myConfigInfo.myAuthor()));
    descriptionElement.appendChild(xmlDocument.createTextNode(myConfigInfo.myDescription()));
    gridHeightElement.appendChild(xmlDocument.createTextNode(String.valueOf(myConfigInfo.myGridWidth())));
    gridWidthElement.appendChild(xmlDocument.createTextNode(String.valueOf(myConfigInfo.myGridHeight())));
    defaultSpeedElement.appendChild(xmlDocument.createTextNode(String.valueOf(myConfigInfo.myTickSpeed())));

    rootElement.appendChild(titleElement);
    rootElement.appendChild(authorElement);
    rootElement.appendChild(descriptionElement);
    rootElement.appendChild(gridWidthElement);
    rootElement.appendChild(gridHeightElement);
    rootElement.appendChild(defaultSpeedElement);
    rootElement.appendChild(initialStatesElement);
    addInitialGridElements(initialStatesElement,myConfigInfo.myGrid(),xmlDocument);
  }

  private void addInitialGridElements(Element initialStatesElement, List<List<Integer>> grid,
      Document xmlDocument) {
    StringBuilder rowString;
    for (List<Integer> row : grid) {
      Element rowElement = null;
      rowString = new StringBuilder();
      for (Integer i : row) {
        rowString.append(i).append(" ");
      }
      System.out.println(rowString);
      rowElement = xmlDocument.createElement("row");
      rowElement.appendChild(xmlDocument.createTextNode(rowString.toString().toString().trim()));
      initialStatesElement.appendChild(rowElement);
    }
  }

  private File createOutputFile() {
    String baseFilename = myConfigInfo.myTitle().replaceAll(" ","") + "Save";
    String fileExtension = ".xml";

    File configDirectory = new File(DEFAULT_CONFIG_FOLDER);

    // Create directory if it does not exist
    if (!configDirectory.exists()) {
      if (!configDirectory.mkdirs()) {
        System.err.println("Failed to create config directory: " + DEFAULT_CONFIG_FOLDER);
        return null;  // Return null to indicate failure
      }
    }

    File outputFile = new File(configDirectory, baseFilename + fileExtension);
    int duplicateNumber = 1;
    while (outputFile.exists()) {
      outputFile = new File(configDirectory, baseFilename + "_" + duplicateNumber + fileExtension);
      duplicateNumber++;
    }

    return outputFile;
  }

  private void writeXMLDocument(Document xmlDocument, File outputFile) {
    if (outputFile == null) {
      System.err.println("Output file is null. Cannot save XML.");
      return;
    }

    try {
      // Ensure the file is created before writing
      if (!outputFile.exists()) {
        if (!outputFile.createNewFile()) {
          System.err.println("Failed to create new XML file: " + outputFile.getAbsolutePath());
          return;
        }
      }

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(xmlDocument);

      try (FileOutputStream fos = new FileOutputStream(outputFile)) {
        StreamResult result = new StreamResult(fos);
        transformer.transform(source, result);
        System.out.println("Config saved to file: " + outputFile.getAbsolutePath());
      }

    } catch (IOException e) {
      System.err.println("IOException while creating the file: " + e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void setConfigInfo(ConfigInfo configInfo) {
    myConfigInfo = configInfo;
  }
}