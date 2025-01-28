package cellsociety.model.config;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
public class ConfigInfo extends Application {
public enum SimulationType {}


  //Sets the current config info for the simulation
  public static void setConfig(ArrayList<>){

  }

  //Returns the simulation type
  public SimulationType getType(){

  }

  // Returns the author name
  public String getAuthor(){

  }

  //Returns the title
  public String getTitle(){

  }

  //Returns the description
  public String getDescription(){

  }

  //Returns the parameters passed in
  public ArrayList<> getParameters(){

  }

  //Returns the width
  public int getWidth(){

  }

  //Returns the height
  public int getHeight(){

  }

  //Returns the speed
  public getSpeed(){

  }

  //Returns the grid
  public getGrid(){

  }

  //Updates the grid
  public updateGrid(ArrayList cells){

  }

}