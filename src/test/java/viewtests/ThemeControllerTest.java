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
  public void testGetThemeSheetDay() {
    ThemeController.Theme theme = ThemeController.Theme.DAY;
    ThemeController.UIComponent component = ThemeController.UIComponent.SCENE;
    String themeSheet = ThemeController.getThemeSheet(theme, component);
    Assertions.assertNotNull(themeSheet);
  }

  @Test
  public void testGetThemeSheetDark() {
    ThemeController.Theme theme = ThemeController.Theme.DARK;
    ThemeController.UIComponent component = ThemeController.UIComponent.WIDGET;
    String themeSheet = ThemeController.getThemeSheet(theme, component);
    Assertions.assertNotNull(themeSheet);
  }

  @Test
  public void testGetThemeSheetMystery() {
    ThemeController.Theme theme = ThemeController.Theme.MYSTERY;
    ThemeController.UIComponent component = ThemeController.UIComponent.DOCKING;
    String themeSheet = ThemeController.getThemeSheet(theme, component);
    Assertions.assertNotNull(themeSheet);
  }

}
