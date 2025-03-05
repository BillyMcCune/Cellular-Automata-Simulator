package viewtests;

import cellsociety.view.controller.LanguageController.Language;
import cellsociety.view.controller.ThemeController.Theme;
import cellsociety.view.scene.SceneUIWidgetFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.DukeApplicationTest;

public class SceneUIWidgetFactoryTest extends DukeApplicationTest {
  private static final StringProperty DUMMY_TITLE = new SimpleStringProperty("Dummy Title");
  private static final StringProperty DUMMY_LABEL = new SimpleStringProperty("Dummy Label");
  private static final StringProperty DUMMY_TOOLTIP = new SimpleStringProperty("Dummy Tooltip");
  private static final Consumer<String> DUMMY_STRING_CONSUMER = System.out::println;
  private static final Consumer<Double> DUMMY_DOUBLE_CONSUMER = System.out::println;
  private static final Consumer<Theme> DUMMY_THEME_CONSUMER = System.out::println;
  private static final Consumer<Language> DUMMY_LANGUAGE_CONSUMER = System.out::println;
  private static final Supplier<Collection<String>> DUMMY_STRING_COLLECTION_SUPPLIER = ArrayList::new;
  private static final Rectangle DUMMY_RECTANGLE = new Rectangle();
  private static final Rectangle DUMMY_RECTANGLE_2 = new Rectangle();

  @Test
  public void createRangeUI_DoubleInput() {
    HBox rangeUI = SceneUIWidgetFactory.createRangeUI(
        5,
        0,
        10,
        DUMMY_LABEL,
        DUMMY_TOOLTIP,
        DUMMY_DOUBLE_CONSUMER
    );
  }

  @Test
  public void createRangeUI_StringInput() {
    HBox rangeUI = SceneUIWidgetFactory.createRangeUI(
        "TEST",
        DUMMY_LABEL,
        DUMMY_TOOLTIP,
        DUMMY_STRING_CONSUMER
    );
  }

  @Test
  public void createColorSelectorUI_CreateBasicWidget() {
    HBox colorSelectorUI = SceneUIWidgetFactory.createColorSelectorUI(
        "RED",
        DUMMY_LABEL,
        DUMMY_TOOLTIP,
        DUMMY_STRING_CONSUMER
    );
  }

  @Test
  public void dragZoomViewUI_CreateBasicWidget() {
    Pane dragZoomViewUI = SceneUIWidgetFactory.dragZoomViewUI(
        DUMMY_RECTANGLE,
        DUMMY_RECTANGLE_2
    );
  }

  @Test
  public void createContainerUI_CreateBasicWidget() {
    ScrollPane containerUI = SceneUIWidgetFactory.createContainerUI(
        DUMMY_RECTANGLE,
        DUMMY_TITLE
    );
  }

  @Test
  public void createSectionUI_CreateBasicWidget() {
    BorderPane sectionUI = SceneUIWidgetFactory.createSectionUI(
        DUMMY_TITLE,
        DUMMY_RECTANGLE,
        DUMMY_RECTANGLE_2
    );
  }

  @Test
  public void createButtonUI_CreateBasicWidget() {
    Button buttonUI = SceneUIWidgetFactory.createButtonUI(
        DUMMY_LABEL,
        e -> System.out.println("Button clicked")
    );
  }

  @Test
  public void createDropDownUI_CreateBasicWidget() {
    HBox dropDownUI = SceneUIWidgetFactory.createDropDownUI(
        DUMMY_LABEL,
        DUMMY_STRING_COLLECTION_SUPPLIER,
        DUMMY_STRING_CONSUMER
    );
  }

  @Test
  public void createThemeLanguageSelectorUI_CreateBasicWidget() {
    HBox themeLanguageSelectorUI = SceneUIWidgetFactory.createThemeLanguageSelectorUI(
        DUMMY_LABEL,
        DUMMY_TITLE,
        DUMMY_LANGUAGE_CONSUMER,
        DUMMY_THEME_CONSUMER
    );
  }

  @Test
  public void setWidgetStyleSheet_SetSheet() {
    SceneUIWidgetFactory.setWidgetStyleSheet("test.css");
  }
}
