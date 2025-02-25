package viewtests;

import cellsociety.view.controller.ThemeController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link cellsociety.view.controller.ThemeController} class.
 *
 * @author Hsuan-Kai Liao
 */
public class ThemeControllerTest {

  @Test
  public void GetThemeSheetDay_ThemeAndComponent_ThemeSheetIsNotNull() {
    ThemeController.Theme theme = ThemeController.Theme.DAY;
    ThemeController.UIComponent component = ThemeController.UIComponent.SCENE;
    String themeSheet = ThemeController.getThemeSheet(theme, component);
    Assertions.assertNotNull(themeSheet);
  }

  @Test
  public void GetThemeSheetDark_ThemeAndComponent_ThemeSheetIsNotNull() {
    ThemeController.Theme theme = ThemeController.Theme.DARK;
    ThemeController.UIComponent component = ThemeController.UIComponent.WIDGET;
    String themeSheet = ThemeController.getThemeSheet(theme, component);
    Assertions.assertNotNull(themeSheet);
  }

  @Test
  public void GetThemeSheetMystery_ThemeAndComponent_ThemeSheetIsNotNull() {
    ThemeController.Theme theme = ThemeController.Theme.MYSTERY;
    ThemeController.UIComponent component = ThemeController.UIComponent.DOCKING;
    String themeSheet = ThemeController.getThemeSheet(theme, component);
    Assertions.assertNotNull(themeSheet);
  }

}
