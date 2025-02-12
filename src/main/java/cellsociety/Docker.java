package cellsociety;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * A docking system that allows users to dock and undock floating windows to the main stage.
 *
 * @author Hsuan-Kai Liao
 */
public class Docker {
  private static final int DOCK_THRESHOLD = 40;
  private static final int DOCK_INDICATOR_WIDTH = 100;
  private static final int DOCK_INDICATOR_HEIGHT = 30;
  private static final double DEFAULT_FLOATING_WIDTH = 200;
  private static final double DEFAULT_FLOATING_HEIGHT = 150;

  private Stage dockIndicator;
  private Stage mainStage;
  private final List<Stage> floatingWindows = new ArrayList<>();

  private double xOffset = 0;
  private double yOffset = 0;

  public Docker(Stage mainStage, double width, double height) {
    SplitPane splitPane = new SplitPane();
    splitPane.setOrientation(Orientation.HORIZONTAL);
    splitPane.setStyle("-fx-background-color: gray;");

    Scene mainScene = new Scene(splitPane, width, height);
    mainStage.setScene(mainScene);

    mainStage.setOnCloseRequest(event -> {
      for (Stage floatingStage : floatingWindows) {
        floatingStage.close();
      }
    });

    createDockIndicator();

    this.mainStage = mainStage;
    mainStage.show();
  }

  public Stage getMainStage() {
    return mainStage;
  }

  public Stage createFloatingWindow(String title, Node content) {
    Stage floatingStage = new Stage();
    floatingStage.initStyle(StageStyle.UTILITY);

    TabPane floatingTabPane = new TabPane();
    Tab tab = new Tab(title);
    tab.setClosable(false);
    tab.setContent(content);
    floatingTabPane.getTabs().add(tab);

    // Adjust the size of the floating window based on the content size
    double contentWidth = content.prefWidth(-1);
    double contentHeight = content.prefHeight(-1);
    floatingStage.setWidth(contentWidth > 0 ? contentWidth : DEFAULT_FLOATING_WIDTH);
    floatingStage.setHeight(contentHeight > 0 ? contentHeight : DEFAULT_FLOATING_HEIGHT);

    Scene floatingScene = new Scene(floatingTabPane, DEFAULT_FLOATING_WIDTH, DEFAULT_FLOATING_HEIGHT);
    floatingStage.setScene(floatingScene);
    floatingScene.getRoot().applyCss();

    Node tabHeaderArea = floatingTabPane.lookup(".tab-header-area");
    if (tabHeaderArea != null) {
      tabHeaderArea.setOnMousePressed(event -> {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
      });

      tabHeaderArea.setOnMouseDragged(event -> {
        double newX = event.getScreenX() - xOffset;
        double newY = event.getScreenY() - yOffset;

        SplitPane mainSplitPane = (SplitPane) mainStage.getScene().getRoot();

        if (isDocked(floatingTabPane, mainSplitPane)) {
          undockTab(floatingTabPane, floatingStage);
        } else {
          floatingStage.setX(newX);
          floatingStage.setY(newY);
          showDockIndicator(mainStage, event.getScreenX(), event.getScreenY());
        }
      });
    }

    floatingStage.setOnCloseRequest(event -> {
      double floatingX = floatingStage.getX();
      double floatingY = floatingStage.getY();
      double floatingWidth = floatingStage.getWidth();
      double floatingHeight = floatingStage.getHeight();

      double mainX = mainStage.getX();
      double mainY = mainStage.getY();
      double mainWidth = mainStage.getWidth();
      double mainHeight = mainStage.getHeight();

      // Calculate the center of the floating window
      double floatingCenterX = floatingX + floatingWidth / 2;
      double floatingCenterY = floatingY + floatingHeight / 2;

      // Calculate the distances to each side of the main window
      double distanceToLeft = floatingCenterX - mainX;
      double distanceToRight = mainX + mainWidth - floatingCenterX;
      double distanceToTop = floatingCenterY - mainY;
      double distanceToBottom = mainY + mainHeight - floatingCenterY;

      // Determine the nearest side
      String nearestSide = "NONE"; // Default if no side is closest
      double minDistance = Double.MAX_VALUE;

      if (distanceToLeft < minDistance) {
        minDistance = distanceToLeft;
        nearestSide = "LEFT";
      }
      if (distanceToRight < minDistance) {
        minDistance = distanceToRight;
        nearestSide = "RIGHT";
      }
      if (distanceToTop < minDistance) {
        minDistance = distanceToTop;
        nearestSide = "TOP";
      }
      if (distanceToBottom < minDistance) {
        minDistance = distanceToBottom;
        nearestSide = "BOTTOM";
      }

      extractAndDockContent(floatingStage, mainStage, nearestSide);
      event.consume(); // Prevent the window from closing
    });

    floatingTabPane.setOnMouseReleased(event -> {
      double mouseX = event.getScreenX();
      double mouseY = event.getScreenY();
      double dockIndicatorX = dockIndicator.getScene().getWindow().getX();
      double dockIndicatorY = dockIndicator.getScene().getWindow().getY();

      if (mouseX >= dockIndicatorX && mouseX <= dockIndicatorX + dockIndicator.getWidth()
          && mouseY >= dockIndicatorY && mouseY <= dockIndicatorY + dockIndicator.getHeight()) {
        checkDocking(floatingStage, mainStage, mouseX, mouseY);
      }

      dockIndicator.setOpacity(0);
      dockIndicator.hide();
    });

    floatingStage.show();
    return floatingStage;
  }

  private void createDockIndicator() {
    dockIndicator = new Stage();
    dockIndicator.initStyle(StageStyle.UNDECORATED);
    dockIndicator.setAlwaysOnTop(true);
    StackPane dockRoot = new StackPane(new Label("Dock Here"));
    dockRoot.setStyle("-fx-background-color: rgba(0, 0, 255, 0.5); -fx-border-color: black; -fx-border-width: 2;");
    Scene dockScene = new Scene(dockRoot, DOCK_INDICATOR_WIDTH, DOCK_INDICATOR_HEIGHT);
    dockIndicator.setScene(dockScene);
    dockIndicator.setOpacity(0);
  }

  private void showDockIndicator(Stage mainStage, double mouseX, double mouseY) {
    double mainX = mainStage.getX();
    double mainY = mainStage.getY();
    double mainWidth = mainStage.getWidth();
    double mainHeight = mainStage.getHeight();

    boolean showIndicator = false;

    // Left Dock
    if (Math.abs(mouseX - mainX) < DOCK_THRESHOLD) {
      dockIndicator.setX(mainX - 50);
      dockIndicator.setY(mainY + mainHeight / 2 - 15);
      showIndicator = true;
    }
    // Right Dock
    else if (Math.abs(mouseX - (mainX + mainWidth)) < DOCK_THRESHOLD) {
      dockIndicator.setX(mainX + mainWidth - 50);
      dockIndicator.setY(mainY + mainHeight / 2 - 15);
      showIndicator = true;
    }
    // Top Dock
    if (Math.abs(mouseY - mainY) < DOCK_THRESHOLD) {
      dockIndicator.setX(mainX + mainWidth / 2 - 50);
      dockIndicator.setY(mainY - 30);
      showIndicator = true;
    }
    // Bottom Dock
    else if (Math.abs(mouseY - (mainY + mainHeight)) < DOCK_THRESHOLD) {
      dockIndicator.setX(mainX + mainWidth / 2 - 50);
      dockIndicator.setY(mainY + mainHeight);
      showIndicator = true;
    }

    if (showIndicator) {
      dockIndicator.setOpacity(1);

      double indicatorX = dockIndicator.getX();
      double indicatorY = dockIndicator.getY();
      double indicatorWidth = dockIndicator.getWidth();
      double indicatorHeight = dockIndicator.getHeight();

      // Highlight the dock indicator if the mouse is over it
      if (mouseX >= indicatorX && mouseX <= indicatorX + indicatorWidth &&
          mouseY >= indicatorY && mouseY <= indicatorY + indicatorHeight) {
        dockIndicator.getScene().getRoot().setStyle("-fx-background-color: yellow; -fx-border-color: black; -fx-border-width: 2;");
      } else {
        dockIndicator.getScene().getRoot().setStyle("-fx-background-color: rgba(0, 0, 255, 0.5); -fx-border-color: black; -fx-border-width: 2;");
      }

      if (!dockIndicator.isShowing()) {
        dockIndicator.show();
      }
    } else {
      dockIndicator.setOpacity(0);
      dockIndicator.hide();
    }
  }

  private void checkDocking(Stage floatingStage, Stage mainStage, double mouseX, double mouseY) {
    double mainX = mainStage.getX();
    double mainY = mainStage.getY();
    double mainWidth = mainStage.getWidth();
    double mainHeight = mainStage.getHeight();

    String dockPosition = null;

    if (Math.abs(mouseX - mainX) < DOCK_THRESHOLD) {
      dockPosition = "LEFT";
    } else if (Math.abs(mouseX - (mainX + mainWidth)) < DOCK_THRESHOLD) {
      dockPosition = "RIGHT";
    } else if (Math.abs(mouseY - mainY) < DOCK_THRESHOLD) {
      dockPosition = "TOP";
    } else if (Math.abs(mouseY - (mainY + mainHeight)) < DOCK_THRESHOLD) {
      dockPosition = "BOTTOM";
    }

    if (dockPosition != null) {
      extractAndDockContent(floatingStage, mainStage, dockPosition);
    }
  }

  private void undockTab(TabPane floatingTabPane, Stage floatingStage) {
    SplitPane mainSplitPane = (SplitPane) floatingTabPane.getScene().getRoot();
    removeTabFromSplitPane(floatingTabPane, mainSplitPane);

    Scene newScene = new Scene(floatingTabPane);
    floatingStage.setScene(newScene);
    floatingStage.setX(mainStage.getX() + mainStage.getWidth() / 2 - floatingStage.getWidth() / 2);
    floatingStage.setY(mainStage.getY() + mainStage.getHeight() / 2 - floatingStage.getHeight() / 2);
    floatingStage.show();
  }


  private void extractAndDockContent(Stage floatingStage, Stage mainStage, String dockPosition) {
    Scene floatingScene = floatingStage.getScene();
    TabPane floatingTabPane = (TabPane) floatingScene.getRoot();
    SplitPane splitPane = (SplitPane) mainStage.getScene().getRoot();
    SplitPane newSplitPane = new SplitPane();
    newSplitPane.setStyle("-fx-background-color: gray;");

    switch (dockPosition) {
      case "LEFT":
        if (splitPane.getOrientation() == Orientation.HORIZONTAL && !splitPane.getItems().isEmpty()) {
          splitPane.getItems().addFirst(floatingTabPane);
        } else {
          newSplitPane.setOrientation(Orientation.HORIZONTAL);
          newSplitPane.getItems().add(floatingTabPane);
          newSplitPane.getItems().add(splitPane);
        }
        break;
      case "RIGHT":
        if (splitPane.getOrientation() == Orientation.HORIZONTAL && !splitPane.getItems().isEmpty()) {
          splitPane.getItems().add(floatingTabPane);
        } else {
          newSplitPane.setOrientation(Orientation.HORIZONTAL);
          newSplitPane.getItems().add(splitPane);
          newSplitPane.getItems().add(floatingTabPane);
        }
        break;
      case "TOP":
        if (splitPane.getOrientation() == Orientation.VERTICAL && !splitPane.getItems().isEmpty()) {
          splitPane.getItems().addFirst(floatingTabPane);
        } else {
          newSplitPane.setOrientation(Orientation.VERTICAL);
          newSplitPane.getItems().add(floatingTabPane);
          newSplitPane.getItems().add(splitPane);
        }
        break;
      case "BOTTOM":
        if (splitPane.getOrientation() == Orientation.VERTICAL && !splitPane.getItems().isEmpty()) {
          splitPane.getItems().add(floatingTabPane);
        } else {
          newSplitPane.setOrientation(Orientation.VERTICAL);
          newSplitPane.getItems().add(splitPane);
          newSplitPane.getItems().add(floatingTabPane);
        }
        break;
    }

    // Remove the old SplitPane if it is empty
    if (splitPane.getItems().isEmpty()) {
      newSplitPane.getItems().remove(splitPane);
    }

    // Set the divider positions
    double newDividerPosition = 1.0 / newSplitPane.getItems().size();
    for (int i = 1; i < newSplitPane.getItems().size(); i++) {
      newSplitPane.setDividerPosition(i - 1, newDividerPosition * i);
    }
    newDividerPosition = 1.0 / splitPane.getItems().size();
    for (int i = 1; i < splitPane.getItems().size(); i++) {
      splitPane.setDividerPosition(i - 1, newDividerPosition * i);
    }

    // Set the new root of the main stage
    if (newSplitPane.getItems().isEmpty()) {
      mainStage.getScene().setRoot(splitPane);
    } else {
      mainStage.getScene().setRoot(newSplitPane);
    }

    // Hide the floating stage
    floatingStage.hide();
    floatingStage.setScene(null);
  }

  private boolean isDocked(TabPane tabPane, SplitPane splitPane) {
    for (javafx.scene.Node node : splitPane.getItems()) {
      if (node instanceof TabPane && node == tabPane) {
        return true;
      }

      if (node instanceof SplitPane) {
        if (isDocked(tabPane, (SplitPane) node)) {
          return true;
        }
      }
    }
    return false;
  }

  private void removeTabFromSplitPane(TabPane tabPane, SplitPane splitPane) {
    for (Node node : splitPane.getItems()) {
      if (node instanceof TabPane && node == tabPane) {
        splitPane.getItems().remove(node);
        break;
      }

      if (node instanceof SplitPane) {
        removeTabFromSplitPane(tabPane, (SplitPane) node);
      }
    }

    if (splitPane.getItems().isEmpty() && splitPane.getParent() != null) {
      splitPane.getParent().getChildrenUnmodifiable().remove(splitPane);
    }
  }
}
