package cellsociety.view.controller;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
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
import javafx.stage.WindowEvent;

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
  private static final double DEFAULT_FLOATING_WIDTH = 200;
  private static final double DEFAULT_FLOATING_HEIGHT = 150;
  private static final double INDICATOR_INNER_SHIFT_OFFSET = 20;
  private static final double INDICATOR_OUTER_SHIFT_OFFSET = 10;

  private Stage dockIndicator;
  private final Stage mainStage;
  private final List<Stage> floatingWindows = new ArrayList<>();
  private final List<SplitPane> splitPanes = new ArrayList<>();

  private double xOffset = 0;
  private double yOffset = 0;
  private Point2D dragStartPoint = null;
  private DockPosition dockPosition = DockPosition.NONE;
  private Stage undockNewStage = null;

  /**
   * The position of the docking.
   */
  public enum DockPosition {
    NONE, LEFT, RIGHT, TOP, BOTTOM
  }

  /**
   * The floating window of the docking system.
   * Users can modify the floating window's size, position, and content.
   * The UI style of the floating window is corresponding to the default style of the docker's mainStage.
   *
   * @author Hsuan-Kai Liao
   */
  public static class DWindow {
    private final Stage floatingStage;
    private final TabPane floatingTabPane;

    private DWindow(Stage floatingStage, TabPane floatingTabPane) {
      this.floatingStage = floatingStage;
      this.floatingTabPane = floatingTabPane;
    }

    /**
     * Returns the width of the floating window.
     * @return the width of the floating window
     */
    public double getWidth() {
      return floatingStage.getWidth();
    }

    /**
     * Returns the height of the floating window.
     * @return the height of the floating window
     */
    public double getHeight() {
      return floatingStage.getHeight();
    }

    /**
     * Sets the width of the floating window.
     * @param width the width of the floating window
     */
    public void setWidth(double width) {
      floatingStage.setWidth(width);
    }

    /**
     * Sets the height of the floating window.
     * @param height the height of the floating window
     */
    public void setHeight(double height) {
      floatingStage.setHeight(height);
    }

    /**
     * Returns the x-coordinate of the floating window.
     * @return the x-coordinate of the floating window
     */
    public double getX() {
      return floatingStage.getX();
    }

    /**
     * Returns the y-coordinate of the floating window.
     * @return the y-coordinate of the floating window
     */
    public double getY() {
      return floatingStage.getY();
    }

    /**
     * Sets the x-coordinate of the floating window.
     * @param x the x-coordinate of the floating window
     */
    public void setX(double x) {
      floatingStage.setX(x);
    }

    /**
     * Sets the y-coordinate of the floating window.
     * @param y the y-coordinate of the floating window
     */

    public void setY(double y) {
      floatingStage.setY(y);
    }

    /**
     * Returns the content of the floating window.
     * @return the content Node of the floating window
     */
    public Node getContent() {
      return floatingTabPane.getTabs().getFirst().getContent();
    }

    /**
     * Sets the content of the floating window.
     * @param content the new content Node of the floating window
     */
    public void setContent(Node content) {
      floatingTabPane.getTabs().getFirst().setContent(content);
    }
  }

  /* APIS BELOW */

  /**
   * Creates a new Docker with the specified main stage and dimensions.
   * After creating a Docker, users should NOT modify the scene of the main stage.
   *
   * @param mainStage the main stage of the docking system
   */
  public Docker(Stage mainStage) {
    this.mainStage = mainStage;

    SplitPane splitPane = new SplitPane();
    splitPane.setOrientation(Orientation.HORIZONTAL);
    splitPane.setStyle("-fx-background-color: gray;");
    splitPanes.add(splitPane);

    // Set the main stage's scene
    Scene mainScene = new Scene(splitPane, mainStage.getWidth(), mainStage.getHeight());
    mainStage.setScene(mainScene);

    // Set the main stage's event listeners
    mainStage.setOnCloseRequest(event -> onMainClose());
    mainStage.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> onTagUndockedDragged(event, undockNewStage));
    mainStage.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
      onTagDropped(event, undockNewStage);
      undockNewStage = null;
    });

    createDockIndicator();

    mainStage.show();
  }

  /**
   * Returns the main docker stage of the docking system.
   *
   * @return the main docker stage
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
  public DWindow createFloatingWindow(String title, Node content, DockPosition dockPosition) {
    Stage floatingStage = new Stage();
    floatingStage.initStyle(StageStyle.UTILITY);

    // Create a TabPane to hold the content
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

    // Set the initial scene of the floating window
    Scene floatingScene = new Scene(floatingTabPane, DEFAULT_FLOATING_WIDTH, DEFAULT_FLOATING_HEIGHT);
    floatingStage.setScene(floatingScene);
    floatingScene.getRoot().applyCss();

    // Add event listeners for tabs for dragging and docking
    Node tabHeaderArea = floatingTabPane.lookup(".tab");
    if (tabHeaderArea != null) {
      tabHeaderArea.setOnMousePressed(this::onTagPressed);
      tabHeaderArea.setOnMouseDragged(event -> {
        if (isDocked(floatingTabPane)) {
          onTagDockedDragged(event, floatingStage, floatingTabPane);
        } else {
          onTagUndockedDragged(event, floatingStage);
        }
      });
    }

    // When closed, dock to the nearest side of the main window
    floatingStage.setOnCloseRequest(event -> onFloatingClose(event, floatingStage));
    floatingTabPane.setOnMouseReleased(event -> onTagDropped(event, floatingStage));

    // Initial dock check
    if (dockPosition == DockPosition.NONE) {
      floatingStage.show();
    } else {
      floatingStage.setOpacity(0);
      dockTab(floatingStage, null, dockPosition);
    }

    floatingWindows.add(floatingStage);
    return new DWindow(floatingStage, floatingTabPane);
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
      if (isMouseInsideMainScene(mouseX, mouseY)) {
        dockIndicator.setOpacity(0);
        dockIndicator.hide();
        return;
      } else {
        // Get the midpoints of the edges of the mainStage
        Point2D[] edgeMidpoints = getStageEdgeMidpoints(mainStage);
        Map.Entry<DockPosition, Point2D> nearestEdgeMidpoint = getClosestEdge(edgeMidpoints, mouseX, mouseY);
        dockPosition = nearestEdgeMidpoint.getKey();
        updateIndicatorPosition(nearestEdgeMidpoint.getValue(), dockPosition, false);

        // Set the style of the dock indicator
        dockIndicator.setOpacity(1);
        setIndicatorHighlight(isMouseInsideIndicator(mouseX, mouseY));
      }
    } else {
      // Get the midpoints of the edges of the TabPane
      Point2D[] edgeMidpoints = getTabPaneEdgeMidpoints(tp);
      Map.Entry<DockPosition, Point2D> nearestEdgeMidpoint = getClosestEdge(edgeMidpoints, mouseX, mouseY);
      dockPosition = nearestEdgeMidpoint.getKey();
      updateIndicatorPosition(nearestEdgeMidpoint.getValue(), dockPosition, true);

      // Set the style of the dock indicator
      dockIndicator.setOpacity(1);
      setIndicatorHighlight(isMouseInsideIndicator(mouseX, mouseY));
    }

    // Show the dock indicator
    if (!dockIndicator.isShowing()) {
      dockIndicator.show();
    }
  }

  private void updateIndicatorPosition(Point2D nearestEdgeMidpoint, DockPosition dockPosition, boolean inOrOutShift) {
    if (nearestEdgeMidpoint == null) return;

    double newX = nearestEdgeMidpoint.getX();
    double newY = nearestEdgeMidpoint.getY();

    double shift = inOrOutShift ? INDICATOR_INNER_SHIFT_OFFSET : -INDICATOR_OUTER_SHIFT_OFFSET;

    switch (dockPosition) {
      case LEFT:
        newX += shift;
        break;
      case RIGHT:
        newX -= shift;
        break;
      case TOP:
        newY += shift;
        if (!inOrOutShift) {
          newY += getDecorationBarHeight(mainStage);
        }
        break;
      case BOTTOM:
        newY -= shift;
        break;
    }

    // Update the position of the dock indicator
    dockIndicator.setX(newX - dockIndicator.getWidth() / 2);
    dockIndicator.setY(newY - dockIndicator.getHeight() / 2);
  }

  private void setIndicatorHighlight(boolean isHighlighted) {
    if (isHighlighted) {
      dockIndicator.getScene().getRoot().setStyle("-fx-background-color: yellow; -fx-border-color: black; -fx-border-width: 2;");
    } else {
      dockIndicator.getScene().getRoot().setStyle("-fx-background-color: rgba(0, 0, 255, 0.5); -fx-border-color: black; -fx-border-width: 2;");
    }
  }

  /* DOCKING CORE */

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
    floatingStage.show();

    undockNewStage = floatingStage;
  }

  /* CALLBACKS */

  private void onMainClose() {
    for (Stage floatingStage : floatingWindows) {
      floatingStage.close();
    }
    Platform.exit();
  }

  private void onFloatingClose(WindowEvent event, Stage floatingWindow) {
    double floatingX = floatingWindow.getX();
    double floatingY = floatingWindow.getY();
    double floatingWidth = floatingWindow.getWidth();
    double floatingHeight = floatingWindow.getHeight();

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

    dockTab(floatingWindow, null, nearestSide);

    // Prevent the window from closing
    event.consume();
  }

  private void onTagUndockedDragged(MouseEvent event, Stage floatingWindow) {
    double mouseX = event.getScreenX();
    double mouseY = event.getScreenY();

    if (floatingWindow != null) {
      floatingWindow.setX(mouseX - xOffset);
      floatingWindow.setY(mouseY - yOffset - getDecorationBarHeight(floatingWindow));

      showDockIndicator(mouseX, mouseY);
    }
  }

  private void onTagDockedDragged(MouseEvent event, Stage floatingWindow, TabPane floatingTabPane) {
    double mouseX = event.getScreenX();
    double mouseY = event.getScreenY();

    double dragOffsetX = mouseX - dragStartPoint.getX();
    double dragOffsetY = mouseY - dragStartPoint.getY();
    double dragDistance = Math.sqrt(dragOffsetX * dragOffsetX + dragOffsetY * dragOffsetY);

    Bounds bounds = floatingTabPane.localToScreen(floatingTabPane.getBoundsInLocal());
    xOffset = mouseX - bounds.getMinX() - dragOffsetX;
    yOffset = mouseY - bounds.getMinY() - dragOffsetY;

    floatingWindow.setX(mouseX - xOffset);
    floatingWindow.setY(mouseY - yOffset);

    if (dragDistance > UNDOCK_MINIMUM_DISTANCE) {
      undockTab(floatingTabPane, floatingWindow);
    }
  }

  private void onTagDropped(MouseEvent event, Stage floatingWindow) {
    if (floatingWindow != null) {
      double mouseX = event.getScreenX();
      double mouseY = event.getScreenY();

      if (isMouseInsideIndicator(mouseX, mouseY)) {
        TabPane targetTabPane = findTabPaneUnderMouse(mouseX, mouseY);

        if (targetTabPane != null) {
          dockTab(floatingWindow, targetTabPane, this.dockPosition);
        } else if (!isMouseInsideMainScene(mouseX, mouseY)) {
          dockTab(floatingWindow, null, this.dockPosition);
        }
      }

      dockIndicator.setOpacity(0);
      dockIndicator.hide();
    }
  }

  private void onTagPressed(MouseEvent event) {
    xOffset = event.getSceneX();
    yOffset = event.getSceneY();
    dragStartPoint = new Point2D(event.getScreenX(), event.getScreenY());
  }

  /* HELPER METHODS */

  private boolean isDocked(TabPane tabPane) {
    for (SplitPane pane : splitPanes) {
      if (pane.getItems().contains(tabPane)) {
        return true;
      }
    }
    return false;
  }

  private boolean isMouseInsideIndicator(double mouseX, double mouseY) {
    double indicatorX = dockIndicator.getX();
    double indicatorY = dockIndicator.getY();
    double indicatorWidth = dockIndicator.getWidth();
    double indicatorHeight = dockIndicator.getHeight();

    return mouseX >= indicatorX && mouseX <= indicatorX + indicatorWidth &&
        mouseY >= indicatorY && mouseY <= indicatorY + indicatorHeight;
  }

  private boolean isMouseInsideMainScene(double mouseX, double mouseY) {
    double sceneX = mainStage.getScene().getWindow().getX();
    double sceneY = mainStage.getScene().getWindow().getY();
    double sceneWidth = mainStage.getScene().getWidth();
    double sceneHeight = mainStage.getScene().getHeight();
    double decorationBarHeight = getDecorationBarHeight(mainStage);

    return mouseX >= sceneX + DOCK_OUTSIDE_OFFSET && mouseX <= sceneX - DOCK_OUTSIDE_OFFSET + sceneWidth &&
        mouseY >= sceneY + DOCK_OUTSIDE_OFFSET + decorationBarHeight && mouseY <= sceneY - DOCK_OUTSIDE_OFFSET + sceneHeight;
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

  private void removeTabFromSplitPane(TabPane tabPane, SplitPane splitPane) {
    // Recursively search for the tab pane in the split pane
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
    double leftMidY = stageY + stageHeight / 2;  // Y-coordinate of left edge midpoint
    double rightMidY = stageY + stageHeight / 2; // Y-coordinate of right edge midpoint
    double topMidX = stageX + stageWidth / 2;  // X-coordinate of top edge midpoint
    double bottomMidX = stageX + stageWidth / 2; // X-coordinate of bottom edge midpoint

    return new Point2D[]{
        new Point2D(stageX, leftMidY),   // Left edge midpoint
        new Point2D(stageX + stageWidth, rightMidY), // Right edge midpoint
        new Point2D(topMidX, stageY),   // Top edge midpoint
        new Point2D(bottomMidX, stageY + stageHeight) // Bottom edge midpoint
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

  private double getDecorationBarHeight(Stage stage) {
    return stage.getHeight() - stage.getScene().getHeight();
  }

}
