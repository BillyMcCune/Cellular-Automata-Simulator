package cellsociety.view.scene;

import cellsociety.view.controller.Docker;
import cellsociety.view.controller.Docker.DockPosition;
import cellsociety.view.controller.SceneController;
import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.input.KeyEvent;
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
  public static final double DEFAULT_WIDTH = 1200;
  public static final double DEFAULT_HEIGHT = 700;

  public static final String STYLE_PATH = "/cellsociety/style/style.css";

  public static final double DEFAULT_GRID_WIDTH = 300;
  public static final double DEFAULT_GRID_HEIGHT = 300;
  public static final double MAX_ZOOM_RATE = 8.0;
  public static final double MIN_ZOOM_RATE = 0.2;

  public static final double MAX_SPEED = 100;
  public static final double MIN_SPEED = 0;
  public static final double SPEED_MULTIPLIER = 2.0;
  public static final String SPEED_TOOLTIP = "Change the speed of the simulation";

  public static final double BUTTON_WIDTH = 80;
  public static final double BUTTON_HEIGHT = 35;
  public static final double MAX_BUTTON_WIDTH = 200;

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
    ScrollPane infoLabel = createInfoLabel();
    ScrollPane parameterPanel = createParameterPanel();

    VBox.setVgrow(gridParent, Priority.ALWAYS);

    // Create a floating window for each component
    docker.createFloatingWindow("Controls", controls, DockPosition.TOP);
    docker.createFloatingWindow("Info", infoLabel, DockPosition.TOP);
    docker.createFloatingWindow("Grid", gridParent, DockPosition.RIGHT);
    docker.createFloatingWindow("Parameters", parameterPanel, DockPosition.RIGHT);

    // Set the scene style
    primaryStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource(STYLE_PATH)).toExternalForm());
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

  private ScrollPane createParameterPanel() {
    parameterBox = new VBox(5);
    parameterBox.setAlignment(Pos.TOP_CENTER);
    parameterBox.getStyleClass().add("parameter-box");
    parameterBox.setMinHeight(Region.USE_COMPUTED_SIZE);
    VBox.setVgrow(parameterBox, Priority.ALWAYS);

    Label parametersLabel = new Label("Parameters");
    parametersLabel.getStyleClass().add("parameter-title");

    // Create the parameter container
    VBox parameterContainer = new VBox(10, parametersLabel, parameterBox);
    parameterContainer.setAlignment(Pos.TOP_CENTER);
    parameterContainer.getStyleClass().add("parameter-container");
    parameterContainer.setPadding(new Insets(10));

    // Wrap the parameter container in a ScrollPane
    ScrollPane scrollPane = new ScrollPane(parameterContainer);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);

    return scrollPane;
  }

  private <T extends Number> HBox createParameter(double min, double max, T defaultValue, String label, String tooltip, Consumer<T> callback) {
    // Create the slider
    Slider slider = new Slider(min, max, defaultValue.doubleValue());
    slider.setMaxWidth(Double.MAX_VALUE);

    // Create the label with tooltip
    Label speedLabel = new Label(label + ": ");
    speedLabel.getStyleClass().add("parameter-label");
    speedLabel.setTooltip(new Tooltip(tooltip));

    // Format the default value based on the type
    String defaultText;
    String minText;
    String maxText;
    if (defaultValue instanceof Integer) {
      defaultText = String.format("%d", defaultValue.intValue());
      minText = String.format("%d", (int) min);
      maxText = String.format("%d", (int) max);
    } else {
      defaultText = String.format("%.1f", defaultValue.doubleValue());
      minText = String.format("%.1f", min);
      maxText = String.format("%.1f", max);
    }

    // Create the text input
    TextField textField = new TextField(defaultText);
    textField.getStyleClass().add("parameter-text-field");
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
    Label maxLabel = new Label(" " + maxText);

    // Create an HBox control
    HBox parameterControl = new HBox(5, speedLabel, textField, minLabel, slider, maxLabel);
    parameterControl.setAlignment(Pos.CENTER);
    HBox.setHgrow(slider, Priority.ALWAYS);
    parameterControl.getStyleClass().add("parameter-control");

    // Add slider listener
    slider.valueProperty().addListener((observable, oldValue, newValue) -> {
      T typedValue;
      if (defaultValue instanceof Integer || defaultValue instanceof Short || defaultValue instanceof Byte || defaultValue instanceof Long) {
        typedValue = (T) Integer.valueOf(newValue.intValue());
      } else {
        typedValue = (T) Double.valueOf(newValue.doubleValue());
      }

      // Update the text field with the formatted value
      textField.setText(
          (typedValue instanceof Integer)
              ? String.format("%d", typedValue.intValue())
              : String.format("%.1f", typedValue.doubleValue())
      );

      // Trigger callback
      callback.accept(typedValue);
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

          T typedValue;
          if (defaultValue instanceof Integer) {
            typedValue = (T) Integer.valueOf((int) newValue);
          } else {
            typedValue = (T) Double.valueOf(newValue);
          }

          // Sync the slider value with the text field value
          slider.setValue(newValue);

          // Trigger callback
          callback.accept(typedValue);

          // Update the text field to reflect the new value
          textField.setText(
              (typedValue instanceof Integer)
                  ? String.format("%d", typedValue.intValue())
                  : String.format("%.1f", typedValue.doubleValue())
          );
        } catch (NumberFormatException e) {
          // Reset text field to slider value if invalid input
          double sliderValue = slider.getValue();
          textField.setText(
              (defaultValue instanceof Integer)
                  ? String.format("%d", (int) sliderValue)
                  : String.format("%.1f", sliderValue)
          );
        }
      }
    });

    // Add text focus listener
    textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) { // When focus is lost
        double sliderValue = slider.getValue();
        textField.setText(
            (defaultValue instanceof Integer)
                ? String.format("%d", (int) sliderValue)
                : String.format("%.1f", sliderValue)
        );
      }
    });

    // Call the callback with the default value
    callback.accept(defaultValue);

    return parameterControl;
  }

  private ScrollPane createControls() {
    // Create title
    Label controlsTitle = new Label("Controls");
    controlsTitle.getStyleClass().add("controls-title");

    // Create buttons
    startPauseButton = new Button("Start");
    Button resetButton = new Button("Reset");
    Button loadButton = new Button("Load");
    Button saveButton = new Button("Save");
    Button directoryButton = new Button("📂");

    // FIXME: IMPLEMENT SAVE
    saveButton.setDisable(true);

    // Set start button sizes
    startPauseButton.setPrefWidth(BUTTON_WIDTH);
    startPauseButton.setPrefHeight(BUTTON_HEIGHT);
    startPauseButton.setMaxWidth(MAX_BUTTON_WIDTH);

    // Set directory button sizes
    directoryButton.setPrefWidth(BUTTON_WIDTH * 0.5);

    // Set ComboBox
    selectType = new ComboBox<>();
    selectType.getItems().addAll("None");
    selectType.setPrefWidth(BUTTON_WIDTH * 1.5);
    selectType.setMaxWidth(MAX_BUTTON_WIDTH);

    // Create directory text field
    directoryField = new TextField();
    directoryField.setPromptText("None");
    directoryField.setEditable(false);
    directoryField.setPrefWidth((BUTTON_WIDTH - 10));
    directoryField.setMaxWidth(MAX_BUTTON_WIDTH);

    // Link the width with the start button
    resetButton.prefWidthProperty().bind(startPauseButton.widthProperty());
    loadButton.prefWidthProperty().bind(startPauseButton.widthProperty());
    saveButton.prefWidthProperty().bind(startPauseButton.widthProperty());

    // Link the height with the start button
    resetButton.prefHeightProperty().bind(startPauseButton.heightProperty());
    loadButton.prefHeightProperty().bind(startPauseButton.heightProperty());
    saveButton.prefHeightProperty().bind(startPauseButton.heightProperty());
    selectType.prefHeightProperty().bind(startPauseButton.heightProperty());
    directoryButton.prefHeightProperty().bind(startPauseButton.heightProperty());
    directoryField.prefHeightProperty().bind(startPauseButton.heightProperty());

    // Style buttons with colors
    startPauseButton.getStyleClass().add("start-button");
    resetButton.getStyleClass().add("reset-button");
    loadButton.getStyleClass().add("load-button");
    saveButton.getStyleClass().add("save-button");
    directoryButton.getStyleClass().add("directory-button");

    // Button callbacks
    startPauseButton.setOnAction(e -> startPauseCallback());
    resetButton.setOnAction(e -> resetCallback());
    loadButton.setOnAction(e -> loadCallback(selectType.getValue()));
    saveButton.setOnAction(e -> saveCallback(directoryField.getText()));
    selectType.setOnMouseClicked(e -> selectDropDownCallback());
    directoryButton.setOnAction(e -> directorySelectCallback());

    // HBox formatting
    HBox firstRow = new HBox(10, startPauseButton, loadButton, selectType);
    firstRow.setAlignment(Pos.CENTER);
    firstRow.setPadding(new Insets(5));

    HBox secondRow = new HBox(10, resetButton, saveButton, directoryField, directoryButton);
    secondRow.setAlignment(Pos.CENTER);
    secondRow.setPadding(new Insets(5));

    // VBox box containing the control buttons
    VBox controlsBox = new VBox(10, firstRow, secondRow);
    controlsBox.setAlignment(Pos.CENTER);
    controlsBox.getStyleClass().add("controls-box");
    controlsBox.setPadding(new Insets(10));
    controlsBox.setMinHeight(Region.USE_COMPUTED_SIZE);
    VBox.setVgrow(controlsBox, Priority.ALWAYS);

    // VBox formatting with title
    VBox controls = new VBox(10, controlsTitle, controlsBox);
    controls.setPadding(new Insets(10));
    controls.setAlignment(Pos.TOP_CENTER);

    // ScrollPane wrapping controls
    ScrollPane scrollPane = new ScrollPane(controls);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);

    return scrollPane;
  }

  private ScrollPane createInfoLabel() {
    Label infoTitle = new Label("Information");
    infoTitle.getStyleClass().add("info-title");

    infoText = new Label();
    infoText.getStyleClass().add("info-text");
    infoText.setWrapText(true);  // Prevent text from wrapping

    VBox infoBox = new VBox(10, infoText);
    infoBox.setAlignment(Pos.TOP_CENTER);
    infoBox.getStyleClass().add("info-box");
    infoBox.setPadding(new Insets(10));
    infoBox.setMinHeight(Region.USE_COMPUTED_SIZE);
    VBox.setVgrow(infoBox, Priority.ALWAYS);

    VBox infoContainer = new VBox(10, infoTitle, infoBox);
    infoContainer.setAlignment(Pos.TOP_CENTER);
    infoContainer.getStyleClass().add("info-container");
    infoContainer.setPadding(new Insets(10));

    ScrollPane scrollPane = new ScrollPane(infoContainer);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);

    return scrollPane;
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
    updateInterval = SPEED_MULTIPLIER / speed;
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
    // Force to pause
    toggleStartPauseButton(true);

    // Reset the simulation
    controller.resetGrid();

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

      // Clear the parameter box except for the speed
      parameterBox.getChildren().clear();
      setParameter("Speed", MIN_SPEED, MAX_SPEED, controller.getConfigSpeed(), SPEED_TOOLTIP, this::speedChangeCallback);

      // Reset the simulation
      controller.resetModel();

      // Update the UI Text
      primaryStage.setTitle(controller.getConfigTitle());
      infoText.setText(controller.getConfigInformation());

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
    if (path != null && !path.isEmpty()) {
      // Force to pause
      toggleStartPauseButton(true);

      // Save the current configuration
      controller.saveConfig(path);
    }
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
  public <T extends Number> void setParameter(String label, double min, double max, T defaultValue, String tooltip, Consumer<T> callback) {
    parameterBox.getChildren().add(createParameter(min, max, defaultValue, label, tooltip, callback));
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
}
