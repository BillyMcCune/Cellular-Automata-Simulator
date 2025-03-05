package cellsociety.model.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GridReader {

  /**
   * Reads the initial grid configuration from the XML root element. The method determines whether
   * the grid is specified using <code>initialCells</code>, <code>initialStates</code>, or
   * <code>initialProportions</code>.
   *
   * @param root the root XML element
   * @return a 2D list of {@code CellRecord} representing the initial grid
   * @throws ParserConfigurationException if multiple grid configuration elements are found or if
   *                                      the grid configuration is missing
   * @author Billy McCune
   */
  public static List<List<CellRecord>> readInitialGrid(Element root)
      throws ParserConfigurationException, IllegalArgumentException {
    int initialCellsCount = root.getElementsByTagName("initialCells").getLength();
    int initialStatesCount = root.getElementsByTagName("initialStates").getLength();
    int initialProportionsCount = root.getElementsByTagName("initialProportions").getLength();

    int providedCount = 0;
    providedCount += (initialCellsCount > 0 ? 1 : 0);
    providedCount += (initialStatesCount > 0 ? 1 : 0);
    providedCount += (initialProportionsCount > 0 ? 1 : 0);

    if (providedCount > 1) {
      throw new ParserConfigurationException("error-multipleGridConfigs");
    }

    if (initialCellsCount > 0) {
      return parseInitialCells(root);
    } else if (initialStatesCount > 0) {
      return RandomStatesAndProportionsGridReader.createCellsByRandomTotalStates(root);
    } else if (initialProportionsCount > 0) {
      return RandomStatesAndProportionsGridReader.createCellsByRandomProportions(root);
    } else {
      throw new ParserConfigurationException("error-missingGridConfig");
    }
  }

  /**
   * Reads accepted states from the XML.
   *
   * @param root the XML root element
   * @return a set of accepted state integers
   * @throws IllegalArgumentException if the accepted states element is missing or empty, or if an
   *                                  accepted state cannot be parsed as an integer
   */
  public static Set<Integer> readAcceptedStates(Element root) throws IllegalArgumentException {
    Element acceptedStatesElement = (Element) root.getElementsByTagName("acceptedStates").item(0);
    if (acceptedStatesElement == null) {
      throw new IllegalArgumentException("error-MissingAcceptedState");
    }
    String statesText = acceptedStatesElement.getTextContent().trim();
    if (statesText.isEmpty()) {
      throw new IllegalArgumentException("error-acceptedStatesEmpty");
    }
    Set<Integer> acceptedStates = new HashSet<>();
    String[] tokens = statesText.split("\\s+");
    for (String token : tokens) {
      try {
        acceptedStates.add(Integer.parseInt(token));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("error-invalidAcceptedState," + token);
      }
    }
    return acceptedStates;
  }

  /**
   * Parses the grid defined by the <initialCells> element.
   *
   * @param root the XML root element
   * @return a 2D list of {@code CellRecord} representing the grid
   * @throws IllegalArgumentException if there are no rows or if any row is empty
   */
  private static List<List<CellRecord>> parseInitialCells(Element root)
      throws IllegalArgumentException {
    Element initialCellsElement = getInitialCellsElement(root);
    NodeList rowNodes = initialCellsElement.getElementsByTagName("row");
    if (rowNodes == null || rowNodes.getLength() == 0) {
      throw new IllegalArgumentException("error-noRowsInInitialCells");
    }

    List<List<CellRecord>> grid = new ArrayList<>();
    for (int i = 0; i < rowNodes.getLength(); i++) {
      Node rowNode = rowNodes.item(i);
      if (rowNode.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      Element rowElement = (Element) rowNode;
      List<CellRecord> rowCells = parseRow(rowElement, i);
      if (rowCells.isEmpty()) {
        throw new IllegalArgumentException("error-EmptyRow," + i);
      }
      grid.add(rowCells);
    }
    return grid;
  }

  /**
   * Retrieves the <initialCells> element from the root.
   *
   * @param root the XML root element
   * @return the <initialCells> element
   * @throws IllegalArgumentException if the <initialCells> element is missing
   */
  private static Element getInitialCellsElement(Element root) throws IllegalArgumentException {
    Element initialCellsElement = (Element) root.getElementsByTagName("initialCells").item(0);
    if (initialCellsElement == null) {
      throw new IllegalArgumentException("error-missingInitialCells");
    }
    return initialCellsElement;
  }


  /**
   * Parses a single row element into a list of {@code CellRecord} objects.
   *
   * @param rowElement the <row> element
   * @param rowIndex   the index of the row (for error reporting)
   * @return a list of {@code CellRecord} objects for that row
   */
  private static List<CellRecord> parseRow(Element rowElement, int rowIndex)
      throws IllegalArgumentException {
    NodeList cellNodes = rowElement.getElementsByTagName("cell");
    List<CellRecord> rowCells = new ArrayList<>();
    for (int j = 0; j < cellNodes.getLength(); j++) {
      Node cellNode = cellNodes.item(j);
      if (cellNode.getNodeType() == Node.ELEMENT_NODE) {
        Element cellElement = (Element) cellNode;
        CellRecord cell = parseCell(cellElement, rowIndex, j);
        rowCells.add(cell);
      }
    }
    return rowCells;
  }

  /**
   * Parses an individual <cell> element into a {@code CellRecord}.
   *
   * @param cellElement the <cell> element
   * @param rowIndex    the row index (for error reporting)
   * @param colIndex    the column index (for error reporting)
   * @return a {@code CellRecord} object representing the cell
   * @throws IllegalArgumentException if the "state" attribute is missing or invalid
   */
  private static CellRecord parseCell(Element cellElement, int rowIndex, int colIndex)
      throws IllegalArgumentException {
    String stateStr = cellElement.getAttribute("state");
    if (stateStr == null || stateStr.isEmpty()) {
      throw new IllegalArgumentException("error-missingCellState," + rowIndex + "," + colIndex);
    }
    int state;
    try {
      state = Integer.parseInt(stateStr);
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException(
          "error-invalidCellState," + rowIndex + "," + colIndex + "," + stateStr);
    }
    Map<String, Double> properties = parseCellProperties(cellElement, rowIndex, colIndex);
    return new CellRecord(state, properties);
  }


  /**
   * Parses the non-state attributes (properties) of a <cell> element.
   *
   * @param cellElement the <cell> element
   * @param rowIndex    the row index (for error reporting)
   * @param colIndex    the column index (for error reporting)
   * @return a map of property names to their double values
   * @throws IllegalArgumentException if any property value cannot be parsed as a double
   */
  private static Map<String, Double> parseCellProperties(Element cellElement, int rowIndex,
      int colIndex) throws IllegalArgumentException {
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
          throw new IllegalArgumentException(
              "error-InvalidCellState," + rowIndex + "," + colIndex + "," + attrName);
        }
      }
    }
    return properties;
  }

  /**
   * Retrieves the text content of the first occurrence of the specified tag within an element.
   *
   * @param e       the XML element
   * @param tagName the tag name to search for
   * @return the text content of the tag
   * @throws IllegalArgumentException if the tag does not exist
   */
  private static String getTextValue(Element e, String tagName) throws IllegalArgumentException {
    NodeList nodeList = e.getElementsByTagName(tagName);
    if (nodeList.getLength() > 0) {
      return nodeList.item(0).getTextContent();
    } else {
      throw new IllegalArgumentException("error-parameterDoesNotExist," + tagName);
    }
  }
}

