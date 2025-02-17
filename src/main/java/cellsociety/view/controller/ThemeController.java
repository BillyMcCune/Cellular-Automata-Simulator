package cellsociety.view.controller;

import cellsociety.view.scene.SceneUIWidget;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ThemeController {

  // CSS style path
  private static final String STYLE_PATH = "/cellsociety/style/";

  /**
   * The theme of the simulation.
   */
  public enum Theme {
    DAY,
    DARK,
    MYSTERY
  }

  /**
   * The Theme UI components that can be themed.
   */
  public enum UIComponent {
    DOCKING,
    SCENE,
    WIDGET;

    private final Map<Theme, String> themeSheetFilePaths = new HashMap<>();

    UIComponent() {
      String fileName = this.name().toLowerCase() + ".css";
      for (Theme theme : Theme.values()) {
        themeSheetFilePaths.put(theme, STYLE_PATH + theme.name().toLowerCase() + "/" + fileName);
      }
    }
  }

  /**
   * Get the theme sheet for the given theme and component.
   * @param theme the theme
   * @param component the component
   * @return the theme sheet
   */
  public static String getThemeSheet(Theme theme, UIComponent component) {
    try {
      return Objects.requireNonNull(component.themeSheetFilePaths.get(theme));
    } catch (NullPointerException e) {
      SceneUIWidget.createErrorDialog("Theme Error", "The theme " + theme + " was not found.", e);
    }

    return null;
  }
}
