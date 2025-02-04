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

  public ConfigWriter(ConfigInfo configInfo) {
    myConfigInfo = configInfo;
  }


  public void saveCurrentConfig() throws ParserConfigurationException {
    File outputFile = createOutputFile();
    Document xmlDocument = createXMLDocument();
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
      e.printStackTrace();
      System.err.println("Error creating XML Document For Configuration Save");
      return null;
    }
    return xmlDocument;
  }

  private void populateXMLDocument(Document xmlDocument) {
    Element rootElement = xmlDocument.createElement("simulation");
    Element descriptionElement = xmlDocument.createElement("description");
    Element titleElement = xmlDocument.createElement("title");
    Element authorElement = xmlDocument.createElement("author");
    Element gridWidthElement = xmlDocument.createElement("gridWidth");
    Element gridHeightElement = xmlDocument.createElement("gridHeight");
    Element defaultSpeedElement = xmlDocument.createElement("defaultSpeed");
    Element initialStatesElement = xmlDocument.createElement("initialStates");

    addInitialGridElements(initialStatesElement, myConfigInfo.getGrid(), xmlDocument);

    titleElement.appendChild(xmlDocument.createTextNode(myConfigInfo.getTitle()));
    authorElement.appendChild(xmlDocument.createTextNode(myConfigInfo.getAuthor()));
    descriptionElement.appendChild(xmlDocument.createTextNode(myConfigInfo.getDescription()));
    gridHeightElement.appendChild(
        xmlDocument.createTextNode(String.valueOf(myConfigInfo.getWidth())));
    gridWidthElement.appendChild(
        xmlDocument.createTextNode(String.valueOf(myConfigInfo.getHeight())));
    defaultSpeedElement.appendChild(
        xmlDocument.createTextNode(String.valueOf(myConfigInfo.getSpeed())));

    xmlDocument.appendChild(rootElement);
    rootElement.appendChild(titleElement);
    rootElement.appendChild(authorElement);
    rootElement.appendChild(descriptionElement);
    rootElement.appendChild(gridWidthElement);
    rootElement.appendChild(gridHeightElement);
    rootElement.appendChild(defaultSpeedElement);
    rootElement.appendChild(initialStatesElement);
  }

  private void addInitialGridElements(Element initialStatesElement, List<List<Integer>> grid,
      Document xmlDocument) {
    StringBuilder rowString = new StringBuilder();
    for (List<Integer> row : grid) {
      Element rowElement = null;
      for (Integer i : row) {
        rowString.append(i).append(" ");
      }
      rowElement = xmlDocument.createElement(rowString.toString().trim());
      initialStatesElement.appendChild(rowElement);
    }
  }

  private File createOutputFile() {
    String baseFilename = myConfigInfo.getTitle() + "Save";
    String fileExtension = ".xml";

    // 2) Decide which folder to save into
    File configDirectory = new File(DEFAULT_CONFIG_FOLDER);
    if (!configDirectory.exists()) {
      System.err.println("Config directory does not exist");
    }

    // 3) Check for duplicates and add a numeric suffix if needed
    File outputFile = new File(configDirectory, baseFilename + fileExtension);
    int duplicateNumber = 1;
    while (outputFile.exists()) {
      outputFile = new File(configDirectory, baseFilename + "_" + duplicateNumber + fileExtension);
      duplicateNumber++;
    }
    return outputFile;
  }

  private void writeXMLDocument(Document xmlDocument, File outputFile) {
    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(xmlDocument);

      try (FileOutputStream fos = new FileOutputStream(outputFile)) {
        StreamResult result = new StreamResult(fos);
        transformer.transform(source, result);
        System.out.println("Config saved to file: " + outputFile.getAbsolutePath());
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}