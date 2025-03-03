package cellsociety.model.modelAPI;

import cellsociety.model.data.constants.BoundaryType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.neighbors.NeighborCalculator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * The StyleManager class is responsible for managing simulation style preferences.
 * <p>
 * It provides methods to retrieve possible values for neighbor arrangements, edge policies,
 * and cell shapes from the style properties file. It also supports updating style preferences
 * such as edge policy, neighbor arrangement, cell shape, and grid outline preference.
 * </p>
 */
public class StyleManager {


  //Style Property names:
  private final String gridOutlineProperty = "GRIDOUTLINE.PREFERENCE";
  private final String edgePolicyProperty = "EDGEPOLICY.PREFERENCE";
  private final String neighborArrangementProperty = "NEIGHBORARRANGEMENT.PREFERENCE";
  private final String cellShapeProperty = "CELLSHAPE.PREFERENCE";

  private NeighborCalculator<?> myNeighborCalculator;


  public StyleManager(NeighborCalculator neighborCalculator) {
    this.myNeighborCalculator = neighborCalculator;
  }

  /**
   * Sets the neighbor arrangement preference.
   *
   * @param neighborArrangement the new neighbor arrangement value
   * @throws NoSuchElementException if the neighbor arrangement cannot be updated due to an I/O
   *                                error
   */
  public void setNeighborArrangement(String neighborArrangement) {
    try {
      Properties simulationStyle = new Properties();
      File file = new File("SimulationStyle.properties");
      if (file.exists()) {
        try (InputStream input = new FileInputStream(file)) {
          simulationStyle.load(input);
        }
      }
      simulationStyle.setProperty(neighborArrangementProperty, neighborArrangement);
      myNeighborCalculator.setNeighborType(NeighborType.valueOf(neighborArrangement));
      try (OutputStream output = new FileOutputStream(file)) {
        simulationStyle.store(output, "User-defined cell colors");
      }
    } catch (IOException e) {
      throw new NoSuchElementException("error-setNeighborArrangement");
    }
  }

  /**
   * Retrieves a list of possible neighbor arrangements defined in the simulation style properties.
   *
   * @return a list of possible neighbor arrangement values
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public List<String> getPossibleNeighborArrangements() {
    List<String> arrangements = new ArrayList<>();
    try (InputStream input = new FileInputStream("SimulationStyle.properties")) {
      Properties simulationStyle = new Properties();
      simulationStyle.load(input);
      for (String key : simulationStyle.stringPropertyNames()) {
        // Exclude the preference key and any blank values.
        if (key.startsWith("NEIGHBORARRANGEMENT.") && !key.equals(
            "NEIGHBORARRANGEMENT.PREFERENCE")) {
          String value = simulationStyle.getProperty(key);
          if (value != null && !value.trim().isEmpty()) {
            arrangements.add(value.trim());
          }
        }
      }
    } catch (IOException e) {
      throw new NoSuchElementException("error-getPossibleNeighborArrangements");
    }
    return arrangements;
  }

  /**
   * Sets the edge policy for the simulation.
   *
   * @param edgePolicy the new edge policy value
   * @throws NoSuchElementException if the edge policy cannot be updated due to an I/O error
   */
  public void setEdgePolicy(String edgePolicy) {
    try {
      Properties simulationStyle = new Properties();
      File file = new File("SimulationStyle.properties");
      if (file.exists()) {
        try (InputStream input = new FileInputStream(file)) {
          simulationStyle.load(input);
        }
      }
      simulationStyle.setProperty(edgePolicyProperty, edgePolicy);
      myNeighborCalculator.setBoundary(BoundaryType.valueOf(edgePolicy));
      try (OutputStream output = new FileOutputStream(file)) {
        simulationStyle.store(output, "User-defined cell colors");
      }
    } catch (IOException e) {
      throw new NoSuchElementException("error-setEdgePolicy");
    }
  }

  /**
   * Retrieves a list of possible edge policies defined in the simulation style properties.
   *
   * @return a list of possible edge policy values
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public List<String> getPossibleEdgePolicies() {
    List<String> edgePolicies = new ArrayList<>();
    try (InputStream input = new FileInputStream("SimulationStyle.properties")) {
      Properties simulationStyle = new Properties();
      simulationStyle.load(input);
      for (String key : simulationStyle.stringPropertyNames()) {
        if (key.startsWith("EDGEPOLICY.") && !key.equals("EDGEPOLICY.PREFERENCE")) {
          String value = simulationStyle.getProperty(key);
          if (value != null && !value.trim().isEmpty()) {
            edgePolicies.add(value.trim());
          }
        }
      }
    } catch (IOException e) {
      throw new NoSuchElementException("error-getPossibleEdgePolicies");
    }
    return edgePolicies;
  }


  /**
   * Sets the cell shape preference.
   *
   * @param cellShape the new cell shape value (e.g., "SQUARE", "HEXAGON")
   * @throws NoSuchElementException if the cell shape cannot be updated due to an I/O error
   */
  public void setCellShape(String cellShape) {
    try {
      Properties simulationStyle = new Properties();
      File file = new File("SimulationStyle.properties");
      if (file.exists()) {
        try (InputStream input = new FileInputStream(file)) {
          simulationStyle.load(input);
        }
      }
      simulationStyle.setProperty(cellShapeProperty, cellShape);
      cellShape = cellShape.toUpperCase();
      myNeighborCalculator.setShape(GridShape.valueOf(cellShape));
      try (OutputStream output = new FileOutputStream(file)) {
        simulationStyle.store(output, "User-defined cell colors");
      }
    } catch (IOException e) {
      throw new NoSuchElementException("error-setCellShape");
    }
  }

  /**
   * Sets the grid outline preference.
   *
   * @param wantsGridOutline true if the grid outline should be displayed, false otherwise
   * @throws NoSuchElementException if the preference cannot be updated due to an I/O error
   */
  public void setGridOutlinePreference(boolean wantsGridOutline) {
    try {
      Properties simulationStyle = new Properties();
      File file = new File("SimulationStyle.properties");
      if (file.exists()) {
        try (InputStream input = new FileInputStream(file)) {
          simulationStyle.load(input);
        }
      }
      simulationStyle.setProperty(gridOutlineProperty, String.valueOf(wantsGridOutline));
      try (OutputStream output = new FileOutputStream(file)) {
        simulationStyle.store(output, "User-defined cell colors");
      }
    } catch (IOException e) {
      throw new NoSuchElementException("error-setGridOutlinePreference");
    }
  }

  /**
   * Retrieves a list of possible cell shapes defined in the simulation style properties.
   *
   * @return a list of possible cell shape values
   * @throws NoSuchElementException if the properties file cannot be read
   */
  public List<String> getPossibleCellShapes() {
    List<String> cellShapes = new ArrayList<>();
    try (InputStream input = new FileInputStream("SimulationStyle.properties")) {
      Properties simulationStyle = new Properties();
      simulationStyle.load(input);
      for (String key : simulationStyle.stringPropertyNames()) {
        if (key.startsWith("CELLSHAPE.") && !key.equals("CELLSHAPE.PREFERENCE")) {
          String value = simulationStyle.getProperty(key);
          if (value != null && !value.trim().isEmpty()) {
            cellShapes.add(value.trim());
          }
        }
      }
    } catch (IOException e) {
      throw new NoSuchElementException("error-getPossibleCellShapes");
    }
    return cellShapes;
  }

}

