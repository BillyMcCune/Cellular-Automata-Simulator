package cellsociety.view.scene;

import cellsociety.view.controller.SceneController;
import java.io.File;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
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
  public static final String STYLE_PATH = "/cellsociety/style/style.css";

  public static final double DEFAULT_WIDTH = 600;
  public static final double DEFAULT_HEIGHT = 700;

  public static final double DEFAULT_GRID_WIDTH = 300;
  public static final double DEFAULT_GRID_HEIGHT = 300;
  public static final double MAX_ZOOM_RATE = 5.0;
  public static final double MIN_ZOOM_RATE = 0.1;

  public static final double MAX_SPEED = 20;
  public static final double MIN_SPEED = 1;

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

    // Create the UI components
    Pane gridParent = createGrid();
    HBox titleLabel = createTitleLabel();
    Pane speedControl = createSpeedControl();
    Pane controls = createControls();
    Label infoLabel = createInfoLabel();

    VBox.setVgrow(gridParent, Priority.ALWAYS);
    VBox.setVgrow(infoLabel, Priority.ALWAYS);

    VBox root = new VBox(10,
        titleLabel,
        gridParent,
        speedControl,
        controls,
        infoLabel
    );
    root.getStylesheets().add(getClass().getResource(STYLE_PATH).toExternalForm());

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
      timeSinceLastUpdate = 0.0;
    }
  }

  /* PRIVATE UI SETUP METHODS */

  private HBox createTitleLabel() {
    titleLabel = new Label("Simulation Title");
    titleLabel.getStyleClass().add("title-label");

    HBox titleContainer = new HBox(titleLabel);
    titleContainer.setAlignment(javafx.geometry.Pos.CENTER);

    return titleContainer;
  }

  private Pane createGrid() {
    this.grid = new GridPane();

    Pane pane = new Pane();
    pane.setPrefSize(DEFAULT_GRID_WIDTH, DEFAULT_GRID_HEIGHT);
    pane.getStyleClass().add("grid-pane");

    // Clip the Pane to the desired size
    Rectangle clip = new Rectangle(DEFAULT_GRID_WIDTH, DEFAULT_GRID_HEIGHT);
    pane.setClip(clip);

    // Update Clip and GridPane size when the Pane size changes
    pane.widthProperty().addListener((observable, oldValue, newValue) -> {
      clip.setWidth(newValue.doubleValue());
      centerGrid();
    });
    pane.heightProperty().addListener((observable, oldValue, newValue) -> {
      clip.setHeight(newValue.doubleValue());
      centerGrid();
    });

    // Update GridPane position when the GridPane size changes
    grid.widthProperty().addListener((observable, oldValue, newValue) -> {
      centerGrid();
    });
    grid.heightProperty().addListener((observable, oldValue, newValue) -> {
      centerGrid();
    });

    pane.getChildren().add(grid);

    // Mouse and Scroll event handlers
    final double[] mouseX = new double[1];
    final double[] mouseY = new double[1];
    pane.setOnMousePressed(event -> {
      mouseX[0] = event.getSceneX() - grid.getLayoutX();
      mouseY[0] = event.getSceneY() - grid.getLayoutY();
    });
    pane.setOnMouseDragged(event -> {
      double offsetX = event.getSceneX() - mouseX[0];
      double offsetY = event.getSceneY() - mouseY[0];
      grid.relocate(offsetX, offsetY);
    });

    final double[] scale = {1.0};
    pane.setOnZoom(event -> {
      double zoomFactor = event.getZoomFactor();

      scale[0] *= zoomFactor;

      // limit the scale to reasonable values
      scale[0] = Math.max(MIN_ZOOM_RATE, Math.min(scale[0], MAX_ZOOM_RATE));

      grid.setScaleX(scale[0]);
      grid.setScaleY(scale[0]);
    });

    return pane;
  }

  private HBox createSpeedControl() {
    speedSlider = new Slider(MIN_SPEED, MAX_SPEED, (MAX_SPEED + MIN_SPEED) / 2);
    speedSlider.setMaxWidth(Double.MAX_VALUE);
    Label speedLabel = new Label("Speed: ");
    speedLabel.getStyleClass().add("speed-label");

    // Add tooltips for hover information
    Label minLabel = new Label("Min");
    Label maxLabel = new Label(" Max");
    Tooltip minTooltip = new Tooltip("Minimum speed (" + MIN_SPEED + ").");
    Tooltip maxTooltip = new Tooltip("Maximum speed (" + MAX_SPEED + ").");
    minLabel.setTooltip(minTooltip);
    maxLabel.setTooltip(maxTooltip);

    // Set slider style
    HBox speedControl = new HBox(10, speedLabel, minLabel, speedSlider, maxLabel);
    HBox.setHgrow(speedSlider, Priority.ALWAYS);
    speedControl.getStyleClass().add("speed-control");

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
    startPauseButton.getStyleClass().add("start-button");
    resetButton.getStyleClass().add("reset-button");
    loadButton.getStyleClass().add("load-button");
    saveButton.getStyleClass().add("save-button");
    directoryButton.getStyleClass().add("directory-button");

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
    infoLabel.getStyleClass().add("info-label");
    infoLabel.setMaxWidth(Double.MAX_VALUE);
    return infoLabel;
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
    toggleStartPauseButton(true);

    // Update the Speed
    speedSlider.setValue(controller.getConfigSpeed());

    // Center the grid
    centerGrid();
  }

  private void loadCallback(String filename) {
    // Load a new simulation
    if (!"None".equals(filename) && filename != null && !filename.isEmpty()) {
      // Force to pause
      toggleStartPauseButton(true);

      // Load the config file
      controller.loadConfig(filename);
      controller.resetModel();

      // Update the Speed
      speedSlider.setValue(controller.getConfigSpeed());

      // Update the UI Text
      titleLabel.setText(controller.getConfigTitle());
      infoLabel.setText(controller.getConfigInformation());

      // Center the grid
      centerGrid();
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

  /* PUBLIC UI SETS METHOD */

  public void setGrid(int numOfRows, int numOfCols) {
    SceneRenderer.drawGrid(grid, numOfRows, numOfCols);
  }

  public void setCell(int row, int col, Enum<?> state) {
    SceneRenderer.drawCell(grid, row, col, state);
  }


  /* PRIVATE UI HELPER METHODS */

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
}
