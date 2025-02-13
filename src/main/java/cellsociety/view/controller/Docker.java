package cellsociety.view.controller;

import java.awt.MouseInfo;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * A docking system that allows users to dock and undock floating windows to the main stage.
 *
 * @author Hsuan-Kai Liao
 */
public class Docker {
  private static final int DOCK_OUTSIDE_OFFSET = 10;
  private static final int DOCK_INDICATOR_WIDTH = 40;
  private static final int DOCK_INDICATOR_HEIGHT = 40;
  private static final int UNDOCK_MINIMUM_DISTANCE = 20;
  private static final int UNDOCK_FOLLOW_FRAME_RATE = 60;
  private static final double DEFAULT_FLOATING_WIDTH = 200;
  private static final double DEFAULT_FLOATING_HEIGHT = 150;

  private Stage dockIndicator;
  private final Stage mainStage;
  private final List<Stage> floatingWindows = new ArrayList<>();
  private final List<SplitPane> splitPanes = new ArrayList<>();

  private double xOffset = 0;
  private double yOffset = 0;
  private Point2D dragStartPoint = null;
  private DockPosition dockPosition = DockPosition.NONE;

  private final Timeline undockStageFollowUpdate = new Timeline();
  private Stage undockNewStage = null;

  /**
   * The position of the dock indicator.
   */
  public enum DockPosition {
    NONE, LEFT, RIGHT, TOP, BOTTOM
  }

  /* APIS BELOW */

  /**
   * Creates a new Docker with the specified main stage and dimensions.
   * After creating a Docker, users should NOT modify the scene of the main stage.
   *
   * @param mainStage the main stage of the docking system
   * @param width the width of the main stage
   * @param height the height of the main stage
   */
  public Docker(Stage mainStage, double width, double height) {
    this.mainStage = mainStage;
    mainStage.show();

    SplitPane splitPane = new SplitPane();
    splitPane.setOrientation(Orientation.HORIZONTAL);
    splitPane.setStyle("-fx-background-color: gray;");
    splitPanes.add(splitPane);

    Scene mainScene = new Scene(splitPane, width, height);
    mainStage.setScene(mainScene);
    mainStage.setOnCloseRequest(event -> {
      for (Stage floatingStage : floatingWindows) {
        floatingStage.close();
      }
      Platform.exit();
    });

    createDockIndicator();

    // Set up the undock stage follow update
    undockStageFollowUpdate.setCycleCount(Timeline.INDEFINITE);
    undockStageFollowUpdate.getKeyFrames().add(new javafx.animation.KeyFrame(
        javafx.util.Duration.seconds(1.0 / UNDOCK_FOLLOW_FRAME_RATE),
        event -> {
          double mouseX = MouseInfo.getPointerInfo().getLocation().getX();
          double mouseY = MouseInfo.getPointerInfo().getLocation().getY();

          undockNewStage.setX(mouseX - xOffset);
          undockNewStage.setY(mouseY - xOffset);

          if (!undockNewStage.isShowing()) {
            undockNewStage.show();
          }
        }
    ));
  }

  /**
   * Returns the main stage of the docking system.
   *
   * @return the main stage
   */
  public Stage getMainStage() {
    return mainStage;
  }

  /**
   * Creates a floating window with the specified title and content.
   *
   * @param title the title of the floating window
   * @param content the content of the floating window
   * @param dockPosition the default dock position of the floating window
   * @return the floating window
   */
  public Stage createFloatingWindow(String title, Node content, DockPosition dockPosition) {
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
        dragStartPoint = new Point2D(event.getScreenX(), event.getScreenY());
      });

      tabHeaderArea.setOnMouseDragged(event -> {
        double newX = event.getScreenX() - xOffset;
        double newY = event.getScreenY() - yOffset;

        if (isDocked(floatingTabPane)) {
          double dragDistance = Math.sqrt(Math.pow(event.getScreenX() - dragStartPoint.getX(), 2) + Math.pow(event.getScreenY() - dragStartPoint.getY(), 2));

          if (dragDistance > UNDOCK_MINIMUM_DISTANCE) {
            xOffset = event.getScreenX() - floatingTabPane.localToScreen(0, 0).getX();
            yOffset = event.getScreenY() - floatingTabPane.localToScreen(0, 0).getY();
            undockTab(floatingTabPane, floatingStage);
          }
        } else {
          floatingStage.setX(newX);
          floatingStage.setY(newY - getDecorationBarHeight(floatingStage));
          showDockIndicator(event.getScreenX(), event.getScreenY());
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
      DockPosition nearestSide = null; // Default if no side is closest
      double minDistance = Double.MAX_VALUE;

      if (distanceToLeft < minDistance) {
        minDistance = distanceToLeft;
        nearestSide = DockPosition.LEFT;
      }
      if (distanceToRight < minDistance) {
        minDistance = distanceToRight;
        nearestSide = DockPosition.RIGHT;
      }
      if (distanceToTop < minDistance) {
        minDistance = distanceToTop;
        nearestSide = DockPosition.TOP;
      }
      if (distanceToBottom < minDistance) {
        nearestSide = DockPosition.BOTTOM;
      }

      dockTab(floatingStage, null, nearestSide);

      // Prevent the window from closing
      event.consume();
    });

    floatingTabPane.setOnMouseReleased(event -> {
      double mouseX = event.getScreenX();
      double mouseY = event.getScreenY();

      if (isMouseInsideIndicator(mouseX, mouseY)) {
        TabPane targetTabPane = findTabPaneUnderMouse(mouseX, mouseY);

        if (targetTabPane != null) {
          dockTab(floatingStage, targetTabPane, this.dockPosition);
        } else if (!isMouseInsideMainStage(mouseX, mouseY)) {
          dockTab(floatingStage, null, this.dockPosition);
        }
      }

      dockIndicator.setOpacity(0);
      dockIndicator.hide();
    });

    floatingStage.setOpacity(0);
    floatingWindows.add(floatingStage);
    floatingStage.show();
    dockTab(floatingStage, null, dockPosition);

    return floatingStage;
  }

  /* INDICATOR IMPLEMENTATIONS */
  private void createDockIndicator() {
    dockIndicator = new Stage();
    dockIndicator.initStyle(StageStyle.UNDECORATED);
    dockIndicator.setAlwaysOnTop(true);
    StackPane dockRoot = new StackPane(new Label("⚓") {{
      setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
    }});
    dockRoot.setStyle("-fx-background-color: rgba(0, 0, 255, 0.5); -fx-border-color: black; -fx-border-width: 2;");
    Scene dockScene = new Scene(dockRoot, DOCK_INDICATOR_WIDTH, DOCK_INDICATOR_HEIGHT);
    dockIndicator.setScene(dockScene);
    dockIndicator.setOpacity(0);
    dockIndicator.setX(-DOCK_INDICATOR_WIDTH);
    dockIndicator.setY(-DOCK_INDICATOR_HEIGHT);
    dockIndicator.show();
  }

  private void showDockIndicator(double mouseX, double mouseY) {
    TabPane tp = findTabPaneUnderMouse(mouseX, mouseY);
    if (tp == null) {
      // Check if the mouse is outside the bounds of the mainStage
      if (!isMouseInsideMainStage(mouseX, mouseY)) {
        // Get the midpoints of the edges of the mainStage
        Point2D[] edgeMidpoints = getStageEdgeMidpoints(mainStage);
        Map.Entry<DockPosition, Point2D> nearestEdgeMidpoint = getClosestEdge(edgeMidpoints, mouseX, mouseY, mainStage);
        updateIndicatorPosition(nearestEdgeMidpoint.getValue(), mainStage, (double) DOCK_INDICATOR_WIDTH / 4, dockPosition);
        dockPosition = nearestEdgeMidpoint.getKey();

        // Set the style of the dock indicator
        dockIndicator.setOpacity(1);
        if (isMouseInsideIndicator(mouseX, mouseY)) {
          dockIndicator.getScene().getRoot().setStyle("-fx-background-color: yellow; -fx-border-color: black; -fx-border-width: 2;");
        } else {
          dockIndicator.getScene().getRoot().setStyle("-fx-background-color: rgba(0, 0, 255, 0.5); -fx-border-color: black; -fx-border-width: 2;");
        }
      } else {
        dockIndicator.setOpacity(0);
        dockIndicator.hide();
        return;
      }
    } else {
      // Get the midpoints of the edges of the TabPane
      Point2D[] edgeMidpoints = getTabPaneEdgeMidpoints(tp);
      Map.Entry<DockPosition, Point2D> nearestEdgeMidpoint = getClosestEdge(edgeMidpoints, mouseX, mouseY);
      updateIndicatorPosition(nearestEdgeMidpoint.getValue(), tp, (double) DOCK_INDICATOR_WIDTH / 2);
      dockPosition = nearestEdgeMidpoint.getKey();

      // Set the style of the dock indicator
      dockIndicator.setOpacity(1);
      if (isMouseInsideIndicator(mouseX, mouseY)) {
        dockIndicator.getScene().getRoot().setStyle("-fx-background-color: yellow; -fx-border-color: black; -fx-border-width: 2;");
      } else {
        dockIndicator.getScene().getRoot().setStyle("-fx-background-color: rgba(0, 0, 255, 0.5); -fx-border-color: black; -fx-border-width: 2;");
      }
    }

    // Show the dock indicator
    if (!dockIndicator.isShowing()) {
      dockIndicator.show();
    }
  }

  private void updateIndicatorPosition(Point2D nearestEdgeMidpoint, TabPane tabPane, double shiftAmount) {
    if (nearestEdgeMidpoint == null) return;

    Bounds bounds = tabPane.localToScreen(tabPane.getBoundsInLocal());

    double leftX = bounds.getMinX();
    double rightX = bounds.getMaxX();
    double topY = bounds.getMinY();
    double bottomY = bounds.getMaxY();

    double newX = nearestEdgeMidpoint.getX();
    double newY = nearestEdgeMidpoint.getY();

    if (nearestEdgeMidpoint.getX() == leftX) {
      newX += shiftAmount;  // Left → Right Shift
    } else if (nearestEdgeMidpoint.getX() == rightX) {
      newX -= shiftAmount;  // Right → Left Shift
    } else if (nearestEdgeMidpoint.getY() == topY) {
      newY += shiftAmount;  // Top → Down Shift
    } else if (nearestEdgeMidpoint.getY() == bottomY) {
      newY -= shiftAmount;  // Bottom → Up Shift
    }

    // Update the position of the dock indicator
    dockIndicator.setX(newX - dockIndicator.getWidth() / 2);
    dockIndicator.setY(newY - dockIndicator.getHeight() / 2);
  }

  private void updateIndicatorPosition(Point2D edgeMidpoint, Stage stage, double shiftAmount, DockPosition dockPosition) {
    // Get the stage's position on screen
    double stageX = stage.getScene().getWindow().getX();
    double stageY = stage.getScene().getWindow().getY();

    // Initialize final indicator position
    double indicatorX = edgeMidpoint.getX();
    double indicatorY = edgeMidpoint.getY();

    // Apply shift based on the dock position
    switch (dockPosition) {
      case TOP:
        // For the top edge, shift upwards (positive Y direction)
        indicatorY -= shiftAmount;
        break;
      case BOTTOM:
        // For the bottom edge, shift downwards (negative Y direction)
        indicatorY += shiftAmount;
        break;
      case LEFT:
        // For the left edge, shift to the left (negative X direction)
        indicatorX -= shiftAmount;
        break;
      case RIGHT:
        // For the right edge, shift to the right (positive X direction)
        indicatorX += shiftAmount;
        break;
      default:
        break;
    }

    // Adjust the position to account for the stage's position on screen
    double newX = indicatorX - stageX;
    double newY = indicatorY - stageY;

    // Set the dock indicator's position, adjusting for its width/height to center it
    dockIndicator.setX(newX - dockIndicator.getWidth() / 2);
    dockIndicator.setY(newY - dockIndicator.getHeight() / 2);
  }

  private void dockTab(Stage floatingStage, TabPane targetTabPane, DockPosition dockPosition) {
    Scene floatingScene = floatingStage.getScene();
    TabPane floatingTabPane = (TabPane) floatingScene.getRoot();

    SplitPane targetSplitPane = new SplitPane();
    int index = -1;
    if (targetTabPane == null) {
      targetSplitPane = (SplitPane) mainStage.getScene().getRoot();
    } else {
      for (SplitPane splitPane : splitPanes) {
        if (splitPane.getItems().contains(targetTabPane)) {
          targetSplitPane = splitPane;
          break;
        }
      }
      index = targetSplitPane.getItems().indexOf(targetTabPane);
    }

    SplitPane newSplitPane = new SplitPane();
    newSplitPane.setStyle("-fx-background-color: gray;");

    switch (dockPosition) {
      case DockPosition.NONE:
        return;
      case DockPosition.LEFT:
        if (index == -1) {
          index = 0;
        }
        if (targetSplitPane.getOrientation() == Orientation.HORIZONTAL && !targetSplitPane.getItems().isEmpty()) {
          targetSplitPane.getItems().add(index, floatingTabPane);
        } else {
          newSplitPane.setOrientation(Orientation.HORIZONTAL);
          newSplitPane.getItems().add(floatingTabPane);

          if (targetTabPane != null) {
            newSplitPane.getItems().add(targetTabPane);
            if (!targetSplitPane.getItems().isEmpty()) {
              targetSplitPane.getItems().set(index, newSplitPane);
            }
          } else {
            newSplitPane.getItems().add(targetSplitPane);
          }

        }
        break;
      case DockPosition.RIGHT:
        if (index == -1) {
          index = targetSplitPane.getItems().size() - 1;
        }
        if (targetSplitPane.getOrientation() == Orientation.HORIZONTAL && !targetSplitPane.getItems().isEmpty()) {
          targetSplitPane.getItems().add(index + 1, floatingTabPane);
        } else {
          newSplitPane.setOrientation(Orientation.HORIZONTAL);
          if (targetTabPane != null) {
            newSplitPane.getItems().add(targetTabPane);
            if (!targetSplitPane.getItems().isEmpty()) {
              targetSplitPane.getItems().set(index, newSplitPane);
            }
          } else {
            newSplitPane.getItems().add(targetSplitPane);
          }

          newSplitPane.getItems().add(floatingTabPane);
        }
        break;
      case DockPosition.TOP:
        if (index == -1) {
          index = 0;
        }
        if (targetSplitPane.getOrientation() == Orientation.VERTICAL && !targetSplitPane.getItems().isEmpty()) {
          targetSplitPane.getItems().add(index, floatingTabPane);
        } else {
          newSplitPane.setOrientation(Orientation.VERTICAL);
          newSplitPane.getItems().add(floatingTabPane);

          if (targetTabPane != null) {
            newSplitPane.getItems().add(targetTabPane);
            if (!targetSplitPane.getItems().isEmpty()) {
              targetSplitPane.getItems().set(index, newSplitPane);
            }
          } else {
            newSplitPane.getItems().add(targetSplitPane);
          }
        }
        break;
      case DockPosition.BOTTOM:
        if (index == -1) {
          index = targetSplitPane.getItems().size() - 1;
        }
        if (targetSplitPane.getOrientation() == Orientation.VERTICAL && !targetSplitPane.getItems().isEmpty()) {
          targetSplitPane.getItems().add(index + 1, floatingTabPane);
        } else {
          newSplitPane.setOrientation(Orientation.VERTICAL);

          if (targetTabPane != null) {
            newSplitPane.getItems().add(targetTabPane);
            if (!targetSplitPane.getItems().isEmpty()) {
              targetSplitPane.getItems().set(index, newSplitPane);
            }
          } else {
            newSplitPane.getItems().add(targetSplitPane);
          }

          newSplitPane.getItems().add(floatingTabPane);
        }
        break;
    }

    // Remove the old SplitPane if it is empty
    if (targetSplitPane.getItems().isEmpty()) {
      newSplitPane.getItems().remove(targetSplitPane);
      splitPanes.remove(targetSplitPane);
    }

    // Set the divider positions
    double newDividerPosition = 1.0 / newSplitPane.getItems().size();
    for (int i = 1; i < newSplitPane.getItems().size(); i++) {
      newSplitPane.setDividerPosition(i - 1, newDividerPosition * i);
    }
    newDividerPosition = 1.0 / targetSplitPane.getItems().size();
    for (int i = 1; i < targetSplitPane.getItems().size(); i++) {
      targetSplitPane.setDividerPosition(i - 1, newDividerPosition * i);
    }

    // Get the parent SplitPane of the target SplitPane
    SplitPane parentSplitPane = null;
    for (SplitPane splitPane : splitPanes) {
      if (splitPane.getItems().contains(targetSplitPane)) {
        parentSplitPane = splitPane;
        break;
      }
    }

    // Set the new root of the main stage
    if (!newSplitPane.getItems().isEmpty()) {
      splitPanes.add(newSplitPane);
      if (parentSplitPane == null && targetTabPane == null) {
        mainStage.getScene().setRoot(newSplitPane);
      }
    }

    // Hide the floating stage
    floatingStage.setScene(null);
    floatingStage.setOpacity(0);
    floatingStage.hide();
  }

  private void undockTab(TabPane floatingTabPane, Stage floatingStage) {
    double height = floatingTabPane.getHeight();
    double width = floatingTabPane.getWidth();

    SplitPane mainSplitPane = (SplitPane) floatingTabPane.getScene().getRoot();
    removeTabFromSplitPane(floatingTabPane, mainSplitPane);

    Scene newScene = new Scene(floatingTabPane);
    newScene.getStylesheets().setAll(mainStage.getScene().getStylesheets());
    floatingStage.setScene(newScene);
    floatingStage.setWidth(width);
    floatingStage.setHeight(height);
    floatingStage.setOpacity(1);

    // Set up the undock stage follow update
    undockNewStage = floatingStage;
    undockStageFollowUpdate.play();

    // Only execute the undock for pseudo release check
    EventHandler<MouseEvent> releaseHandler = new EventHandler<>() {
      @Override
      public void handle(MouseEvent event) {
        undockStageFollowUpdate.stop();
        if (undockNewStage != null && undockNewStage.getScene() != null) {
          undockNewStage.getScene().removeEventFilter(MouseEvent.MOUSE_MOVED, this);
          undockNewStage = null;
        }
      }
    };
    floatingStage.getScene().addEventFilter(MouseEvent.MOUSE_MOVED, releaseHandler);
  }

  /* HELPER METHODS */

  private boolean isMouseInsideIndicator(double mouseX, double mouseY) {
    double indicatorX = dockIndicator.getX();
    double indicatorY = dockIndicator.getY();
    double indicatorWidth = dockIndicator.getWidth();
    double indicatorHeight = dockIndicator.getHeight();

    return mouseX >= indicatorX && mouseX <= indicatorX + indicatorWidth &&
        mouseY >= indicatorY && mouseY <= indicatorY + indicatorHeight;
  }

  private boolean isMouseInsideMainStage(double mouseX, double mouseY) {
    double stageX = mainStage.getScene().getWindow().getX();
    double stageY = mainStage.getScene().getWindow().getY();
    double stageWidth = mainStage.getWidth();
    double stageHeight = mainStage.getHeight();

    return mouseX >= stageX + DOCK_OUTSIDE_OFFSET && mouseX <= stageX - DOCK_OUTSIDE_OFFSET + stageWidth &&
        mouseY >= stageY + DOCK_OUTSIDE_OFFSET && mouseY <= stageY - DOCK_OUTSIDE_OFFSET + stageHeight;
  }

  private boolean isDocked(TabPane tabPane) {
    for (SplitPane pane : splitPanes) {
      if (pane.getItems().contains(tabPane)) {
        return true;
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

    List<SplitPane> emptySplitPanes = new ArrayList<>();
    for (Node node : splitPane.getItems()) {
      if (node instanceof SplitPane && ((SplitPane) node).getItems().isEmpty()) {
        emptySplitPanes.add((SplitPane) node);
        splitPanes.remove(node);
      }
    }
    splitPane.getItems().removeAll(emptySplitPanes);
  }

  private TabPane findTabPaneUnderMouse(double mouseX, double mouseY) {
    for (SplitPane splitPane : splitPanes) {
      for (Node node : splitPane.getItems()) {
        if (node instanceof TabPane tabPane) {
          Bounds screenBounds = tabPane.localToScreen(tabPane.getBoundsInLocal());
          if (screenBounds != null && screenBounds.contains(mouseX, mouseY)) {
            return tabPane;
          }
        }
      }
    }
    return null;
  }

  private Point2D[] getTabPaneEdgeMidpoints(TabPane tabPane) {
    Bounds bounds = tabPane.localToScreen(tabPane.getBoundsInLocal());

    double leftX = bounds.getMinX();
    double rightX = bounds.getMaxX();
    double topY = bounds.getMinY();
    double bottomY = bounds.getMaxY();

    double centerX = (leftX + rightX) / 2;
    double centerY = (topY + bottomY) / 2;

    return new Point2D[]{
        new Point2D(leftX, centerY),   // Left center
        new Point2D(rightX, centerY),  // Right center
        new Point2D(centerX, topY),    // Top center
        new Point2D(centerX, bottomY)  // Bottom center
    };
  }

  private Point2D[] getStageEdgeMidpoints(Stage stage) {
    double stageX = stage.getScene().getWindow().getX();
    double stageY = stage.getScene().getWindow().getY();
    double stageWidth = stage.getWidth();
    double stageHeight = stage.getHeight();

    // Calculate the midpoints of the stage edges
    double topMidX = stageX + stageWidth / 2;  // X-coordinate of top edge midpoint
    double bottomMidX = stageX + stageWidth / 2; // X-coordinate of bottom edge midpoint
    double leftMidY = stageY + stageHeight / 2;  // Y-coordinate of left edge midpoint
    double rightMidY = stageY + stageHeight / 2; // Y-coordinate of right edge midpoint

    return new Point2D[]{
        new Point2D(topMidX, stageY),   // Top edge midpoint
        new Point2D(bottomMidX, stageY + stageHeight), // Bottom edge midpoint
        new Point2D(stageX, leftMidY),   // Left edge midpoint
        new Point2D(stageX + stageWidth, rightMidY) // Right edge midpoint
    };
  }

  private Map.Entry<DockPosition, Point2D> getClosestEdge(Point2D[] midpoints, double mouseX, double mouseY) {
    Map<DockPosition, Point2D> edges = new HashMap<>();
    edges.put(DockPosition.LEFT, midpoints[0]);   // Left center
    edges.put(DockPosition.RIGHT, midpoints[1]);  // Right center
    edges.put(DockPosition.TOP, midpoints[2]);    // Top center
    edges.put(DockPosition.BOTTOM, midpoints[3]); // Bottom center

    DockPosition closestEdge = null;
    Point2D closestPoint = null;
    double minDistance = Double.MAX_VALUE;

    for (Map.Entry<DockPosition, Point2D> entry : edges.entrySet()) {
      double distance = entry.getValue().distance(mouseX, mouseY);
      if (distance < minDistance) {
        minDistance = distance;
        closestEdge = entry.getKey();
        closestPoint = entry.getValue();
      }
    }

    return new AbstractMap.SimpleEntry<>(closestEdge, closestPoint);
  }

  private Map.Entry<DockPosition, Point2D> getClosestEdge(Point2D[] midpoints, double mouseX, double mouseY, Stage stage) {
    // Get the stage's position on screen (screen-based coordinates)
    double stageX = stage.getScene().getWindow().getX();
    double stageY = stage.getScene().getWindow().getY();

    // Adjust the edge midpoints from stage coordinates to screen coordinates
    Point2D[] screenMidpoints = new Point2D[]{
        new Point2D(midpoints[0].getX() + stageX, midpoints[0].getY() + stageY),   // Top
        new Point2D(midpoints[1].getX() + stageX, midpoints[1].getY() + stageY),   // Bottom
        new Point2D(midpoints[2].getX() + stageX, midpoints[2].getY() + stageY),   // Left
        new Point2D(midpoints[3].getX() + stageX, midpoints[3].getY() + stageY)    // Right
    };

    Map<DockPosition, Point2D> edges = new HashMap<>();
    edges.put(DockPosition.TOP, screenMidpoints[0]);
    edges.put(DockPosition.BOTTOM, screenMidpoints[1]);
    edges.put(DockPosition.LEFT, screenMidpoints[2]);
    edges.put(DockPosition.RIGHT, screenMidpoints[3]);

    DockPosition closestEdge = null;
    Point2D closestPoint = null;
    double minDistance = Double.MAX_VALUE;

    // Find the closest edge
    for (Map.Entry<DockPosition, Point2D> entry : edges.entrySet()) {
      double distance = entry.getValue().distance(mouseX, mouseY); // Calculate screen distance
      if (distance < minDistance) {
        minDistance = distance;
        closestEdge = entry.getKey();
        closestPoint = entry.getValue();
      }
    }

    return new AbstractMap.SimpleEntry<>(closestEdge, closestPoint);
  }

  private double getDecorationBarHeight(Stage stage) {
    return stage.getHeight() - stage.getScene().getHeight();
  }

}
