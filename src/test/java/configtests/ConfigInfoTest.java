package configtests;

import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class ConfigInfoTest {
  String GameOfLifeGliderTitle = "GameOfLifeGlider";
  ConfigReader configReader = new ConfigReader();
  ConfigInfo configInfo = ConfigInfo.createInstance();

  @Test
  void getTitle_ConfigIsGameOfLifeGlider_isCorrect() {
    assertEquals(GameOfLifeGliderTitle, ConfigInfo.getTitle());
  }


}
