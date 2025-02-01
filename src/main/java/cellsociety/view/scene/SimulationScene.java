package cellsociety.view.scene;

import cellsociety.view.controller.SceneController;
import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
  public static final double DEFAULT_WIDTH = 600;
  public static final double DEFAULT_HEIGHT = 700;
  public static final double MAX_SPEED = 10;
  public static final double MIN_SPEED = 1;

  // Color constants
  private static final String BUTTON_START_COLOR = "#4CAF50";
  private static final String BUTTON_PAUSE_COLOR = "#FF5722";
  private static final String BUTTON_RESET_COLOR = "#f44336";
  private static final String BUTTON_LOAD_COLOR = "#2196F3";
  private static final String BUTTON_SAVE_COLOR = "#FF9800";
  private static final String BUTTON_DIRECTORY_COLOR = "#9E9E9E";
  private static final String BACKGROUND_COLOR = "#d4d4d9";
  private static final String LABEL_TEXT_COLOR = "#333333";
  private static final String BUTTON_TEXT_COLOR = "#ffffff";
  private static final String CONTROL_BG_COLOR = "#ffffff";
  private static final String BORDER_COLOR = "#cccccc";

  // UI components
  private Button startPauseButton;
  private Label titleLabel;
  private GridPane grid;
  private Slider speedSlider;
  private ComboBox<String> selectType;
  private TextField directoryField;
  private Label infoLabel;

  // Instance variables
  private final Stage primaryStage;
  private final SceneController controller;
  private double updateInterval;
  private double timeSinceLastUpdate;

  /**
   * Constructor for the SimulationScene class
   * @param primaryStage the primary stage of the application to display the scene
   */
  public SimulationScene(Stage primaryStage) {
    this.primaryStage = primaryStage;
    this.controller = new SceneController(this);
    this.updateInterval = 2.0 / (MAX_SPEED + MIN_SPEED);
    this.timeSinceLastUpdate = 0.0;

    VBox root = new VBox(10,
        createTitleLabel(),
        createGrid(),
        createSpeedControl(),
        createControls(),
        createInfoLabel()
    );
    root.setStyle("-fx-padding: 20px; -fx-background-color: " + BACKGROUND_COLOR + ";");

    primaryStage.setScene(new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT));
  }

  /**
   * Method to update the grid with the new state of the simulation
   * @param elapsedTime the time elapsed since the last update
   */
  public void step(double elapsedTime) {
    timeSinceLastUpdate += elapsedTime;
    if (timeSinceLastUpdate >= updateInterval) {
      controller.update();
      timeSinceLastUpdate = 0.0; // 重置计时器
    }
  }

  /* PRIVATE UI SETUP METHODS */

  private HBox createTitleLabel() {
    titleLabel = new Label("Simulation Title");
    titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + LABEL_TEXT_COLOR + ";");

    HBox titleContainer = new HBox(titleLabel);
    titleContainer.setAlignment(javafx.geometry.Pos.CENTER);

    return titleContainer;
  }

  private GridPane createGrid() {
    grid = new GridPane();
    grid.setStyle("-fx-border-color: " + BORDER_COLOR + "; -fx-background-color: " + CONTROL_BG_COLOR + "; -fx-min-height: 300px;");
    grid.setMinHeight(300);
    grid.setMaxHeight(300);

    return grid;
  }

  private HBox createSpeedControl() {
    speedSlider = new Slider(MIN_SPEED, MAX_SPEED, (MAX_SPEED + MIN_SPEED) / 2);
    speedSlider.setMaxWidth(Double.MAX_VALUE);
    Label speedLabel = new Label("Speed: ");
    Label minLabel = new Label("Min " + MIN_SPEED);
    Label maxLabel = new Label("Max " + MAX_SPEED);
    HBox speedControl = new HBox(10, speedLabel, minLabel, speedSlider, maxLabel);
    HBox.setHgrow(speedSlider, Priority.ALWAYS);
    speedControl.setStyle("-fx-padding: 10px; -fx-background-color: " + CONTROL_BG_COLOR + "; -fx-border-color: #dddddd;");

    // Add listener to the slider
    speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
      speedChangeCallback(newValue.doubleValue());
    });

    return speedControl;
  }

  private VBox createControls() {
    // Create buttons
    startPauseButton = new Button("Start");
    Button resetButton = new Button("Reset");
    Button loadButton = new Button("Load");
    Button saveButton = new Button("Save");
    Button directoryButton = new Button("📂");

    // Create directory text field
    directoryField = new TextField();
    directoryField.setPromptText("No directory selected");
    directoryField.setEditable(false);

    // Set button heights and color
    double buttonHeight = 35;
    startPauseButton.setMinHeight(buttonHeight);
    resetButton.setMinHeight(buttonHeight);
    loadButton.setMinHeight(buttonHeight);
    saveButton.setMinHeight(buttonHeight);
    directoryButton.setMinHeight(buttonHeight);
    directoryField.setMinHeight(buttonHeight);

    // Set button widths
    double totalWidth = 500;
    double button40 = totalWidth * 0.4; // 40%
    double button20 = totalWidth * 0.2; // 20%
    double directoryButtonWidth = totalWidth * 0.05; // 5%
    double directoryFieldWidth = totalWidth * 0.25; // 15%

    startPauseButton.setMinWidth(button40);
    loadButton.setMinWidth(button20);
    resetButton.setMinWidth(button40);
    saveButton.setMinWidth(button20);
    directoryButton.setMinWidth(directoryButtonWidth);
    directoryField.setMinWidth(directoryFieldWidth);

    // Style buttons with colors
    startPauseButton.setStyle("-fx-background-color: " + BUTTON_START_COLOR + "; -fx-text-fill: " + BUTTON_TEXT_COLOR + "; -fx-background-insets: 0,1,2; -fx-background-radius: 3,2,1;");
    resetButton.setStyle("-fx-background-color: " + BUTTON_RESET_COLOR + "; -fx-text-fill: " + BUTTON_TEXT_COLOR + "; -fx-background-insets: 0,1,2; -fx-background-radius: 3,2,1;");
    loadButton.setStyle("-fx-background-color: " + BUTTON_LOAD_COLOR + "; -fx-text-fill: " + BUTTON_TEXT_COLOR + "; -fx-background-insets: 0,1,2; -fx-background-radius: 3,2,1;");
    saveButton.setStyle("-fx-background-color: " + BUTTON_SAVE_COLOR + "; -fx-text-fill: " + BUTTON_TEXT_COLOR + "; -fx-background-insets: 0,1,2; -fx-background-radius: 3,2,1;");
    directoryButton.setStyle("-fx-background-color: " + BUTTON_DIRECTORY_COLOR + "; -fx-text-fill: " + BUTTON_TEXT_COLOR + "; -fx-background-insets: 0,1,2; -fx-background-radius: 3,2,1;");

    // Set ComboBox
    selectType = new ComboBox<>();
    selectType.getItems().addAll("None");
    selectType.setMinHeight(buttonHeight);
    selectType.setMinWidth(button40); // 40%

    // Button callbacks
    startPauseButton.setOnAction(e -> startPauseCallback());

    resetButton.setOnAction(e -> resetCallback());
    loadButton.setOnAction(e -> loadCallback(selectType.getValue()));
    saveButton.setOnAction(e -> saveCallback(directoryField.getText()));

    selectType.setOnMouseClicked(e -> selectDropDownCallback());

    // Directory selection button
    directoryButton.setOnAction(e -> directorySelectCallback());

    // HBox formatting
    HBox firstRow = new HBox(10, startPauseButton, loadButton, selectType);
    firstRow.setAlignment(Pos.CENTER);  // Use Pos.CENTER for alignment
    firstRow.setPadding(new Insets(5));

    HBox secondRow = new HBox(10, resetButton, saveButton, directoryField, directoryButton);
    secondRow.setAlignment(Pos.CENTER);  // Use Pos.CENTER for alignment
    secondRow.setPadding(new Insets(5));

    // VBox formatting
    VBox controls = new VBox(10, firstRow, secondRow);
    controls.setPadding(new Insets(10));
    controls.setAlignment(Pos.CENTER);  // Use Pos.CENTER for alignment

    return controls;
  }

  private Label createInfoLabel() {
    infoLabel = new Label("Information");
    infoLabel.setStyle("-fx-border-color: " + BORDER_COLOR + "; -fx-padding: 10px; -fx-background-color: " + CONTROL_BG_COLOR + "; -fx-alignment: CENTER_LEFT; -fx-font-size: 16px; -fx-font-weight: bold;");
    infoLabel.setMaxWidth(Double.MAX_VALUE);
    return infoLabel;
  }

  /* HANDLE ALL THE UI CALLBACK FUNCTIONS HERE */

  private void startPauseCallback() {
    // Start or pause the simulation
    if (!controller.isLoaded()) {
      return;
    }

    if ("Start".equals(startPauseButton.getText())) {
      startPauseButton.setText("Pause");
      startPauseButton.setStyle("-fx-background-color: " + BUTTON_PAUSE_COLOR + "; -fx-text-fill: " + BUTTON_TEXT_COLOR + ";");
      controller.setStartPause(false);
    } else {
      startPauseButton.setText("Start");
      startPauseButton.setStyle("-fx-background-color: " + BUTTON_START_COLOR + "; -fx-text-fill: " + BUTTON_TEXT_COLOR + ";");
      controller.setStartPause(true);
    }
  }

  private void speedChangeCallback(double speed) {
    // Change the speed of the simulation
    updateInterval = 1.0 / speed;
  }

  private void selectDropDownCallback() {
    // Select a simulation type
    controller.getAllConfigFileNames();

    // Set all the config file names to the dropdown
    String[] items = (controller.getAllConfigFileNames().toArray(new String[0]));
    selectType.getItems().clear();
    selectType.getItems().addAll(items);
  }

  private void resetCallback() {
    // Reset the simulation
    controller.resetModel();

    // Force to pause
    if ("Pause".equals(startPauseButton.getText())) {
      startPauseButton.setText("Start");
      startPauseButton.setStyle("-fx-background-color: " + BUTTON_START_COLOR + "; -fx-text-fill: " + BUTTON_TEXT_COLOR + ";");
      controller.setStartPause(true);
    }

    // Update the Speed
    speedSlider.setValue(controller.getConfigSpeed());
  }

  private void loadCallback(String filename) {
    // Load a new simulation
    if (!"None".equals(filename) && filename != null && !filename.isEmpty()) {
      controller.loadConfig(filename);
      controller.resetModel();

      // Force to pause
      if ("Pause".equals(startPauseButton.getText())) {
        startPauseButton.setText("Start");
        startPauseButton.setStyle("-fx-background-color: " + BUTTON_START_COLOR + "; -fx-text-fill: " + BUTTON_TEXT_COLOR + ";");
        controller.setStartPause(true);
      }

      // Update the Speed
      speedSlider.setValue(controller.getConfigSpeed());

      // Update the UI Text
      titleLabel.setText(controller.getConfigTitle());
      infoLabel.setText(controller.getConfigInformation());
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
    // TODO: Save the current simulation
  }

  /* PUBLIC UI SETS METHOD */

  public void setGrid(int numOfRows, int numOfCols) {
    SceneRenderer.drawGrid(grid, numOfRows, numOfCols);
  }

  public void setCell(int row, int col, Enum<?> state) {
    SceneRenderer.drawCell(grid, row, col, state);
  }
}
