package cellsociety.model.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Billy McCune
 * The RandomStatesAndProportionsGridReader class is responsible for creating
 * grids based on random state counts or proportions defined in an XML document.
 * <p>
 * It provides methods to generate a grid (as a 2D list of CellRecord) using either total state
 * counts or state proportions.
 */
public class RandomStatesAndProportionsGridReader {

  /**
   * Creates the initial grid by assigning cell states based on total state counts specified in the
   * XML.
   *
   * @param root the root XML element
   * @return a 2D list of {@code CellRecord} representing the grid
   * @throws IllegalArgumentException if any parsing error occurs
   */
  public static List<List<CellRecord>> createCellsByRandomTotalStates(Element root) {
    int width = Integer.parseInt(getTextValue(root, "width"));
    int height = Integer.parseInt(getTextValue(root, "height"));
    int totalCells = width * height;

    // Reuse GridReader's method to read accepted states.
    Set<Integer> acceptedStates = GridReader.readAcceptedStates(root);
    Map<Integer, Integer> stateCounts = parseInitialStates(root, acceptedStates, totalCells);
    List<Integer> randomizedStates = generateRandomizedStateList(stateCounts, totalCells);
    return createGrid(randomizedStates, width, height);
  }

  /**
   * Creates the initial grid by assigning cell states based on random proportions specified in the
   * XML.
   *
   * @param root the root XML element
   * @return a 2D list of {@code CellRecord} representing the grid
   * @throws IllegalArgumentException if any parsing or validation error occurs
   */
  public static List<List<CellRecord>> createCellsByRandomProportions(Element root) {
    int width = Integer.parseInt(getTextValue(root, "width"));
    int height = Integer.parseInt(getTextValue(root, "height"));
    int totalCells = width * height;

    Set<Integer> acceptedStates = GridReader.readAcceptedStates(root);
    Map<Integer, Integer> stateCounts = parseInitialProportions(root, acceptedStates, totalCells);
    List<Integer> randomizedStates = generateRandomizedStateList(stateCounts, totalCells);
    return createGrid(randomizedStates, width, height);
  }

  /**
   * Parses the initial states defined by total state counts from the XML.
   *
   * @param root           the root XML element
   * @param acceptedStates the set of accepted states
   * @param totalCells     the total number of cells in the grid
   * @return a map where each state is mapped to its parsed cell count
   * @throws IllegalArgumentException if any parsing or validation error occurs
   */
  private static Map<Integer, Integer> parseInitialStates(Element root, Set<Integer> acceptedStates,
      int totalCells) {
    Element initialStatesElement = getInitialStatesElement(root);
    Map<Integer, Integer> stateCounts = new HashMap<>();
    NodeList stateNodes = initialStatesElement.getChildNodes();
    int specifiedSum = 0;

    for (int i = 0; i < stateNodes.getLength(); i++) {
      Node node = stateNodes.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      Element stateElement = (Element) node;
      // Process each state element and add its count to the map.
      specifiedSum += processStateElement(stateElement, acceptedStates, stateCounts);
    }

    validateTotalSum(specifiedSum, totalCells, stateCounts, acceptedStates);
    return stateCounts;
  }

  /**
   * Retrieves the <code>&lt;initialStates&gt;</code> element from the root.
   *
   * @param root the root XML element
   * @return the <code>&lt;initialStates&gt;</code> element
   * @throws IllegalArgumentException if the element is missing
   */
  private static Element getInitialStatesElement(Element root) {
    Element initialStatesElement = (Element) root.getElementsByTagName("initialStates").item(0);
    if (initialStatesElement == null) {
      throw new IllegalArgumentException("error-missingInitialStates");
    }
    return initialStatesElement;
  }

  /**
   * Processes a single state element by extracting its state number and cell count.
   *
   * @param stateElement   the state element (e.g., &lt;state1&gt;)
   * @param acceptedStates the set of accepted states
   * @param stateCounts    the map where the parsed state and its count are stored
   * @return the cell count for the processed state element
   * @throws IllegalArgumentException if the state tag is invalid, not accepted, or if the cell
   *                                  count cannot be parsed
   */
  private static int processStateElement(Element stateElement, Set<Integer> acceptedStates,
      Map<Integer, Integer> stateCounts) {
    String tagName = stateElement.getTagName();
    if (!tagName.startsWith("state")) {
      return 0; // Ignore any non-state elements.
    }

    String numberPart = tagName.substring("state".length());
    int state = parseStateTag(numberPart);

    if (!acceptedStates.contains(state)) {
      throw new IllegalArgumentException("error-stateIsNotInAcceptedStates," + state);
    }

    int count = parseStateCount(stateElement, state);
    stateCounts.put(state, count);
    return count;
  }

  /**
   * Parses the state number from the given tag suffix.
   *
   * @param numberPart the numeric part of the tag (e.g., "1" from "state1")
   * @return the state as an integer
   * @throws IllegalArgumentException if the number cannot be parsed
   */
  private static int parseStateTag(String numberPart) {
    try {
      return Integer.parseInt(numberPart);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("error-invalidStateTag," + numberPart);
    }
  }

  /**
   * Parses the cell count for a given state element.
   *
   * @param stateElement the state element
   * @param state        the state number
   * @return the cell count as an integer
   * @throws IllegalArgumentException if the cell count cannot be parsed
   */
  private static int parseStateCount(Element stateElement, int state) {
    try {
      return Integer.parseInt(stateElement.getTextContent().trim());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("error-invalidCellCountForState," + state);
    }
  }

  /**
   * Validates the total sum of the cell counts specified in the XML.
   * <p>
   * If the specified sum exceeds the total number of cells, an error is thrown. If state 0 is not
   * specified, its count is computed as the remainder. If state 0 is specified but the sum does not
   * equal the total, an error is thrown.
   *
   * @param specifiedSum   the sum of cell counts from the XML
   * @param totalCells     the total number of cells in the grid
   * @param stateCounts    the map of parsed state counts
   * @param acceptedStates the set of accepted states
   * @throws IllegalArgumentException if validation fails
   */
  private static void validateTotalSum(int specifiedSum, int totalCells,
      Map<Integer, Integer> stateCounts, Set<Integer> acceptedStates) {
    if (specifiedSum > totalCells) {
      throw new IllegalArgumentException(
          "error-TotalCellExceedsGridSize," + specifiedSum + "," + totalCells);
    }

    if (!stateCounts.containsKey(0)) {
      if (!acceptedStates.contains(0)) {
        throw new IllegalArgumentException("error-NoDefaultZeroInAcceptedStates");
      }
      int remaining = totalCells - specifiedSum;
      stateCounts.put(0, remaining);
    } else if (specifiedSum != totalCells) {
      throw new IllegalArgumentException("error-specifiedSumDoesNotEqualTotalSum," + totalCells);
    }
  }


  /**
   * Parses the initial states defined by random proportions from the XML.
   *
   * @param root           the root XML element
   * @param acceptedStates the set of accepted states
   * @param totalCells     the total number of cells in the grid
   * @return a map where each state is mapped to its calculated cell count based on proportions
   * @throws IllegalArgumentException if any proportion value is invalid or if validation fails
   */
  private static Map<Integer, Integer> parseInitialProportions(Element root,
      Set<Integer> acceptedStates, int totalCells) {
    List<String> errorMessages = new ArrayList<>();
    Element proportionsElement = (Element) root.getElementsByTagName("initialProportions").item(0);
    Map<Integer, Double> proportions = new HashMap<>();
    double totalSpecifiedProportion = 0.0;
    if (proportionsElement != null) {
      ProportionResult result = extractProportions(proportionsElement, acceptedStates,
          errorMessages);
      proportions = result.proportions;
      totalSpecifiedProportion = result.totalSpecifiedProportion;
    }
    totalSpecifiedProportion = finalizeProportions(proportions, totalSpecifiedProportion,
        errorMessages);
    if (!errorMessages.isEmpty()) {
      throw new IllegalArgumentException(String.join("\n", errorMessages));
    }
    return computeStateCounts(proportions, totalCells);
  }

  /**
   * A helper class that holds the parsed state proportions and their total sum.
   */
  private static class ProportionResult {

    Map<Integer, Double> proportions;
    double totalSpecifiedProportion;

    ProportionResult(Map<Integer, Double> proportions, double totalSpecifiedProportion) {
      this.proportions = proportions;
      this.totalSpecifiedProportion = totalSpecifiedProportion;
    }
  }

  /**
   * Extracts state proportions from the <code>&lt;initialProportions&gt;</code> element.
   *
   * @param proportionsElement the <code>&lt;initialProportions&gt;</code> element
   * @param acceptedStates     the set of accepted states
   * @param errorMessages      a list to collect error messages during parsing
   * @return a ProportionResult containing a map of state proportions and the total sum of
   * proportions
   */
  private static ProportionResult extractProportions(Element proportionsElement,
      Set<Integer> acceptedStates, List<String> errorMessages) {
    Map<Integer, Double> proportions = new HashMap<>();
    double totalSpecifiedProportion = 0.0;
    NodeList nodes = proportionsElement.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      Element elem = (Element) node;
      String tagName = elem.getTagName();
      if (!tagName.startsWith("state")) {
        continue;
      }
      String numberPart = tagName.substring("state".length());
      int state;
      try {
        state = Integer.parseInt(numberPart);
      } catch (NumberFormatException e) {
        errorMessages.add("error-invalidStateTag," + numberPart);
        continue;
      }
      if (!acceptedStates.contains(state)) {
        errorMessages.add("error-stateIsNotInAcceptedStates," + state);
        continue;
      }
      double proportion;
      try {
        proportion = Double.parseDouble(elem.getTextContent().trim());
      } catch (NumberFormatException e) {
        errorMessages.add("error-InvalidProportion," + state);
        continue;
      }
      proportions.put(state, proportion);
      totalSpecifiedProportion += proportion;
    }
    return new ProportionResult(proportions, totalSpecifiedProportion);
  }

  /**
   * Finalizes the proportions by ensuring that state 0 is set to the remaining proportion if not
   * explicitly provided.
   *
   * @param proportions              a map of state proportions
   * @param totalSpecifiedProportion the total sum of the specified proportions
   * @param errorMessages            a list to collect error messages during finalization
   * @return the finalized total proportion
   */
  private static double finalizeProportions(Map<Integer, Double> proportions,
      double totalSpecifiedProportion, List<String> errorMessages) {
    if (!proportions.containsKey(0)) {
      double remainder = 100.0 - totalSpecifiedProportion;
      if (remainder < 0) {
        errorMessages.add("error-proportionsExceed100");
      } else {
        proportions.put(0, remainder);
        totalSpecifiedProportion = 100.0;
      }
    } else {
      if (Math.abs(totalSpecifiedProportion - 100.0) > 0.001) {
        errorMessages.add("error-totalProportionsGreaterThan100," + totalSpecifiedProportion);
      }
    }
    return totalSpecifiedProportion;
  }

  /**
   * Computes the final cell counts for each state based on the provided proportions.
   *
   * @param proportions a map of state proportions
   * @param totalCells  the total number of cells in the grid
   * @return a map where each state is mapped to its calculated cell count
   * @throws IllegalStateException if the total calculated count does not equal the total number of
   *                               cells
   */
  private static Map<Integer, Integer> computeStateCounts(Map<Integer, Double> proportions,
      int totalCells) {
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
      throw new IllegalStateException(
          "error-TotalCellExceedsGridSize," + totalAssigned + "," + totalCells);
    }
    return stateCounts;
  }

  /**
   * Generates a randomized list of cell state integers based on the provided state counts.
   *
   * @param stateCounts a map where each state is mapped to its cell count
   * @param totalCells  the total number of cells in the grid
   * @return a shuffled list of cell state integers
   * @throws IllegalStateException if the generated list size does not match the total number of
   *                               cells
   */
  private static List<Integer> generateRandomizedStateList(Map<Integer, Integer> stateCounts,
      int totalCells) {
    List<Integer> statesList = new ArrayList<>();
    for (Map.Entry<Integer, Integer> entry : stateCounts.entrySet()) {
      int state = entry.getKey();
      int count = entry.getValue();
      for (int i = 0; i < count; i++) {
        statesList.add(state);
      }
    }
    if (statesList.size() != totalCells) {
      throw new IllegalStateException("error-totalCellsDoesntEqualTotalStates");
    }
    Collections.shuffle(statesList, new Random());
    return statesList;
  }

  /**
   * Creates a 2D grid (list of lists) of {@code CellRecord} objects from a flat list of cell state
   * integers.
   *
   * @param statesList a list of cell state integers
   * @param width      the number of columns in the grid
   * @param height     the number of rows in the grid
   * @return a 2D list of {@code CellRecord} representing the grid
   */
  private static List<List<CellRecord>> createGrid(List<Integer> statesList, int width,
      int height) {
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
   * Retrieves the text content of the first occurrence of the specified tag within an element.
   *
   * @param e       the XML element
   * @param tagName the tag name to search for
   * @return the text content of the tag
   * @throws IllegalArgumentException if the tag does not exist
   */
  private static String getTextValue(Element e, String tagName) {
    NodeList nodeList = e.getElementsByTagName(tagName);
    if (nodeList.getLength() > 0) {
      return nodeList.item(0).getTextContent();
    } else {
      throw new IllegalArgumentException("error-parameterDoesNotExist," + tagName);
    }
  }
}
