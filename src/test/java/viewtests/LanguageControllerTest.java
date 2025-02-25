package viewtests;

import cellsociety.view.controller.LanguageController;
import cellsociety.view.controller.LanguageController.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link cellsociety.view.controller.LanguageController} class.
 *
 * @author Hsuan-Kai Liao
 */
public class LanguageControllerTest {

  @BeforeEach
  public void SetUp() {
    // Load the default language to initialize all the loads for all languages
    LanguageController.switchLanguage(Language.ENGLISH);
  }

  @Test
  public void GetStringProperty_StateIsGiven_ExpectedValueIsReturned() {
    Assertions.assertEquals("Welcome to Cell Society!", LanguageController.getStringProperty("welcome-title").getValue());
  }

  @Test
  public void SwitchLanguage_StateIsGiven_ExpectedLanguageIsSwitched() {
    LanguageController.switchLanguage(Language.FRENCH);
    System.out.println(LanguageController.getStringProperty("welcome-title").getValue());
    Assertions.assertEquals("Bienvenue dans Cell Society !", LanguageController.getStringProperty("welcome-title").getValue());
  }
}
