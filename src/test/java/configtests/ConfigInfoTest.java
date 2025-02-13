package configtests;

import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xml.sax.SAXException;

import static org.junit.jupiter.api.Assertions.*;


public class ConfigInfoTest {
  String GameOfLifeGliderTitle = "GameOfLifeGlider";
  ConfigReader configReader = new ConfigReader();
  ConfigInfo configInfo = ConfigInfo.createInstance();
  List<String> files = configReader.getFileNames();
  Map<String,Double> Emptymap = new HashMap<>();


  @Test
  void getTitle_ConfigIsGameOfLifeGlider_isCorrect()
      throws ParserConfigurationException, IOException, SAXException {
    configReader.readConfig(files.getFirst());
    assertNotNull(configInfo.getTitle());
  }

  @Test
  void getParameters_ConfigIsGameOfLifeGlider_isCorrect()
      throws ParserConfigurationException, IOException, SAXException {
    configReader.readConfig(files.getFirst());
    assertEquals(Emptymap, configInfo.getParameters());
    System.out.println(configInfo.getParameters());
  }


}
