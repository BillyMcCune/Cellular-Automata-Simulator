package cellsociety.view.scene;

import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class SimulationScene {
  // Constants
  public static final double DEFAULT_WIDTH = 600;
  public static final double DEFAULT_HEIGHT = 700;

  // Color constants
  private static final String BUTTON_START_COLOR = "#4CAF50";
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
  private Label titleLabel;
  private GridPane grid;
  private Slider speedSlider;
  private TextField directoryField;
  private Label infoLabel;

  // Instance variables
  private final Stage primaryStage;

  /**
   * Constructor for the SimulationScene class
   * @param primaryStage the primary stage of the application to display the scene
   */
  public SimulationScene(Stage primaryStage) {
    this.primaryStage = primaryStage;

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

    System.out.println("Simulation step executed with elapsedTime: " + elapsedTime);
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
    grid.setStyle("-fx-border-color: " + BORDER_COLOR + "; -fx-background-color: " + CONTROL_BG_COLOR + "; -fx-min-height: 300px; -fx-min-width: 400px;");
    return grid;
  }

  private HBox createSpeedControl() {
    speedSlider = new Slider(0, 100, 50);
    speedSlider.setMaxWidth(Double.MAX_VALUE);
    Label speedLabel = new Label("Speed: ");
    Label minLabel = new Label("Min");
    Label maxLabel = new Label("Max");
    HBox speedControl = new HBox(10, speedLabel, minLabel, speedSlider, maxLabel);
    HBox.setHgrow(speedSlider, Priority.ALWAYS);
    speedControl.setStyle("-fx-padding: 10px; -fx-background-color: " + CONTROL_BG_COLOR + "; -fx-border-color: #dddddd;");
    return speedControl;
  }

  private VBox createControls() {
    // Create buttons
    Button startPauseButton = new Button("Start");
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
    startPauseButton.setStyle("-fx-background-color: " + BUTTON_START_COLOR + "; -fx-text-fill: " + BUTTON_TEXT_COLOR + ";");
    resetButton.setStyle("-fx-background-color: " + BUTTON_RESET_COLOR + "; -fx-text-fill: " + BUTTON_TEXT_COLOR + ";");
    loadButton.setStyle("-fx-background-color: " + BUTTON_LOAD_COLOR + "; -fx-text-fill: " + BUTTON_TEXT_COLOR + ";");
    saveButton.setStyle("-fx-background-color: " + BUTTON_SAVE_COLOR + "; -fx-text-fill: " + BUTTON_TEXT_COLOR + ";");
    directoryButton.setStyle("-fx-background-color: " + BUTTON_DIRECTORY_COLOR + "; -fx-text-fill: " + BUTTON_TEXT_COLOR + ";");

    // Set ComboBox
    ComboBox<String> selectType = new ComboBox<>();
    selectType.getItems().addAll("Type 1", "Type 2", "Type 3");
    selectType.setMinHeight(buttonHeight);
    selectType.setMinWidth(button40); // 40%

    // Start/Pause button action
    startPauseButton.setOnAction(e -> {
      if ("Start".equals(startPauseButton.getText())) {
        startPauseButton.setText("Pause");
      } else {
        startPauseButton.setText("Start");
      }
    });

    // Directory selection button
    directoryButton.setOnAction(e -> {
      DirectoryChooser directoryChooser = new DirectoryChooser();
      directoryChooser.setTitle("Select Directory");
      File selectedDirectory = directoryChooser.showDialog(primaryStage);

      if (selectedDirectory != null) {
        directoryField.setText(selectedDirectory.getAbsolutePath());
      }
    });

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
    infoLabel.setStyle("-fx-border-color: " + BORDER_COLOR + "; -fx-padding: 10px; -fx-background-color: " + CONTROL_BG_COLOR + "; -fx-alignment: CENTER_LEFT;");
    infoLabel.setMaxWidth(Double.MAX_VALUE);
    return infoLabel;
  }

  /* PUBLIC UI SETS METHOD */
  public void setTitleLabelText(String text) {
    titleLabel.setText(text);
  }

  public void setInfoLabelText(String text) {
    infoLabel.setText(text);
  }
}
