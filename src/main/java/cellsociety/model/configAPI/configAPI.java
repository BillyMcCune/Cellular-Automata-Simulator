package cellsociety.model.configAPI;

import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigReader;
import cellsociety.model.config.ConfigWriter;
import cellsociety.model.config.ParameterRecord;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

public class configAPI {

  private ConfigReader configReader;
  private ConfigWriter configWriter;
  private ConfigInfo configInfo;
  private ParameterRecord parameterRecord;
  private boolean isLoaded;
  /**
   * Retrieves a list of available configuration file names.
   *
   * @return a List of configuration file names.
   */
  public List<String> getFileNames() {
    ConfigReader configReader = new ConfigReader();
    return configReader.getFileNames();
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
      throws ParserConfigurationException, IOException, SAXException {
    try {
      configInfo = configReader.readConfig(fileName);
      if (configInfo != null) {
        isLoaded = true;
        parameterRecord = configInfo.myParameters();
      }
    } catch (ParserConfigurationException e) {
      throw new ParserConfigurationException(e.getMessage());
    } catch (SAXException e) {
      throw new SAXException(e.getMessage());
    } catch (IOException e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Saves the current simulation configuration to the specified file path
   *
   * @param FilePath the file path where the configuration should be saved
   * @return the file name of the simulation that was saved
   * @throws ParserConfigurationException if a parser configuration error occurs
   * @throws IOException                  if an I/O error occurs
   * @throws TransformerException         if an error occurs during transformation
   */
  public String saveSimulation(String FilePath)
      throws ParserConfigurationException, IOException, TransformerException {
    ConfigWriter configWriter = new ConfigWriter();
    configWriter.saveCurrentConfig(configInfo, FilePath);
    return configWriter.getLastFileSaved();
  }


  /**
   * Retrieves the accepted simulation states
   *
   * @return a of accepted state integers
   * @throws NullPointerException if the configuration information is not loaded
   */
  public Set<Integer> getAcceptedStates() {
    try {
      return configInfo.acceptedStates();
    } catch (NullPointerException e) {
      throw new NullPointerException("error-configInfo-NULL");
    }
  }

  /**
   * Retrieves the grid width from the configuration
   *
   * @return the grid width
   * @throws NumberFormatException if the configuration information is not loaded
   */
  public int getGridWidth() {
    try {
      return configInfo.myGridWidth();
    } catch (NullPointerException e) {
      throw new NumberFormatException("error-configInfo-NULL");
    }
  }

  /**
   * Retrieves the grid height from the configuration
   *
   * @return the grid height
   * @throws NumberFormatException if the configuration information is not loaded
   */
  public int getGridHeight() {
    try {
      return configInfo.myGridHeight();
    } catch (NullPointerException e) {
      throw new NumberFormatException("error-configInfo-NULL");
    }
  }

  public Map<String, String> getSimulationInformation() {
    try {
      HashMap<String, String> simulationDetails = new HashMap<>();
      simulationDetails.put("author", configInfo.myAuthor());
      simulationDetails.put("title", configInfo.myTitle());
      simulationDetails.put("type", configInfo.myType().toString());
      simulationDetails.put("description", configInfo.myDescription());
      return simulationDetails;
    }
    catch (NullPointerException e) {
      throw new NullPointerException("error-configInfo-NULL");
    }
  }



  public Map<String, String> getSimulationStyles() {
    return new HashMap<>();
  }

  public void setSimulationStyles(Map<String, String> simulationStyles) {

  }


  public double getConfigSpeed() {
    try {
      return configInfo.myTickSpeed();
    } catch (NullPointerException e) {
      throw new NumberFormatException("error-configInfo-NULL");
    }
  }

}
