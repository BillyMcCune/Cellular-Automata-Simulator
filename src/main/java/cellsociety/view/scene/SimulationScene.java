package cellsociety.view.scene;

import static java.lang.Math.abs;

import cellsociety.logging.Log;
import cellsociety.view.controller.ThemeController;
import cellsociety.view.controller.ThemeController.Theme;
import cellsociety.view.controller.ThemeController.UIComponent;
import cellsociety.view.docking.Docker;
import cellsociety.view.docking.Docker.DockPosition;
import cellsociety.view.controller.SceneController;
import java.io.File;
import java.util.function.Consumer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Shape;
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

  public static final double MAX_SPEED = 100;
  public static final double MIN_SPEED = 0;
  public static final double SPEED_MULTIPLIER = 3;
  public static final String SPEED_TOOLTIP = "Change the speed of the simulation";

  // UI components
  private Button startPauseButton;
  private GridPane grid;
  private VBox parameterBox;
  private ComboBox<String> selectType;
  private TextField directoryField;
  private Label infoText;

  // Instance variables
  private final Stage primaryStage;
  private final Docker docker;
  private final SceneController controller;

  private double updateInterval;
  private double timeSinceLastUpdate;

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
    this.updateInterval = 2.0 / (MAX_SPEED + MIN_SPEED);
    this.timeSinceLastUpdate = 0.0;

    // Create the UI components
    Pane gridParent = createGrid();
    ScrollPane controls = createControls();
    ScrollPane infoLabel = createInfoPanel();
    ScrollPane parameterPanel = createParameterPanel();
    VBox.setVgrow(gridParent, Priority.ALWAYS);

    // Create a floating window for each component
    docker.createDWindow("Controls", controls, DockPosition.TOP);
    docker.createDWindow("Info", infoLabel, DockPosition.TOP);
    docker.createDWindow("Grid", gridParent, DockPosition.RIGHT);
    docker.createDWindow("Parameters", parameterPanel, DockPosition.RIGHT);
    docker.reformat();

    // Set the default style
    setUIStyle(Theme.DAY);
  }

  /**
   * Start the simulation scene with the given frames per second
   * @param framesPerSecond the number of frames per second
   */
  public void start(int framesPerSecond) {
    // Create the splash screen
    SceneUIWidget.createSplashScreen("English", Theme.DAY, this::splashScreenLanguageCallback, this::splashScreenThemeCallback, () -> {
      // Log the welcome
      Log.info("Welcome to Cell Society! This simulation is made by Jacob You, Hsuan-Kai Liao, and Billy McCune.");

      // Show the primary stage
      primaryStage.show();

      // Set up the game loop
      Timeline gameLoop = new Timeline();
      gameLoop.setCycleCount(Timeline.INDEFINITE);
      gameLoop.getKeyFrames().add(new KeyFrame(javafx.util.Duration.seconds(1.0 / framesPerSecond), e -> step(1.0 / framesPerSecond)));
      gameLoop.play();
    });
  }

  /* PRIVATE UI SETUP METHODS */

  private Pane createGrid() {
    grid = new GridPane();
    grid.setGridLinesVisible(false);
    grid.getStyleClass().add("grid-panel");
    grid.setOpacity(0);

    return SceneUIWidget.dragZoomViewUI(grid);
  }

  private ScrollPane createParameterPanel() {
    parameterBox = new VBox(5);
    parameterBox.setAlignment(Pos.TOP_CENTER);
    parameterBox.getStyleClass().add("parameter-box");
    parameterBox.setMinHeight(Region.USE_COMPUTED_SIZE);
    VBox.setVgrow(parameterBox, Priority.ALWAYS);

    return SceneUIWidget.createContainerUI(parameterBox, "Parameters");
  }

  private ScrollPane createControls() {
    // Create buttons
    startPauseButton = SceneUIWidget.createButtonUI("Start", e -> startPauseCallback());
    Button resetButton = SceneUIWidget.createButtonUI("Reset", e -> resetCallback());
    Button loadButton = SceneUIWidget.createButtonUI("Load", e -> loadCallback(selectType.getValue()));
    Button saveButton = SceneUIWidget.createButtonUI("Save", e -> saveCallback(directoryField.getText()));
    Button directoryButton = SceneUIWidget.createButtonUI("📂", e -> directorySelectCallback());
    Button flipButton = SceneUIWidget.createButtonUI("Flip", e -> flipCallback());

    // Create checkbox
    CheckBox showBorderCheckBox = new CheckBox("Striking Border");
    showBorderCheckBox.getStyleClass().add("border-checkbox");
    showBorderCheckBox.setOnAction(e -> toggleBorderCallback(showBorderCheckBox.isSelected()));
    showBorderCheckBox.setSelected(doShowBorder);

    // Set button sizes
    startPauseButton.setPrefWidth(SceneUIWidget.BUTTON_WIDTH);
    startPauseButton.setPrefHeight(SceneUIWidget.BUTTON_HEIGHT);
    startPauseButton.setMaxWidth(SceneUIWidget.MAX_BUTTON_WIDTH);
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
    loadButton.getStyleClass().add("load-button");
    saveButton.getStyleClass().add("save-button");
    directoryButton.getStyleClass().add("directory-button");
    flipButton.getStyleClass().add("flip-button");

    // HBox formatting for each row
    HBox row1 = new HBox(10, startPauseButton, resetButton);
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

    BorderPane section1 = SceneUIWidget.createSectionUI("Operations", row1);
    BorderPane section2 = SceneUIWidget.createSectionUI("Load & Save", row2, row3);
    BorderPane section3 = SceneUIWidget.createSectionUI("Grid Settings", row4);

    // Main VBox containing all sections
    VBox controlsBox = new VBox(10, section1, section2, section3);
    controlsBox.setAlignment(Pos.CENTER);
    controlsBox.getStyleClass().add("controls-box");
    controlsBox.setPadding(new Insets(10));
    controlsBox.setMinHeight(Region.USE_COMPUTED_SIZE);
    VBox.setVgrow(controlsBox, Priority.ALWAYS);

    return SceneUIWidget.createContainerUI(controlsBox, "Controls");
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

    return SceneUIWidget.createContainerUI(infoBox, "Information");
  }

  /* HANDLE ALL THE UI CALLBACK FUNCTIONS HERE */

  private void startPauseCallback() {
    // Start or pause the simulation
    if (!controller.isLoaded()) {
      return;
    }

    toggleStartPauseButton(!controller.isPaused());
  }

  private void speedChangeCallback(double speed) {
    // Change the speed of the simulation
    updateInterval = 10 / (speed * SPEED_MULTIPLIER);
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

  private void loadCallback(String filename) {
    // Load a new simulation
    if (!"None".equals(filename) && filename != null && !filename.isEmpty()) {
      // Force to pause
      toggleStartPauseButton(true);

      // Load the config file
      controller.loadConfig(filename);

      // TODO: Move this into the controller
      // Clear the parameter box except for the speed
      parameterBox.getChildren().clear();
      setParameter(MIN_SPEED, MAX_SPEED, controller.getConfigSpeed(), "Speed", SPEED_TOOLTIP, this::speedChangeCallback);

      // Reset the simulation
      controller.resetModel();

      // Update the UI Text
      primaryStage.setTitle(controller.getConfigTitle());
      infoText.setText(controller.getConfigInformation());

      // Center the grid
      if (grid.getOpacity() == 0) {
        grid.setOpacity(1);
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

    for (Node node : grid.getChildren()) {
      ((Shape) node).setStrokeWidth(showBorder ? SceneRenderer.DEFAULT_BORDER_SIZE : 0);
    }
  }

  private void splashScreenThemeCallback(Theme theme) {
    setUIStyle(theme);
  }

  private void splashScreenLanguageCallback(String language) {
    // TODO: Implement language change
  }
  /* PUBLIC UI SETS METHOD */

  /**
   * Set the grid with the given number of rows and columns
   * @param numOfRows the number of rows in the grid
   * @param numOfCols the number of columns in the grid
   */
  public void setGrid(int numOfRows, int numOfCols) {
    SceneRenderer.drawGrid(grid, numOfRows, numOfCols);
  }

  /**
   * Set the cell at the given row and column with the given state
   * @param row the row of the cell
   * @param col the column of the cell
   * @param state the state of the cell
   */
  public void setCell(int row, int col, Enum<?> state) {
    SceneRenderer.drawCell(grid, row, col, state);
  }

  /**
   * Set the parameter with the given label, min, max, default value, tooltip, and callback
   * @param label the label of the parameter
   * @param min the minimum value of the parameter
   * @param max the maximum value of the parameter
   * @param defaultValue the default value of the parameter
   * @param tooltip the tooltip of the parameter
   * @param callback the callback function of the parameter
   */
  public void setParameter(double min, double max, double defaultValue, String label, String tooltip, Consumer<Double> callback) {
    parameterBox.getChildren().add(SceneUIWidget.createRangeUI(min, max, defaultValue, label, tooltip, callback));
  }

  /**
   * Set the parameter with the given default value, label, tooltip, and callback
   * @param defaultValue the default value of the parameter
   * @param label the label of the parameter
   * @param tooltip the tooltip of the parameter
   * @param callback the callback function of the parameter
   */
  public void setParameter(String defaultValue, String label, String tooltip, Consumer<String> callback) {
    parameterBox.getChildren().add(SceneUIWidget.createRangeUI(defaultValue, label, tooltip, callback));
  }

  /**
   * Set the UI style with the given theme
   * @param theme the theme of the UI style
   */
  public void setUIStyle(Theme theme) {
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
    SceneUIWidget.setWidgetStyleSheet(widgetSheet);
  }

  /* PRIVATE UI HELPER METHODS */

  private void step(double elapsedTime) {
    timeSinceLastUpdate += elapsedTime;
    if (timeSinceLastUpdate >= updateInterval) {
      controller.update();
      timeSinceLastUpdate = 0.0;
    }
  }

  private void toggleStartPauseButton(boolean isPause) {
    if (isPause) {
      startPauseButton.setText("Start");
      startPauseButton.getStyleClass().setAll("button", "start-button");
      controller.setStartPause(true);
    } else {
      startPauseButton.setText("Pause");
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
}
