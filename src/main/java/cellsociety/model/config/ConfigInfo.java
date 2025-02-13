package cellsociety.model.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.scene.layout.GridPane;

/**
 * @author Billy McCune Purpose: Assumptions: Dependecies (classes or packages): How to Use: Any
 * Other Details:
 */
public class ConfigInfo {

  private SimulationType myType;
  private String myTitle;
  private String myAuthor;
  private String myDescription;
  private int myGridWidth;
  private int myGridHeight;
  private int myTickSpeed;
  private List<List<Integer>> myGrid;
  private static ConfigInfo instance;
  private Map<String, Double> myParameters;
  private String myFileName;

  public enum SimulationType {
    LIFE, PERCOLATION, FIRE, SEGREGATION, WATOR
  }

  private ConfigInfo() {
  }

  public static ConfigInfo createInstance() {
    if (instance == null) {
      instance = new ConfigInfo();
    }
    return instance;
  }


  /**
   * Sets the config data from a generic Object list. Expects the ArrayList<> order: 0: String of
   * the simulation type (e.g., "GAMEOFLIFE") 1: String of the title 2: String of the author 3:
   * String of the description 4: Integer for grid width 5: Integer for grid height 6: Integer for
   * tick speed 7: List<List<Integer>> for the initial grid 8: Map<String,Double> for parameters
   */
  public void setConfig(ArrayList<Object> config) {
    setMyType((String) config.get(0));
    setMyTitle((String) config.get(1));
    setMyAuthor((String) config.get(2));
    setMyDescription((String) config.get(3));
    setMyGridWidth((int) config.get(4));
    setMyGridHeight((int) config.get(5));
    setTickSpeed((int) config.get(6));
    setMyGrid((List<List<Integer>>) config.get(7));
    setMyParameters((Map<String, Double>) config.get(8));
  }

  private List<List<Integer>> createGridFromConfig(List<List<Integer>> inputGrid) {
    List<List<Integer>> newGrid = new ArrayList<>();
    for (List<Integer> row : inputGrid) {
      // create a copy of each row
      newGrid.add(new ArrayList<>(row));
    }
    return newGrid;
  }

  // Returns the simulation type
  public SimulationType getType() {
    return myType;
  }

  // Returns the author name
  public String getAuthor() {
    return myAuthor;
  }

  // Returns the title
  public String getTitle() {
    return myTitle;
  }

  // Returns the description
  public String getDescription() {
    return myDescription;
  }

  public void setMyFileName(String fileName) {
    myFileName = fileName;
  }


  //TODO create a list of strings to pass in - try to reduce the number of getters and setters
  // Returns the parameters passed in
  public ArrayList<Object> getAllConfigInfo() {
    ArrayList<Object> configInfo = new ArrayList<>();
    configInfo.add(myType.toString());
    configInfo.add(myTitle);
    configInfo.add(myAuthor);
    configInfo.add(myDescription);
    configInfo.add(myGridWidth);
    configInfo.add(myGridHeight);
    configInfo.add(myTickSpeed);
    configInfo.add(myGrid);
    configInfo.add(myParameters);
    return configInfo;
  }

  //Returns the width
  public int getWidth() {
    return myGridWidth;
  }

  //Returns the height
  public int getHeight() {
    return myGridHeight;
  }

  //Returns the speed
  public int getSpeed() {
    return myTickSpeed;
  }

  //Returns the grid
  public List<List<Integer>> getGrid() {
    return myGrid;
  }

  public Map<String, Double> getParameters() {
    return myParameters;
  }

  public void setMyType(String type) {
    myType = SimulationType.valueOf(type);
  }

  public void setMyTitle(String title) {
    myTitle = title;
  }

  public void setMyAuthor(String author) {
    myAuthor = author;
  }

  public void setMyDescription(String description) {
    myDescription = description;
  }

  public void setMyGridWidth(int width) {
    myGridWidth = width;
  }

  public void setMyGridHeight(int height) {
    myGridHeight = height;
  }

  public void setTickSpeed(int tickSpeed) {
    myTickSpeed = tickSpeed;
  }

  public void setMyGrid(List<List<Integer>> grid) {
    myGrid = grid;
  }

  public void setMyParameters(Map<String, Double> parameters) {
    this.myParameters = parameters;
  }

  // Updates the grid
  // TODO Finish this
  // NOTE: I think this is unnecessary, the grid control should be handled by the scene controller
  //   BY: Hsuan-Kai Liao
  // I think the update grid is just for saving the grid
  public void saveGrid(List<List<Integer>> cells) {
    GridPane grid = new GridPane();
  }

  public boolean isValid() {
    // Check if the simulation type is not set
    if (myType == null) {
      return false;
    }
    // Check if the title, author, or description are null or empty strings
    if (myTitle == null || myTitle.trim().isEmpty()) {
      return false;
    }
    if (myAuthor == null || myAuthor.trim().isEmpty()) {
      return false;
    }
    if (myDescription == null || myDescription.trim().isEmpty()) {
      return false;
    }
    // Check if grid dimensions are invalid (zero or negative)
    if (myGridWidth <= 0 || myGridHeight <= 0) {
      return false;
    }
    // Check if tick speed is invalid (zero or negative)
    if (myTickSpeed <= 0) {
      return false;
    }
    // Check if the grid is null or contains no rows
    if (myGrid == null || myGrid.isEmpty()) {
      return false;
    }
    return true;
  }


}