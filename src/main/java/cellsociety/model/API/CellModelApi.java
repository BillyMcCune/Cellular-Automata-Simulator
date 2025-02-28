package cellsociety.model.API;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author Billy McCune
 */
public class CellModelApi {


  public List<String> getFileNames(){
    return new ArrayList<>();
  }

  public void loadSimulation(String fileName){

  }

  public void updateSimulation() {

  }

  public String saveSimulation(String fileName,String FilePath) {
    return "not implemented";
  }

  public void resetGrid(){

  }

  public Map<String,String> getSimulationStyles(){
    return new HashMap<>();
  }

  public void setSimulationStyles(Map<String,String> simulationStyles){

  }

  public Map<String,String> getSimulationDetails() {
    return new HashMap<>();
  }

  public Map<String,String> getParameters() {
    return new HashMap<>();
  }

  public int setParameter(String name, Double value){
    return -1;
  }

  public int getGridWidth() {
    return 0;
  }

  public int getGridHeight() {
    return 0;
  }

  public String getCellColor(int row, int col) {
    return null;
  }

  public String getCellShape() {
    return null;
  }

  public void setCellShape(String cellShape) {

  }











}
