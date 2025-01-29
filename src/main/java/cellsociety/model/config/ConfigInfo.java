package cellsociety.model.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Billy McCune
 * Purpose:
 * Assumptions:
 * Dependecies (classes or packages):
 * How to Use:
 * Any Other Details:
 */
public class ConfigInfo {
  private String myTitle;
  private String myAuthor;
  private String myDescription;
  private int myGridWidth;
  private int myGridHeight;
  private int myTickSpeed;
  private List<List<Integer>> myGrid;

  public enum SimulationType {

  }


  //Sets the current config info for the simulation
  public static void setConfig(ArrayList<Object> config) {
      return;
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
    return new ArrayList<Object>();
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

  //Updates the grid
  public void updateGrid(List<List<Integer>> cells){
    GridPane grid = new GridPane();
  }

}