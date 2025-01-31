package cellsociety.model.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.scene.layout.GridPane;
import javax.lang.model.type.NullType;

/**
 * @author Billy McCune
 * Purpose:
 * Assumptions:
 * Dependecies (classes or packages):
 * How to Use:
 * Any Other Details:
 */
public class ConfigInfo {
  private SimulationType myType;
  private String myTitle;
  private String myAuthor;
  private String myDescription;
  private int myGridWidth;
  private int myGridHeight;
  private int myTickSpeed;
  private List<String> myParameters;
  private List<List<Integer>> myGrid;
  private static ConfigInfo instance;

  public enum SimulationType {
    GAMEOFLIFE, GAMEOFLIFEGLIDER, PERCOLATION, SPREADINGOFFIRE
  }

  private ConfigInfo(){

  }

  public static ConfigInfo createInstance(){
    if (instance == null){
      instance = new ConfigInfo();
    }
    return instance;
  }


  /**
   * Sets the config data from a generic Object list.
   * Expects the ArrayList<Object> order:
   *   0: String of the simulation type (e.g., "GAMEOFLIFE")
   *   1: String of the title
   *   2: String of the author
   *   3: String of the description
   *   4: Integer for grid width
   *   5: Integer for grid height
   *   6: Integer for tick speed
   *   7: List<List<Integer>> for the initial grid
   */
  public void setConfig(ArrayList<Object> config) {
      myType = SimulationType.valueOf((String)config.get(0));
      myTitle = (String)config.get(1);
      myAuthor = (String)config.get(2);
      myDescription = (String)config.get(3);
      myGridWidth = (int)config.get(4);
      myGridHeight = (int)config.get(5);
      myTickSpeed = (int)config.get(6);
      myGrid = createGridFromConfig((List<List<Integer>>) config.get(7));
  }

  private List<List<Integer>> createGridFromConfig(List<List<Integer>> inputGrid) {
    List<List<Integer>> newGrid = new ArrayList<>();
    for (List<Integer> row : inputGrid) {
      // create a copy of each row
      newGrid.add(new ArrayList<>(row));
    }
    return newGrid;
  }

  //Returns the simulation type
  public SimulationType getType(){
      return SimulationType.values()[myGrid.size()];
  }

  // Returns the author name
  public String getAuthor(){
    return myAuthor;
  }

  //Returns the title
  public String getTitle(){
    return myTitle;
  }

  //Returns the description
  public String getDescription(){
    return myDescription;
  }

  //Returns the parameters passed in
  public ArrayList<Object> getParameters(){
    ArrayList<Object> params = new ArrayList<>();
    params.add(myType.toString());
    params.add(myTitle);
    params.add(myAuthor);
    params.add(myDescription);
    params.add(myGridWidth);
    params.add(myGridHeight);
    params.add(myTickSpeed);
    params.add(myGrid);
    return params;
  }

  //Returns the width
  public int getWidth(){
    return myGridWidth;
  }

  //Returns the height
  public int getHeight(){
    return myGridHeight;
  }

  //Returns the speed
  public int getSpeed(){
    return myTickSpeed;
  }

  //Returns the grid
  public int getGrid(){
    return myGrid.size();
  }

  public void setMyTitle(String title){
    myTitle = title;
  }
  public void setMyAuthor(String author){
    myAuthor = author;
  }
  public void setMyDescription(String description){
    myDescription = description;
  }
  public void setMyGridWidth(int width){
    myGridWidth = width;
  }
  public void setMyGridHeight(int height){
    myGridHeight = height;
  }
  public void setTickSpeed(int tickSpeed){
    myTickSpeed = tickSpeed;
  }

  public void setMyGrid(List<List<Integer>> grid){
    myGrid = grid;
  }

  //Updates the grid
  //TODO Finish this
  public void updateGrid(List<List<Integer>> cells){
    GridPane grid = new GridPane();
  }

  public boolean isValid(){
    // Check if the simulation type is not set
    if(myType == null) {
      return false;
    }
    // Check if the title, author, or description are null or empty strings
    if(myTitle == null || myTitle.trim().isEmpty()) {
      return false;
    }
    if(myAuthor == null || myAuthor.trim().isEmpty()) {
      return false;
    }
    if(myDescription == null || myDescription.trim().isEmpty()) {
      return false;
    }
    // Check if grid dimensions are invalid (zero or negative)
    if(myGridWidth <= 0 || myGridHeight <= 0) {
      return false;
    }
    // Check if tick speed is invalid (zero or negative)
    if(myTickSpeed <= 0) {
      return false;
    }
    // Check if the grid is null or contains no rows
    if(myGrid == null || myGrid.isEmpty()) {
      return false;
    }
    return true;
  }


}