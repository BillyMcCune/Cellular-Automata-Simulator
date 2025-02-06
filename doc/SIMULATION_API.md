# Cell Society API Lab Discussion
### NAMES: Billy McCune wrm29, Jacob You jay27, Hsuan-Kai Liao hl475
### TEAM 1


## Current Simulation Model API

* Identified Public Classes/Methods
  Config:
  ConfigReader
  public ConfigInfo readConfig(String fileName)
  public ConfigInfo getConfigInformation(File xmlFile)
  public List<String> getFileNames()
  public void createListOfConfigFiles()
  ConfigWriter
  public void saveCurrentConfig() throws ParserConfigurationException
  public void saveCurrentConfig(String Path) throws ParserConfigurationException
  public void setConfigInfo(ConfigInfo configInfo)
  ConfigInfo
  public static ConfigInfo createInstance()
  public void setConfig(ArrayList<Object> config)
  all get methods and set methods for the parameters

Model:
View:
SimulationScene:
public void setGrid(int numOfRows, int numOfCols)
public void setCell(int row, int col, Enum<?> state)
public void setParameter(String label, double min, double max, double defaultValue, String toolTip, Consumer<double> callback)
SimulationRenderer:
public static void drawGrid(GridPane grid, int numOfRows, int numOfCols)
public static void drawCell(GridPane grid, int row, int col, Enum<?> state)
SceneController:
*This is the class that act as the “bridges” for the view, config, and model classes, it encapsulates the APIs from other parts of the codes.



## Wish Simulation Model API

* Goals

* Contract
    * Services

    * Abstractions and their Methods

    * Exceptions





## API Task Description

* English

* Java
 
