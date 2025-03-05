package cellsociety.model.configAPI;

import cellsociety.model.config.CellRecord;
import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigReader;
import cellsociety.model.config.ConfigWriter;
import cellsociety.model.config.ParameterRecord;
import cellsociety.model.modelAPI.ModelApi;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * @author Billy McCune
 */
public class configAPI {

  private ConfigReader configReader;
  private ConfigWriter configWriter;
  private ConfigInfo configInfo;
  private ParameterRecord parameterRecord;
  private boolean isLoaded;
  private ModelApi myModelApi;
  private List<List<Integer>> myGridStates;
  private List<List<Map<String, Double>>> myGridProperties;


  public configAPI() {
  }

  /**
   * Retrieves a list of available configuration file names.
   *
   * @return a List of configuration file names.
   */
  public List<String> getFileNames() {
    configReader = new ConfigReader();
    return configReader.getFileNames();
  }

  /**
   * Sets the modelAPI instance used by this API.
   *
   * @param modelAPI the modelAPI instance to set.
   */
  public void setModelAPI(ModelApi modelAPI) {
    myModelApi = modelAPI;
  }

  /**
   * Loads a simulation configuration from the specified file.
   *
   * @param fileName the name of the configuration file to load
   * @throws ParserConfigurationException if a parser configuration error occurs
   * @throws IOException                  if an I/O error occurs
   * @throws SAXException                 if a SAX parsing error occurs - check configReader for
   *                                      more information regarding load simulation errors
   */
  public void loadSimulation(String fileName)
      throws ParserConfigurationException, IOException, SAXException, IllegalArgumentException {
    try {
      if (configReader == null) {
        configReader = new ConfigReader();
      }
      configInfo = configReader.readConfig(fileName);
      if (configInfo != null) {
        isLoaded = true;
        myModelApi.setConfigInfo(configInfo);
      }
    } catch (ParserConfigurationException e) {
      throw new ParserConfigurationException(e.getMessage());
    } catch (SAXException e) {
      throw new SAXException(e.getMessage());
    } catch (IOException e) {
      throw new IOException(e.getMessage());
    } catch (NullPointerException e) {
      throw new NullPointerException(e.getMessage());
    }
  }

  /**
   * Saves the current simulation configuration to the specified file path.
   *
   * <p>
   * This method gathers grid states and properties from the modelAPI, constructs a new ConfigInfo,
   * and then uses ConfigWriter to save the configuration.
   *
   * @param FilePath the file path where the configuration should be saved.
   * @return the file name of the simulation that was saved.
   * @throws ParserConfigurationException if a parser configuration error occurs.
   * @throws IOException                  if an I/O error occurs.
   * @throws TransformerException         if an error occurs during XML transformation.
   */
  public String saveSimulation(String FilePath)
      throws ParserConfigurationException, IOException, TransformerException {
    if (configWriter == null) {
      configWriter = new ConfigWriter();
    }
    //Save the grid dataGrid<?>
    myGridStates = myModelApi.getCellStates();
    myGridProperties = myModelApi.getCellProperties();
    List<List<CellRecord>> gridData = new ArrayList<>();
    for (int i = 0; i < myGridStates.size(); i++) {
      List<CellRecord> row = new ArrayList<>();
      for (int j = 0; j < myGridStates.get(i).size(); j++) {
        row.add(new CellRecord(myGridStates.get(i).get(j), myGridProperties.get(i).get(j)));
      }
      gridData.add(row);
    }

    Map<String, Double> doubleParams = myModelApi.getDoubleParameters();
    Map<String, String> stringParams = myModelApi.getStringParameters();

    ParameterRecord parameters = new ParameterRecord(doubleParams, stringParams);

    // TODO: Make user input for title, author, description
    ConfigInfo savedConfigInfo = new ConfigInfo(
        configInfo.myType(),
        configInfo.myCellShapeType(),
        configInfo.myGridEdgeType(),
        configInfo.myneighborArrangementType(),
        configInfo.neighborRadius(),
        configInfo.myTitle(),
        configInfo.myAuthor(),
        configInfo.myDescription(),
        gridData.getFirst().size(),
        gridData.size(),
        configInfo.myTickSpeed(),
        gridData,
        parameters,
        configInfo.acceptedStates(),
        configInfo.myFileName()
    );
    configWriter.saveCurrentConfig(savedConfigInfo, FilePath);
    return configWriter.getLastFileSaved();
  }


  /**
   * Retrieves the accepted simulation states.
   *
   * @return a set of accepted state integers.
   * @throws NullPointerException if the configuration information is not loaded.
   */
  public Set<Integer> getAcceptedStates() {
    try {
      return configInfo.acceptedStates();
    } catch (NullPointerException e) {
      throw new NullPointerException("error-configInfo-NULL");
    }
  }

  /**
   * Retrieves the grid width from the configuration.
   *
   * @return the grid width.
   * @throws NumberFormatException if the configuration information is not loaded.
   */
  public int getGridWidth() throws NullPointerException {
    try {
      return configInfo.myGridWidth();
    } catch (NullPointerException e) {
      throw new NumberFormatException("error-configInfo-NULL");
    }
  }

  /**
   * Retrieves the grid height from the configuration.
   *
   * @return the grid height
   * @throws NumberFormatException if the configuration information is not loaded
   */
  public int getGridHeight() throws NullPointerException{
    try {
      return configInfo.myGridHeight();
    } catch (NullPointerException e) {
      throw new NumberFormatException("error-configInfo-NULL");
    }
  }

  /**
   * Retrieves simulation information such as author, title, type, and description.
   *
   * @return a map containing simulation details.
   * @throws NullPointerException if the configuration information is not loaded.
   */
  public Map<String, String> getSimulationInformation() throws NullPointerException {
    try {
      HashMap<String, String> simulationDetails = new HashMap<>();
      simulationDetails.put("author", configInfo.myAuthor());
      simulationDetails.put("title", configInfo.myTitle());
      simulationDetails.put("type", configInfo.myType().toString());
      simulationDetails.put("description", configInfo.myDescription());
      return simulationDetails;
    } catch (NullPointerException e) {
      throw new NullPointerException("error-configInfo-NULL");
    }
  }

  /**
   * Sets simulation information using the provided details.
   *
   * @param simulationDetails a map containing simulation details such as title, author, and
   *                          description.
   * @throws NullPointerException if the current configuration information is not loaded.
   */
  public void setSimulationInformation(Map<String, String> simulationDetails) throws NullPointerException {
    try {
      ConfigInfo tempConfigInfo = new ConfigInfo(
          configInfo.myType(),
          configInfo.myCellShapeType(),
          configInfo.myGridEdgeType(),
          configInfo.myneighborArrangementType(),
          configInfo.neighborRadius(),
          simulationDetails.get("title"),
          simulationDetails.get("author"),
          simulationDetails.get("description"),
          configInfo.myGridWidth(),
          configInfo.myGridHeight(),
          configInfo.myTickSpeed(),
          configInfo.myGrid(),
          configInfo.myParameters(),
          configInfo.acceptedStates(),
          configInfo.myFileName()
      );
      configInfo = tempConfigInfo;
    } catch (NullPointerException e) {
      throw new NullPointerException("error-missing-simulation-info");
    }
  }

  /**
   * Retrieves the simulation tick speed from the configuration.
   *
   * @return the simulation tick speed.
   * @throws NumberFormatException if the configuration information is not loaded.
   */
  public double getConfigSpeed() throws NullPointerException {
    try {
      return configInfo.myTickSpeed();
    } catch (NullPointerException e) {
      throw new NumberFormatException("error-configInfo-NULL");
    }
  }

}
