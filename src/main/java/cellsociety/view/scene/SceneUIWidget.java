package cellsociety.view.scene;

import cellsociety.view.controller.ThemeController;
import cellsociety.view.controller.ThemeController.Theme;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * A utility class for creating UI widgets.
 *
 * @author Hsuan-Kai Liao
 */
public class SceneUIWidget {
  // UI constants
  public static final double MAX_ZOOM_RATE = 8.0;
  public static final double MIN_ZOOM_RATE = 0.2;

  // Error Stage
  private static final double DEFAULT_SPLASH_WIDTH = 400;
  private static final double DEFAULT_SPLASH_HEIGHT = 300;
  private static String WIDGET_STYLE_SHEET;

  /* UI WIDGETS */

  /**
   * Create a range UI control with a slider and text field.
   * @param min minimum value
   * @param max maximum value
   * @param defaultValue default value
   * @param label label text
   * @param tooltip tooltip text
   * @param callback callback function to be called when the value changes
   * @return the range UI control HBox
   */
  public static HBox createRangeUI(double min, double max, double defaultValue, String label, String tooltip, Consumer<Double> callback) {
    // Create the slider
    Slider slider = new Slider(min, max, defaultValue);
    slider.setMaxWidth(Double.MAX_VALUE);

    // Create the label with tooltip
    Label speedLabel = new Label(label + ": ");
    speedLabel.getStyleClass().add("range-label");
    speedLabel.setTooltip(new Tooltip(tooltip));

    // Format the default value based on the type
    String defaultText;
    String minText;
    String maxText;
    defaultText = String.format("%.1f", defaultValue);
    minText = String.format("%.1f", min);
    maxText = String.format("%.1f", max);

    // Create the text input
    TextField textField = new TextField(defaultText);
    textField.getStyleClass().add("range-text-field");
    textField.setAlignment(Pos.CENTER);

    // Restrict input to numbers and decimal point
    UnaryOperator<Change> filter = change -> {
      String newText = change.getControlNewText();
      if (newText.matches("\\d*\\.?\\d*")) {
        return change;
      } else {
        return null;
      }
    };
    textField.setTextFormatter(new TextFormatter<>(filter));

    // Create min and max labels
    Label minLabel = new Label(minText + " ");
    minLabel.getStyleClass().add("range-minmax");
    Label maxLabel = new Label(" " + maxText);
    maxLabel.getStyleClass().add("range-minmax");

    // Create an HBox control
    HBox rangeControl = new HBox(5, speedLabel, textField, minLabel, slider, maxLabel);
    rangeControl.setAlignment(Pos.CENTER);
    HBox.setHgrow(slider, Priority.ALWAYS);
    rangeControl.getStyleClass().add("range-control");

    // Add slider listener
    slider.valueProperty().addListener((observable, oldValue, newValue) -> {
      // Update the text field with the formatted value
      textField.setText(String.format("%.1f", newValue.doubleValue()));

      // Trigger callback
      callback.accept(newValue.doubleValue());
    });

    // Add text listener
    textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
      if (event.getCode().toString().equals("ENTER")) {
        String newValueText = textField.getText();
        try {
          double newValue = Double.parseDouble(newValueText);

          // Clamp the value to the min and max
          if (newValue < min) {
            newValue = min;
          } else if (newValue > max) {
            newValue = max;
          }

          // Sync the slider value with the text field value
          slider.setValue(newValue);

          // Trigger callback
          callback.accept(newValue);

          // Update the text field to reflect the new value
          textField.setText(String.format("%.1f", newValue)
          );
        } catch (NumberFormatException e) {
          // Reset text field to slider value if invalid input
          double sliderValue = slider.getValue();
          textField.setText(String.format("%.1f", sliderValue));
        }
      }
    });

    // Add text focus listener
    textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) { // When focus is lost
        double sliderValue = slider.getValue();
        textField.setText(String.format("%.1f", sliderValue));
      }
    });

    // Call the callback with the default value
    callback.accept(defaultValue);

    return rangeControl;
  }

  /**
   * Create a range UI control with a text field.
   * @param defaultValue default value
   * @param label label text
   * @param tooltip tooltip text
   * @param callback callback function to be called when the value changes
   * @return the range UI control HBox
   */
  public static HBox createRangeUI(String defaultValue, String label, String tooltip, Consumer<String> callback) {
    // Create the label with tooltip
    Label labelComponent = new Label(label + ": ");
    labelComponent.getStyleClass().add("range-label");
    labelComponent.setTooltip(new Tooltip(tooltip));

    // Create the text input
    TextField textField = new TextField(defaultValue);
    textField.getStyleClass().add("range-text");
    textField.setAlignment(Pos.CENTER);

    // Restrict input to numbers, decimal point, and optional leading negative sign
    UnaryOperator<Change> filter = change -> {
      String newText = change.getControlNewText();
      // Allow letters, digits, and special characters, but not Unicode characters (like Chinese)
      if (newText.matches("[a-zA-Z0-9!\"#$%&'()*+,-./:;<=>?@\\\\^_`{|}~]*")) {
        return change;
      } else {
        return null;
      }
    };
    textField.setTextFormatter(new TextFormatter<>(filter));

    // Create an HBox control
    HBox rangeControl = new HBox(5, labelComponent, textField);
    rangeControl.setAlignment(Pos.CENTER);
    HBox.setHgrow(textField, Priority.ALWAYS);
    rangeControl.getStyleClass().add("range-control");

    // Add text listener
    textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
      if (event.getCode().toString().equals("ENTER")) {
        String newValue = textField.getText();
        callback.accept(newValue);
      }
    });

    // Add text focus listener
    textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) { // When focus is lost
        String textValue = textField.getText();
        callback.accept(textValue);
      }
    });

    // Call the callback with the default value
    callback.accept(defaultValue);

    return rangeControl;
  }

  /**
   * Create a draggable and zoom-able view UI control.
   *
   * @param content the content node to be displayed
   */
  public static Pane dragZoomViewUI(Node content) {
    Pane pane = new Pane();
    pane.getStyleClass().add("content-pane");
    pane.getChildren().add(content);

    // Create centering consumer
    Consumer<Node> centerContent = node -> {
      double paneWidth = pane.getWidth();
      double paneHeight = pane.getHeight();

      double nodeWidth = node.prefWidth(-1);
      double nodeHeight = node.prefHeight(-1);

      double centerX = (paneWidth - nodeWidth) / 2;
      double centerY = (paneHeight - nodeHeight) / 2;

      node.relocate(centerX, centerY);
    };

    // Clip the Pane to the desired size
    Rectangle clip = new Rectangle();
    pane.setClip(clip);

    // Update Clip and center content when the Pane size changes
    pane.widthProperty().addListener((observable, oldValue, newValue) -> {
      clip.setWidth(newValue.doubleValue());
      centerContent.accept(content);
    });
    pane.heightProperty().addListener((observable, oldValue, newValue) -> {
      clip.setHeight(newValue.doubleValue());
      centerContent.accept(content);
    });
    content.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
      centerContent.accept(content);
    });

    // Mouse and Scroll event handlers
    final double[] mouseX = new double[1];
    final double[] mouseY = new double[1];
    pane.setOnMousePressed(event -> {
      mouseX[0] = event.getSceneX() - content.getLayoutX();
      mouseY[0] = event.getSceneY() - content.getLayoutY();
    });
    pane.setOnMouseDragged(event -> {
      double offsetX = event.getSceneX() - mouseX[0];
      double offsetY = event.getSceneY() - mouseY[0];
      content.relocate(offsetX, offsetY);
    });

    final double[] scale = {1.0};
    pane.setOnZoom(event -> {
      double zoomFactor = event.getZoomFactor();

      scale[0] *= zoomFactor;

      // limit the scale to reasonable values
      scale[0] = Math.max(MIN_ZOOM_RATE, Math.min(scale[0], MAX_ZOOM_RATE));

      content.setScaleX(scale[0]);
      content.setScaleY(scale[0]);
    });
    pane.setOnScroll(event -> {
      double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;

      scale[0] *= zoomFactor;

      // limit the scale to reasonable values
      scale[0] = Math.max(MIN_ZOOM_RATE, Math.min(scale[0], MAX_ZOOM_RATE));

      content.setScaleX(scale[0]);
      content.setScaleY(scale[0]);

      event.consume();
    });

    return pane;
  }

  /**
   * Create a container UI with a title and content.
   * @param content the content of the container
   * @param title the title of the container
   * @return the container UI control
   */
  public static ScrollPane createContainerUI(Node content, String title) {
    Label containerTitle = new Label(title);
    containerTitle.getStyleClass().add("container-title");

    VBox containerContainer = new VBox(10, containerTitle, content);
    containerContainer.setAlignment(Pos.TOP_CENTER);
    containerContainer.getStyleClass().add("container-container");
    containerContainer.setPadding(new Insets(10));

    ScrollPane scrollPane = new ScrollPane(containerContainer);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);

    return scrollPane;
  }

  /**
   * Create a modal error dialog with a title, message, and exception details.
   * @param title   the title of the error dialog
   * @param message the message to display in the dialog
   * @param e       the exception to display (optional, can be null)
   */
  public static void createErrorDialog(String title, String message, Exception e) {
    Stage errorStage = new Stage();
    errorStage.setTitle(title);
    errorStage.initModality(Modality.APPLICATION_MODAL); // Must be closed before continuing interaction

    // Title
    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add("error-title");

    // Error message
    Label messageLabel = new Label(message);
    messageLabel.getStyleClass().add("error-message");

    // Exception details (if available)
    TextArea exceptionArea = new TextArea();
    exceptionArea.setEditable(false);
    exceptionArea.setWrapText(true);
    exceptionArea.setPrefHeight(100);
    if (e != null) {
      // Convert stack trace to string
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      exceptionArea.setText(sw.toString());
    } else {
      exceptionArea.setText("No additional details available.");
    }

    // Layout
    VBox layout = new VBox(15, titleLabel, messageLabel, exceptionArea);
    layout.setAlignment(Pos.TOP_CENTER);
    layout.setPadding(new Insets(20));
    layout.getStyleClass().add("error-container");
    VBox.setVgrow(exceptionArea, Priority.ALWAYS);

    // Create scene
    Scene scene = new Scene(layout, DEFAULT_SPLASH_WIDTH, DEFAULT_SPLASH_HEIGHT);
    if (WIDGET_STYLE_SHEET != null) {
      scene.getStylesheets().add(WIDGET_STYLE_SHEET);
    }
    errorStage.setScene(scene);

    // Show the dialog and wait for the user to close it
    errorStage.showAndWait();
  }

  /**
   * Create a splash screen with language and theme selection.
   * @param languageConsumer the consumer for the selected language
   * @param themeConsumer the consumer for the selected theme
   */
  public static void createSplashScreen(String defaultLanguage, Theme defaultTheme, Consumer<String> languageConsumer, Consumer<Theme> themeConsumer) {
    // Create a new stage for the splash screen
    Stage splashStage = new Stage();
    splashStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
    splashStage.setTitle("Cell Society");
    splashStage.setOnCloseRequest(event -> Platform.exit());

    // Create the splash screen scene
    Scene splashScene = new Scene(new Region(), DEFAULT_SPLASH_WIDTH, DEFAULT_SPLASH_HEIGHT);

    // Create the splash screen content
    VBox splashScreen = new VBox(20);
    splashScreen.getStyleClass().add("splash-screen");

    // Create the welcome label at the top
    Label welcomeLabel = new Label("Welcome");
    welcomeLabel.getStyleClass().add("splash-welcome");

    // TODO: SET UP LANGUAGE AND THEME SELECTION
    // Create the Language label and dropdown (ComboBox)
    HBox languageContainer = new HBox(10);
    languageContainer.setAlignment(Pos.CENTER);
    Label languageLabel = new Label("Language");
    languageLabel.getStyleClass().add("splash-label");
    ComboBox<String> languageComboBox = new ComboBox<>();
    HBox.setHgrow(languageComboBox, Priority.ALWAYS);
    languageComboBox.getItems().addAll("English", "French", "Spanish");
    languageComboBox.setValue(defaultLanguage);
    languageComboBox.getStyleClass().add("splash-combo-box");

    // Set listener for language selection and display selection
    languageComboBox.setOnAction(event -> {
      String selectedLanguage = languageComboBox.getValue();
      languageConsumer.accept(selectedLanguage);
      languageComboBox.setStyle("-fx-text-fill: #7a5cff;");
    });

    // Add the label and ComboBox into the languageContainer HBox
    languageContainer.getChildren().addAll(languageLabel, languageComboBox);

    // Create the Theme label and dropdown (ComboBox)
    HBox themeContainer = new HBox(10);
    themeContainer.setAlignment(Pos.CENTER);
    Label themeLabel = new Label("Theme");
    themeLabel.getStyleClass().add("splash-label");
    ComboBox<String> themeComboBox = new ComboBox<>();
    HBox.setHgrow(themeComboBox, Priority.ALWAYS);
    themeComboBox.getItems().addAll(
        Arrays.stream(ThemeController.Theme.values())
            .map(theme -> theme.name().substring(0, 1).toUpperCase() + theme.name().substring(1).toLowerCase())
            .toList()
    );
    themeComboBox.setValue(defaultTheme.name().substring(0, 1).toUpperCase() + defaultTheme.name().substring(1).toLowerCase());
    themeComboBox.getStyleClass().add("splash-combo-box");

    // Set listener for theme selection and display selection
    themeComboBox.setOnAction(event -> {
      String selectedTheme = themeComboBox.getValue();
      themeConsumer.accept(Theme.valueOf(selectedTheme.toUpperCase()));
      splashScene.getStylesheets().clear();
      splashScene.getStylesheets().add(WIDGET_STYLE_SHEET);
    });

    // Add the label and ComboBox into the themeContainer HBox
    themeContainer.getChildren().addAll(themeLabel, themeComboBox);

    // Add the language and theme selection to the splash screen
    HBox splashBox = new HBox(10, languageContainer, themeContainer);
    HBox.setHgrow(languageContainer, Priority.ALWAYS);
    HBox.setHgrow(themeContainer, Priority.ALWAYS);

    // Create the Start button
    Button startButton = new Button("Start");
    startButton.getStyleClass().add("splash-start-button");
    startButton.setOnAction(event -> splashStage.close());

    // Add all elements to the splash screen VBox
    splashScreen.getChildren().addAll(welcomeLabel, splashBox, startButton);

    // Set the scene with the splash screen
    splashScene.setRoot(splashScreen);
    if (WIDGET_STYLE_SHEET != null) {
      splashScene.getStylesheets().add(WIDGET_STYLE_SHEET);
    }
    splashStage.setScene(splashScene);

    // Show the splash screen and wait for it to close
    splashStage.showAndWait();
  }

  /* STYLESHEET */

  /**
   * Set a style sheet to the widget stages.
   * @param styleSheet the style sheet to add
   */
  public static void setWidgetStyleSheet(String styleSheet) {
    SceneUIWidget.WIDGET_STYLE_SHEET = styleSheet;
  }
}
