package cellsociety.view.controller;

import cellsociety.view.scene.SceneUIWidget;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.InputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.HashMap;

/**
 * A controller class that manages the language translations for the program.
 *
 * @author Hsuan-Kai Liao
 */
public class LanguageController {

  // Language properties file path
  private static final String LANGUAGE_PATH = "cellsociety/lang/";

  // The string property translations for each language
  private static final Map<String, StringProperty> translations = new HashMap<>();

  static {
    // Load the default language
    switchLanguage(Language.ENGLISH);
  }

  /**
   * The supported languages.
   */
  public enum Language {
    ENGLISH,
    FRENCH,
    MANDARIN;

    private final Map<String, String> translation = new HashMap<>();

    Language() {
      String languageFile = LANGUAGE_PATH + name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase() + ".properties";
      try (InputStream input = LanguageController.class.getClassLoader().getResourceAsStream(languageFile)) {
        if (input == null) {
          throw new IOException("Language file not found: " + languageFile);
        }

        Properties properties = new Properties();
        properties.load(input);
        for (String key : properties.stringPropertyNames()) {
          translation.put(key, properties.getProperty(key));

          if (!translations.containsKey(key)) {
            translations.put(key, new SimpleStringProperty("??"));
          }
        }

      } catch (IOException ex) {
        SceneUIWidget.createErrorDialog("Error loading language file", ex.getMessage(), ex);
      }
    }
  }

  /* API BELOW */

  /**
   * Get the string property for the given key.
   * @param key the key
   * @return the string property
   */
  public static StringProperty getStringProperty(String key) {
    return translations.getOrDefault(key, null);
  }

  /**
   * Update all the string property to the translation of the given language.
   * @param lang the language
   */
  public static void switchLanguage(Language lang) {
    for (Map.Entry<String, StringProperty> entry : translations.entrySet()) {
      entry.getValue().setValue(lang.translation.get(entry.getKey()));
    }
  }
}
