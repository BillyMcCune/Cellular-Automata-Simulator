package viewtests;

import cellsociety.view.controller.LanguageController.Language;
import cellsociety.view.controller.ThemeController.Theme;
import cellsociety.view.scene.SceneUIWidgetFactory;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import util.DukeApplicationTest;

/**
 * Tests for the SceneUIWidgetFactory class. Tests the creation of various UI widgets and their
 * functionality.
 *
 * @author Hsuan-Kai Liao
 */
public class SceneUIWidgetFactoryTest extends DukeApplicationTest {
  /* DUMMY DATA BELOW */

  private static final StringProperty DUMMY_TITLE = new SimpleStringProperty("Dummy Title");
  private static final StringProperty DUMMY_LABEL = new SimpleStringProperty("Dummy Label");
  private static final StringProperty DUMMY_TOOLTIP = new SimpleStringProperty("Dummy Tooltip");
  private static final Consumer<String> DUMMY_STRING_CONSUMER = System.out::println;
  private static final Consumer<Double> DUMMY_DOUBLE_CONSUMER = System.out::println;
  private static final Consumer<Theme> DUMMY_THEME_CONSUMER = System.out::println;
  private static final Consumer<Language> DUMMY_LANGUAGE_CONSUMER = System.out::println;
  private static final Supplier<Collection<String>> DUMMY_STRING_COLLECTION_SUPPLIER = () -> {;
    Collection<String> dummyCollection = new ArrayList<>();
    dummyCollection.add("Option 1");
    dummyCollection.add("Option 2");
    dummyCollection.add("Option 3");
    return dummyCollection;
  };
  private static final Rectangle DUMMY_RECTANGLE = new Rectangle(200, 200, Color.BLACK);
  private static final Rectangle DUMMY_RECTANGLE_2 = new Rectangle(50, 50, Color.RED);

  /* STYLESHEET PATHS BELOW */

  private static final String STYLE_PATH = "/cellsociety/style/";
  private static final String WIDGET_STYLE_SHEET_DAY = STYLE_PATH + "/day/widget.css";
  private static final String WIDGET_STYLE_SHEET_DARK = STYLE_PATH + "/dark/widget.css";
  private static final String WIDGET_STYLE_SHEET_MYSTERY = STYLE_PATH + "/mystery/widget.css";

  /* SPLASH SCREEN SETUP BELOW */

  private CountDownLatch latch;

  private Button createBasicSplashScreen(Node content, String testWidgetName) {
    latch = new CountDownLatch(1);

    Button finishButton = new Button("Finish");
    finishButton.setOnAction(e -> {
      latch.countDown();
      latch = null;
    });

    runAsJFXAction(() -> {
      Stage splashStage = new Stage();
      splashStage.setTitle("SPLASH SCREEN UI TEST");
      splashStage.initModality(Modality.APPLICATION_MODAL);

      Label splashLabel = new Label(testWidgetName);
      splashLabel.setStyle("-fx-font-size: 24px;");

      StackPane centeredContent = new StackPane(content);
      centeredContent.setAlignment(Pos.CENTER);
      centeredContent.setStyle("-fx-background-color: grey; -fx-padding: 10; -fx-border-color: black; -fx-border-width: 1;");
      centeredContent.setMaxHeight(350);
      VBox.setVgrow(centeredContent, Priority.NEVER);
      VBox.setVgrow(content, Priority.NEVER);

      VBox layout = new VBox(10, splashLabel, centeredContent, finishButton);
      layout.setAlignment(Pos.CENTER);
      layout.setPadding(new Insets(20));
      layout.setSpacing(20);

      Scene splashScene = new Scene(layout, 600, 450);
      setTestStyleSheet(splashScene);
      splashStage.setScene(splashScene);
      splashStage.show();
    });

    return finishButton;
  }

  private void setTestStyleSheet(Scene scene) {
    scene.getStylesheets().add(WIDGET_STYLE_SHEET_DAY);
    // scene.getStylesheets().add(WIDGET_STYLE_SHEET_DARK);
    // scene.getStylesheets().add(WIDGET_STYLE_SHEET_MYSTERY);
  }

  @AfterEach
  public void waitForLatch() {
    // Wait for the latch to be counted down
    try {
      if (latch != null) {
        latch.await();
      }
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  /* UI TESTS BELOW */

  @Test
  public void createRangeUI_DoubleInput_ValidAndInValidWrite() {
    HBox rangeUI = SceneUIWidgetFactory.createRangeUI(
        0,
        10,
        5,
        DUMMY_LABEL,
        DUMMY_TOOLTIP,
        DUMMY_DOUBLE_CONSUMER
    );
    Button finishButton = createBasicSplashScreen(rangeUI, "Double Range UI");

    // Assertions for UI initialization
    Slider slider = (Slider) rangeUI.lookup(".slider");
    Node thumb = slider.lookup(".thumb");
    TextField rangeTextField = (TextField) rangeUI.lookup(".range-text-field");
    Assertions.assertEquals("5.0", rangeTextField.getText());
    Assertions.assertEquals(5.0, slider.getValue());
    Assertions.assertNotNull(thumb);

    // Action Test
    drag(thumb).moveBy(50, 0);
    drag(thumb).moveBy(-100, 0);
    drag(thumb).drop();
    writeInputTo(rangeTextField, "7.5"); // Valid input
    press(KeyCode.ENTER);
    writeInputTo(rangeTextField, "###"); // Invalid input
    press(KeyCode.ENTER);

    // Close
    clickOn(finishButton);
  }

  @Test
  public void createRangeUI_StringInput_ValidAndInvalidWrite() {
    HBox rangeUI = SceneUIWidgetFactory.createRangeUI(
        "B3/34",
        DUMMY_LABEL,
        DUMMY_TOOLTIP,
        DUMMY_STRING_CONSUMER
    );
    Button finishButton = createBasicSplashScreen(rangeUI, "String Range UI");

    // Assertions for UI initialization
    TextField rangeTextField = (TextField) rangeUI.lookup(".range-text");
    Assertions.assertEquals("B3/34", rangeTextField.getText());

    // Action Test
    writeInputTo(rangeTextField, "ABCDEF/1234"); // Valid input
    press(KeyCode.ENTER);
    writeInputTo(rangeTextField, "NEW TEST"); // Invalid input
    press(KeyCode.ENTER);

    // Close
    clickOn(finishButton);
  }

  @Test
  public void createColorSelectorUI_CreateBasicWidget_PickColorInPickerAndTextInput() {
    HBox colorSelectorUI = SceneUIWidgetFactory.createColorSelectorUI(
        "#AABBCC",
        DUMMY_LABEL,
        DUMMY_TOOLTIP,
        DUMMY_STRING_CONSUMER
    );
    Button finishButton = createBasicSplashScreen(colorSelectorUI, "Color Selector UI");

    // Assertions for UI initialization
    TextField colorTextField = (TextField) colorSelectorUI.lookup(".color-text-field");
    Assertions.assertEquals("#AABBCC", colorTextField.getText());
    ColorPicker colorPicker = (ColorPicker) colorSelectorUI.lookup(".color-picker");
    Assertions.assertEquals("0xaabbccff", colorPicker.getValue().toString());

    // Action Test
    clickOn(colorPicker).moveBy(0, 100).clickOn(MouseButton.PRIMARY);
    writeInputTo(colorTextField, "#123456"); // Valid input
    press(KeyCode.ENTER);
    writeInputTo(colorTextField, "###"); // Invalid input
    press(KeyCode.ENTER);

    // Close
    clickOn(finishButton);
  }

  @Test
  public void dragZoomViewUI_CreateBasicWidget_ZoomAndDrag() {
    Pane dragZoomViewUI = SceneUIWidgetFactory.dragZoomViewUI(
        DUMMY_RECTANGLE,
        DUMMY_RECTANGLE_2
    );
    StackPane container = new StackPane(dragZoomViewUI);
    container.setMaxHeight(300);
    container.setMinHeight(300);
    Button finishButton = createBasicSplashScreen(container, "Drag Zoom View UI");

    // Assertions for UI initialization
    Pane miniMapPane = (Pane) dragZoomViewUI.lookup(".mini-map-pane");
    Assertions.assertNotNull(miniMapPane);

    // Action Test
    drag(dragZoomViewUI).moveBy(50, 50);
    drag(dragZoomViewUI).moveBy(-100, -100);
    drag(dragZoomViewUI).drop();
    scroll(VerticalDirection.UP);
    scroll(VerticalDirection.DOWN);

    // Close
    clickOn(finishButton);
  }

  @Test
  public void createContainerUI_CreateBasicWidget_ScrollVertically() {
    ScrollPane containerUI = SceneUIWidgetFactory.createContainerUI(
        DUMMY_RECTANGLE,
        DUMMY_TITLE
    );
    Button finishButton = createBasicSplashScreen(containerUI, "Container UI");

    // Assertions for UI initialization
    VBox containerRectangle = (VBox) containerUI.lookup(".container-container");
    Assertions.assertNotNull(containerRectangle);

    // Action Test
    moveTo(containerRectangle);
    scroll(VerticalDirection.UP);
    scroll(VerticalDirection.DOWN);

    // Close
    clickOn(finishButton);
  }

  @Test
  public void createSectionUI_CreateBasicWidget_InitializationTextCheck() {
    BorderPane sectionUI = SceneUIWidgetFactory.createSectionUI(
        DUMMY_TITLE,
        DUMMY_RECTANGLE,
        DUMMY_RECTANGLE_2
    );
    Button finishButton = createBasicSplashScreen(sectionUI, "Section UI");

    // Assertions for UI initialization
    Label sectionLabel = (Label) sectionUI.lookup(".section-label");
    Assertions.assertEquals(DUMMY_TITLE.getValue(), sectionLabel.getText());
    BorderPane sectionRectangle = (BorderPane) sectionUI.lookup(".section-border");
    Assertions.assertNotNull(sectionRectangle);

    // Action Test
    clickOn(finishButton);
  }

  @Test
  public void createButtonUI_CreateBasicWidget_ButtonClickCallbackCheck() {
    AtomicBoolean buttonClicked = new AtomicBoolean(false);

    Button buttonUI = SceneUIWidgetFactory.createButtonUI(
        DUMMY_LABEL,
        e -> {
          buttonClicked.set(true);}
    );
    Button finishButton = createBasicSplashScreen(buttonUI, "Button UI");

    // Action Test
    Assertions.assertEquals(DUMMY_LABEL.getValue(), buttonUI.getText());
    clickOn(buttonUI);
    Assertions.assertTrue(buttonClicked.get());

    // Close
    clickOn(finishButton);
  }

  @Test
  public void createDropDownUI_CreateBasicWidget_DropDownSelection() {
    HBox dropDownUI = SceneUIWidgetFactory.createDropDownUI(
        DUMMY_LABEL,
        DUMMY_STRING_COLLECTION_SUPPLIER,
        DUMMY_STRING_CONSUMER
    );
    Button finishButton = createBasicSplashScreen(dropDownUI, "Drop Down UI");

    // Assertions for UI initialization
    Label dropDownLabel = (Label) dropDownUI.lookup(".drop-down-label");
    ComboBox<String> dropDownComboBox = (ComboBox<String>) dropDownUI.lookup(".drop-down-box");
    Assertions.assertEquals(DUMMY_LABEL.getValue(), dropDownLabel.getText());

    // Action Test
    clickOn(dropDownUI);
    press(KeyCode.ENTER);
    press(KeyCode.DOWN);
    press(KeyCode.ENTER);
    Assertions.assertEquals(1, dropDownComboBox.getSelectionModel().getSelectedIndex());

    // Close
    clickOn(finishButton);
  }

  @Test
  public void setWidgetStyleSheet_SetSheet_WidgetSheetFieldCheck() {
    SceneUIWidgetFactory.setWidgetStyleSheet("test.css");

    // Assertions for setting the stylesheet
    String widgetStyleSheet;
    try {
      Field field = SceneUIWidgetFactory.class.getDeclaredField("WIDGET_STYLE_SHEET");
      field.setAccessible(true);
      widgetStyleSheet = (String) field.get(null);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    Assertions.assertEquals("test.css", widgetStyleSheet);

  }
}
