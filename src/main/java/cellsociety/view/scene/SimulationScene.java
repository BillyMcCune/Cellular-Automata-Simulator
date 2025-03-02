package cellsociety.view.scene;

import static java.lang.Math.abs;

import cellsociety.logging.Log;
import cellsociety.view.controller.LanguageController;
import cellsociety.view.controller.LanguageController.Language;
import cellsociety.view.controller.ThemeController;
import cellsociety.view.controller.ThemeController.Theme;
import cellsociety.view.controller.ThemeController.UIComponent;
import cellsociety.view.controller.SceneController;
import cellsociety.view.docking.DWindow;
import cellsociety.view.docking.Docker;
import cellsociety.view.docking.Docker.DockPosition;
import cellsociety.view.renderer.drawer.GridDrawer;
import cellsociety.view.renderer.SceneRenderer;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * The SimulationScene class represents the main scene of the application where the simulation is displayed.
 * This is a pure UI class that is responsible for setting up the JavaFX scene and updating the UI components.
 *
 * @author Hsuan-Kai Liao
 */
public class SimulationScene {
  // Constants
  public static final double DEFAULT_WIDTH = 1200;
  public static final double DEFAULT_HEIGHT = 700;

  public static final double NEW_SIMULATION_OFFSETX = 50;
  public static final double NEW_SIMULATION_OFFSETY = 50;

  // UI components
  private Button startPauseButton;
  private Pane grid;
  private Pane miniGrid;
  private VBox parameterBox;
  private ComboBox<String> selectType;
  private TextField directoryField;
  private Label infoText;

  // Instance variables
  private final Stage primaryStage;
  private final Docker docker;
  private final SceneController controller;

  private int framesPerSecond;
  private boolean doShowBorder = true;

  /**
   * Constructor for the SimulationScene class
   * @param primaryStage the primary stage of the application
   */
  public SimulationScene(Stage primaryStage) {
    primaryStage.setWidth(DEFAULT_WIDTH);
    primaryStage.setHeight(DEFAULT_HEIGHT);

    this.primaryStage = primaryStage;
    this.docker = new Docker(primaryStage);
    this.controller = new SceneController(this);

    // Set the default style and language
    updateUIStyle(Theme.DAY);
    updateUILang(Language.ENGLISH);

    // Create the UI components
    Pane gridParent = createGrid();
    ScrollPane controls = createControls();
    ScrollPane infoLabel = createInfoPanel();
    ScrollPane parameterPanel = createParameterPanel();
    ScrollPane logPanel = createLogPanel();
    VBox.setVgrow(gridParent, Priority.ALWAYS);

    // Create a floating window for each component
    docker.createDWindow(LanguageController.getStringProperty("controls-window"), controls, DockPosition.TOP, null);
    docker.createDWindow(LanguageController.getStringProperty("grid-window"), gridParent, DockPosition.RIGHT, null);
    DWindow parameterWindow = docker.createDWindow(LanguageController.getStringProperty("parameters-window"), parameterPanel, DockPosition.RIGHT, null);
    DWindow logWindow = docker.createDWindow(LanguageController.getStringProperty("log-window"), logPanel, DockPosition.BOTTOM, parameterWindow);
    docker.createDWindow(LanguageController.getStringProperty("info-window"), infoLabel, DockPosition.CENTER, logWindow);
    docker.reformat();
  }

  /**
   * Start the simulation scene with the given frames per second
   * @param framesPerSecond the number of frames per second
   */
  public void start(int framesPerSecond, boolean showSplashScreen) {
    this.framesPerSecond = framesPerSecond;

    // Callback to start the simulation
    Runnable startSimulationCallback = () -> {
      // Show the primary stage
      primaryStage.show();

      // Set up the game loop
      Timeline gameLoop = new Timeline();
      gameLoop.setCycleCount(Timeline.INDEFINITE);
      gameLoop.getKeyFrames().add(
          new KeyFrame(javafx.util.Duration.seconds(1.0 / framesPerSecond),
              e -> controller.update(1.0 / framesPerSecond)));
      gameLoop.play();
    };

    // Create the splash screen
    if (showSplashScreen) {
      SceneUIWidget.createSplashScreen(
        Language.ENGLISH,
        Theme.DAY,
        LanguageController.getStringProperty("welcome-title"),
        LanguageController.getStringProperty("welcome-button"),
        LanguageController.getStringProperty("welcome-language"),
        LanguageController.getStringProperty("welcome-theme"),
        this::splashScreenLanguageCallback,
        this::splashScreenThemeCallback,
        () -> {
          // Log the welcome
          Log.info("Welcome to Cell Society! This simulation is made by Jacob You, Hsuan-Kai Liao, and Billy McCune.");

          // Run the simulation
          startSimulationCallback.run();
        }
      );
    } else {
      // Run the simulation
      Log.trace("New simulation started.");

      startSimulationCallback.run();
    }
  }

  /* PRIVATE UI SETUP METHODS */

  private Pane createGrid() {
    grid = new Pane();
    grid.getStyleClass().add("grid-panel");
    grid.setOpacity(0);

    miniGrid = new Pane();
    miniGrid.getStyleClass().add("grid-panel");
    miniGrid.setOpacity(0);

    return SceneUIWidget.dragZoomViewUI(grid, miniGrid);
  }

  private ScrollPane createParameterPanel() {
    parameterBox = new VBox(5);
    parameterBox.setAlignment(Pos.TOP_CENTER);
    parameterBox.getStyleClass().add("parameter-box");
    parameterBox.setMinHeight(Region.USE_COMPUTED_SIZE);
    VBox.setVgrow(parameterBox, Priority.ALWAYS);

    return SceneUIWidget.createContainerUI(parameterBox, LanguageController.getStringProperty("parameter-panel"));
  }

  private ScrollPane createControls() {
    // Create buttons
    startPauseButton = SceneUIWidget.createButtonUI(LanguageController.getStringProperty("start-button"), e -> startPauseCallback());
    Button resetButton = SceneUIWidget.createButtonUI(LanguageController.getStringProperty("reset-button"), e -> resetCallback());
    Button newButton = SceneUIWidget.createButtonUI(LanguageController.getStringProperty("new-button"), e -> newSimulationCallback());
    Button loadButton = SceneUIWidget.createButtonUI(LanguageController.getStringProperty("load-button"), e -> loadCallback(selectType.getValue()));
    Button saveButton = SceneUIWidget.createButtonUI(LanguageController.getStringProperty("save-button"), e -> saveCallback(directoryField.getText()));
    Button directoryButton = SceneUIWidget.createButtonUI(LanguageController.getStringProperty("directory-button"), e -> directorySelectCallback());
    Button flipButton = SceneUIWidget.createButtonUI(LanguageController.getStringProperty("flip-button"), e -> flipCallback());

    // Create checkbox
    CheckBox showBorderCheckBox = new CheckBox();
    showBorderCheckBox.textProperty().bind(LanguageController.getStringProperty("checkbox-text"));
    showBorderCheckBox.getStyleClass().add("border-checkbox");
    showBorderCheckBox.setOnAction(e -> toggleBorderCallback(showBorderCheckBox.isSelected()));
    showBorderCheckBox.setSelected(doShowBorder);

    // Set button sizes
    directoryButton.setPrefWidth(SceneUIWidget.BUTTON_WIDTH * 0.8);

    // Set ComboBox
    selectType = new ComboBox<>();
    selectDropDownCallback();
    selectType.setPromptText("...");
    selectType.setOnMouseClicked(e -> selectDropDownCallback());
    selectType.setPrefWidth(SceneUIWidget.BUTTON_WIDTH * 1.8);
    selectType.setMaxWidth(SceneUIWidget.MAX_BUTTON_WIDTH);
    selectType.setPrefHeight(SceneUIWidget.BUTTON_HEIGHT);

    // Create directory text field
    directoryField = new TextField();
    directoryField.setPromptText("...");
    directoryField.setPrefWidth(SceneUIWidget.BUTTON_WIDTH - 10);
    directoryField.setMaxWidth(SceneUIWidget.MAX_BUTTON_WIDTH);
    directoryField.setPrefHeight(SceneUIWidget.BUTTON_HEIGHT);

    // Style buttons
    startPauseButton.getStyleClass().add("start-button");
    resetButton.getStyleClass().add("reset-button");
    newButton.getStyleClass().add("new-button");
    loadButton.getStyleClass().add("load-button");
    saveButton.getStyleClass().add("save-button");
    directoryButton.getStyleClass().add("directory-button");
    flipButton.getStyleClass().add("flip-button");

    // HBox formatting for each dx
    HBox row1 = new HBox(10, startPauseButton, resetButton, newButton);
    row1.setAlignment(Pos.CENTER);
    row1.setPadding(new Insets(5));

    HBox row2 = new HBox(10, loadButton, selectType);
    row2.setAlignment(Pos.CENTER);
    row2.setPadding(new Insets(5));

    HBox row3 = new HBox(10, saveButton, directoryField, directoryButton);
    row3.setAlignment(Pos.CENTER);
    row3.setPadding(new Insets(5));

    HBox row4 = new HBox(10, flipButton, showBorderCheckBox);
    row4.setAlignment(Pos.CENTER);
    row4.setPadding(new Insets(5));

    // Create sections
    BorderPane section1 = SceneUIWidget.createSectionUI(LanguageController.getStringProperty("operations-section"), row1);
    BorderPane section2 = SceneUIWidget.createSectionUI(LanguageController.getStringProperty("io-section"), row2, row3);
    BorderPane section3 = SceneUIWidget.createSectionUI(LanguageController.getStringProperty("grid-section"), row4);
    BorderPane section4 = SceneUIWidget.createSectionUI(LanguageController.getStringProperty("themelang-section"), SceneUIWidget.createThemeLanguageSelectorUI(
        LanguageController.getStringProperty("welcome-language"),
        LanguageController.getStringProperty("welcome-theme"),
        this::splashScreenLanguageCallback,
        this::splashScreenThemeCallback,
        "...",
        "..."
    ));

    // Main VBox containing all sections
    VBox controlsBox = new VBox(10, section1, section2, section3, section4);
    controlsBox.setAlignment(Pos.CENTER);
    controlsBox.getStyleClass().add("controls-box");
    controlsBox.setPadding(new Insets(10));
    controlsBox.setMinHeight(Region.USE_COMPUTED_SIZE);
    VBox.setVgrow(controlsBox, Priority.ALWAYS);

    return SceneUIWidget.createContainerUI(controlsBox, LanguageController.getStringProperty("controls-panel"));
  }

  private ScrollPane createInfoPanel() {
    infoText = new Label();
    infoText.getStyleClass().add("info-text");
    infoText.setWrapText(true);

    HBox infoContainer = new HBox(10, infoText);
    HBox.setHgrow(infoText, Priority.ALWAYS);
    infoContainer.setAlignment(Pos.CENTER_LEFT);

    VBox infoBox = new VBox(10, infoContainer);
    infoBox.setAlignment(Pos.TOP_CENTER);
    infoBox.getStyleClass().add("info-box");
    infoBox.setPadding(new Insets(10));
    infoBox.setMinHeight(Region.USE_COMPUTED_SIZE);
    VBox.setVgrow(infoBox, Priority.ALWAYS);

    return SceneUIWidget.createContainerUI(infoBox, LanguageController.getStringProperty("info-panel"));
  }

  private ScrollPane createLogPanel() {
    // Create the log text area
    TextFlow logText = new TextFlow();
    logText.getStyleClass().add("log-text");
    Log.addLogListener(log -> {
      Text text = processAnsiCodes(log);
      logText.getChildren().add(text);
    });
    VBox.setVgrow(logText, Priority.ALWAYS);

    // Create the log container
    VBox logContainer = new VBox(logText);
    logContainer.setAlignment(Pos.TOP_LEFT);
    logContainer.getStyleClass().add("log-box");
    logContainer.setMinHeight(Region.USE_COMPUTED_SIZE);
    VBox.setVgrow(logContainer, Priority.ALWAYS);

    return SceneUIWidget.createContainerUI(logContainer, LanguageController.getStringProperty("log-panel"));
  }

  /* HANDLE ALL THE UI CALLBACK FUNCTIONS HERE */

  private void startPauseCallback() {
    // Start or pause the simulation
    if (!controller.isLoaded()) {
      Log.warn("Simulation is not loaded. Aborting start.");
      return;
    }

    toggleStartPauseButton(!controller.isPaused());
  }

  private void selectDropDownCallback() {
    // Original value
    String originalValue = selectType.getValue();

    // Select a simulation type
    controller.getAllConfigFileNames();

    // Set all the config file names to the dropdown
    String[] items = controller.getAllConfigFileNames().stream().sorted().toArray(String[]::new);
    selectType.getItems().clear();
    selectType.getItems().addAll(items);
    selectType.setValue(originalValue);
  }

  private void resetCallback() {
    // Force to pause
    toggleStartPauseButton(true);

    // Reset the simulation
    controller.resetGrid();

    // Center the grid
    centerGrid();

    // Reset the flip
    resetFlip();
  }

  private void newSimulationCallback() {
    Stage newStage = new Stage();
    newStage.setX(primaryStage.getX() + NEW_SIMULATION_OFFSETX);
    newStage.setY(primaryStage.getY() + NEW_SIMULATION_OFFSETY);
    newStage.setTitle("New Simulation");

    SimulationScene newScene = new SimulationScene(newStage);
    newScene.start(framesPerSecond, false);
  }

  private void loadCallback(String filename) {
    // Load a new simulation
    if (!"None".equals(filename) && filename != null && !filename.isEmpty()) {
      // Force to pause
      toggleStartPauseButton(true);

      // Load the config file
      controller.loadConfig(filename);

      // Reset the simulation
      controller.resetModel();

      // Update the Title Text
      primaryStage.setTitle(controller.getSimulationTitle());

      // Center the grid
      if (grid.getOpacity() == 0) {
        grid.setOpacity(1);
        miniGrid.setOpacity(1);
      }
      centerGrid();

      // Reset the flip
      resetFlip();
    }
  }

  private void directorySelectCallback() {
    // Select a directory
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle("Select Directory");
    File selectedDirectory = directoryChooser.showDialog(primaryStage);

    if (selectedDirectory != null) {
      directoryField.setText(selectedDirectory.getAbsolutePath());
    }
  }

  private void saveCallback(String path) {
    if (path != null && !path.isEmpty()) {
      // Force to pause
      toggleStartPauseButton(true);

      // Save the current configuration
      controller.saveConfig(path);
    }
  }

  private void flipCallback() {
    // Flip the grid
    double scaleX = grid.getScaleX();
    double scaleY = grid.getScaleY();

    if (scaleX * scaleY < 0) {
      if (scaleX > 0) {
        grid.setScaleX(-1 * scaleX);
      } else {
        grid.setScaleY(-1 * scaleY);
      }
    } else {
      grid.setScaleX(-1 * scaleX);
      grid.setScaleY(-1 * scaleY);
    }
  }

  private void toggleBorderCallback(boolean showBorder) {
    doShowBorder = showBorder;
    List<Node> children = grid.getChildren();

    // The last child is the boundary
    for (int i = 0; i < children.size() - 1; i++) {
      ((Shape) children.get(i)).setStrokeWidth(showBorder ? GridDrawer.DEFAULT_BORDER_SIZE : 0);
    }
  }

  private void splashScreenThemeCallback(Theme theme) {
    updateUIStyle(theme);
  }

  private void splashScreenLanguageCallback(Language language) {
    updateUILang(language);
  }
  /* PUBLIC UI SETS METHOD */

  /**
   * Set the grid with the given number of rows and columns
   * @param numOfRows the number of rows in the grid
   * @param numOfCols the number of columns in the grid
   */
  public void setGrid(int numOfRows, int numOfCols) {
    SceneRenderer.drawGrid(grid, numOfRows, numOfCols);
    SceneRenderer.drawGrid(miniGrid, numOfRows, numOfCols);
  }

  /**
   * Set the cell at the given dx and column with the given state
   * @param row the dx of the cell
   * @param col the column of the cell
   * @param color the state of the cell
   */
  public void setCell(int rowCount, int row, int col, String color) {
    SceneRenderer.drawCell(grid, rowCount, row, col, color);
  }

  /**
   * Set the info text with the given information
   *
   * @param info the information to be displayed
   */
  public void setInfo(String info) {
    infoText.setText(info);
  }

  /**
   * Clear all the parameters in the parameter box
   */
  public void clearParameters() {
    parameterBox.getChildren().clear();
  }

  /**
   * Set the parameter with the given label, min, max, default value, tooltip, and callback
   * @param labelKey the label key for the StringProperty of the parameter
   * @param min the minimum value of the parameter
   * @param max the maximum value of the parameter
   * @param defaultValue the default value of the parameter
   * @param tooltipKey the tooltip key for the StringProperty of the parameter
   * @param callback the callback function of the parameter
   */
  public void setParameter(double min, double max, double defaultValue, String labelKey, String tooltipKey, Consumer<Double> callback) {
    parameterBox.getChildren().add(SceneUIWidget.createRangeUI(min, max, defaultValue, LanguageController.getStringProperty(labelKey), LanguageController.getStringProperty(tooltipKey), callback));
  }

  /**
   * Set the parameter with the given default value, label, tooltip, and callback
   * @param defaultValue the default value of the parameter
   * @param labelKey the label key for the StringProperty of the parameter
   * @param tooltipKey the tooltip key for the StringProperty of the parameter
   * @param callback the callback function of the parameter
   */
  public void setParameter(String defaultValue, String labelKey, String tooltipKey, Consumer<String> callback) {
    parameterBox.getChildren().add(SceneUIWidget.createRangeUI(defaultValue, LanguageController.getStringProperty(labelKey), LanguageController.getStringProperty(tooltipKey), callback));
  }

  /* PRIVATE UI HELPER METHODS */

  private static Text processAnsiCodes(String message) {
    BiFunction<String, String, Text> createTextNode = (text, colorStyle) -> {
      Text textNode = new Text(text + "\n");
      textNode.getStyleClass().add("log-text-" + colorStyle);
      return textNode;
    };

    if (message.startsWith(Log.ERROR_COLOR)) {
      return createTextNode.apply(message.substring(Log.ERROR_COLOR.length()), "error");
    } else if (message.startsWith(Log.WARN_COLOR)) {
      return createTextNode.apply(message.substring(Log.WARN_COLOR.length()), "warn");
    } else if (message.startsWith(Log.INFO_COLOR)) {
      return createTextNode.apply(message.substring(Log.INFO_COLOR.length()), "info");
    } else if (message.startsWith(Log.TRACE_COLOR)) {
      return createTextNode.apply(message.substring(Log.TRACE_COLOR.length()), "trace");
    } else {
      return createTextNode.apply(message, "default");
    }
  }

  private void toggleStartPauseButton(boolean isPause) {
    if (isPause) {
      startPauseButton.textProperty().bind(LanguageController.getStringProperty("start-button"));
      startPauseButton.getStyleClass().setAll("button", "start-button");
      controller.setStartPause(true);
    } else {
      startPauseButton.textProperty().bind(LanguageController.getStringProperty("pause-button"));
      startPauseButton.getStyleClass().setAll("button", "pause-button");
      controller.setStartPause(false);
    }
  }

  private void centerGrid() {
    Pane pane = (Pane) grid.getParent();

    double paneWidth = pane.getWidth();
    double paneHeight = pane.getHeight();

    double gridWidth = grid.getWidth();
    double gridHeight = grid.getHeight();

    double centerX = (paneWidth - gridWidth) / 2;
    double centerY = (paneHeight - gridHeight) / 2;

    grid.relocate(centerX, centerY);
  }

  private void resetFlip() {
    grid.setScaleX(abs(grid.getScaleX()));
    grid.setScaleY(abs(grid.getScaleY()));
  }

  private void updateUIStyle(Theme theme) {
    // Get the style sheets for the scene, widget, and docking
    String sceneSheet = ThemeController.getThemeSheet(theme, UIComponent.SCENE);
    String widgetSheet = ThemeController.getThemeSheet(theme, UIComponent.WIDGET);
    String dockingSheet = ThemeController.getThemeSheet(theme, UIComponent.DOCKING);

    // Clear the current style sheets and add the new ones
    primaryStage.getScene().getStylesheets().clear();
    docker.clearStyleSheets();

    // Add the new style sheets
    primaryStage.getScene().getStylesheets().add(sceneSheet);
    primaryStage.getScene().getStylesheets().add(widgetSheet);
    docker.addStyleSheet(dockingSheet);
    docker.addStyleSheet(sceneSheet);
    docker.addStyleSheet(widgetSheet);
    SceneUIWidget.setWidgetStyleSheet(widgetSheet);
  }

  private void updateUILang(Language language) {
    LanguageController.switchLanguage(language);
  }
}
