package cellsociety.view.docking;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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
  private final List<DWindow> floatingWindows = new ArrayList<>();
  private final List<SplitPane> splitPanes = new ArrayList<>();

  private double xOffset = 0;
  private double yOffset = 0;
  private Point2D dragStartPoint = null;
  private DockPosition dockPosition = DockPosition.NONE;
  private DWindow undockNewWindow = null;

  /**
   * The position of the docking.
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
    mainStage.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> onTabUndockedDragged(event, undockNewWindow));
    mainStage.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
      onTabDropped(event, undockNewWindow);
      undockNewWindow = null;
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
   * Reformats the main stage to adjust the divider positions of the split panes.
   */
  public void reFormat() {
    // Recursively reformat SplitPanes
    Consumer<SplitPane> recursiveReformat = new Consumer<SplitPane>() {
      @Override
      public void accept(SplitPane splitPane) {
        int size = splitPane.getItems().size();
        if (size < 2) return;

        double[] positions = new double[size - 1];
        for (int i = 0; i < positions.length; i++) {
          positions[i] = (i + 1) / (double) size;
        }
        splitPane.setDividerPositions(positions);

        // Recursively divide child SplitPanes
        for (Node node : splitPane.getItems()) {
          if (node instanceof SplitPane childSplitPane) {
            accept(childSplitPane);
          }
        }
      }
    };

    // Get the main SplitPane
    SplitPane mainSplitPane = (SplitPane) mainStage.getScene().getRoot();
    recursiveReformat.accept(mainSplitPane);
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

    // Create a DWindow object
    DWindow dWindow = new DWindow(floatingStage, floatingTabPane);

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
      tabHeaderArea.setOnMousePressed(this::onTabPressed);
      tabHeaderArea.setOnMouseDragged(event -> {
        if (isDocked(floatingTabPane)) {
          onTabDockedDragged(event, dWindow);
        } else {
          onTabUndockedDragged(event, dWindow);
        }
      });
    }

    // When closed, dock to the nearest side of the main window
    floatingStage.setOnCloseRequest(event -> onFloatingClose(event, dWindow));
    floatingTabPane.setOnMouseReleased(event -> onTabDropped(event, dWindow));

    // Initial dock check
    if (dockPosition == DockPosition.NONE) {
      floatingStage.show();
    } else {
      floatingStage.setOpacity(0);
      dockTab(dWindow, null, dockPosition);
    }

    floatingWindows.add(dWindow);
    return dWindow;
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

  private void dockTab(DWindow dWindow, TabPane destTabPane, DockPosition dockPosition) {
    // Add the floating TabPane to the target TabPane
    addTabToDocker(dWindow.floatingTabPane, destTabPane, dockPosition);

    // Hide the floating stage
    dWindow.floatingStage.setScene(null);
    dWindow.floatingStage.setOpacity(0);
    dWindow.floatingStage.hide();

    // Call the onDockEvent
    if (dWindow.onDockEvent != null) {
      dWindow.onDockEvent.handle(null);
    }
  }

  private void undockTab(DWindow dWindow) {
    // Undock the TabPane from the Docker
    TabPane floatingTabPane = dWindow.floatingTabPane;
    Stage floatingWindow = dWindow.floatingStage;

    // Get the current position of the floating window
    double height = floatingTabPane.getHeight();
    double width = floatingTabPane.getWidth();

    // Remove the TabPane from the Docker
    removeTabFromDocker(floatingTabPane);
    collapseSplitPanes();

    // Set the scene of the floating window
    Scene newScene = new Scene(floatingTabPane);
    newScene.getStylesheets().setAll(mainStage.getScene().getStylesheets());
    floatingWindow.setScene(newScene);
    floatingWindow.setWidth(width);
    floatingWindow.setHeight(height);
    floatingWindow.setOpacity(1);
    floatingWindow.show();
    undockNewWindow = dWindow;

    // Call the onUndockEvent
    if (dWindow.onUndockEvent != null) {
      dWindow.onUndockEvent.handle(null);
    }
  }

  /* CALLBACKS */

  private void onMainClose() {
    for (DWindow dWindow : floatingWindows) {
      dWindow.floatingStage.close();
    }
    Platform.exit();
  }

  private void onFloatingClose(WindowEvent event, DWindow dWindow) {
    double floatingX = dWindow.floatingStage.getX();
    double floatingY = dWindow.floatingStage.getY();
    double floatingWidth = dWindow.floatingStage.getWidth();
    double floatingHeight = dWindow.floatingStage.getHeight();

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

    dockTab(dWindow, null, nearestSide);

    // Prevent the window from closing
    event.consume();
  }

  private void onTabUndockedDragged(MouseEvent event, DWindow dWindow) {
    double mouseX = event.getScreenX();
    double mouseY = event.getScreenY();

    if (dWindow != null) {
      dWindow.floatingStage.setX(mouseX - xOffset);
      dWindow.floatingStage.setY(mouseY - yOffset - getDecorationBarHeight(dWindow.floatingStage));

      showDockIndicator(mouseX, mouseY);
    }
  }

  private void onTabDockedDragged(MouseEvent event, DWindow dWindow) {
    double mouseX = event.getScreenX();
    double mouseY = event.getScreenY();

    double dragOffsetX = mouseX - dragStartPoint.getX();
    double dragOffsetY = mouseY - dragStartPoint.getY();
    double dragDistance = Math.sqrt(dragOffsetX * dragOffsetX + dragOffsetY * dragOffsetY);

    Bounds bounds = dWindow.floatingTabPane.localToScreen(dWindow.floatingTabPane.getBoundsInLocal());
    xOffset = mouseX - bounds.getMinX() - dragOffsetX;
    yOffset = mouseY - bounds.getMinY() - dragOffsetY;

    dWindow.floatingStage.setX(mouseX - xOffset);
    dWindow.floatingStage.setY(mouseY - yOffset);

    if (dragDistance > UNDOCK_MINIMUM_DISTANCE) {
      undockTab(dWindow);
    }
  }

  private void onTabDropped(MouseEvent event, DWindow dWindow) {
    if (dWindow != null) {
      double mouseX = event.getScreenX();
      double mouseY = event.getScreenY();

      if (isMouseInsideIndicator(mouseX, mouseY)) {
        TabPane targetTabPane = findTabPaneUnderMouse(mouseX, mouseY);

        if (targetTabPane != null) {
          dockTab(dWindow, targetTabPane, this.dockPosition);
        } else if (!isMouseInsideMainScene(mouseX, mouseY)) {
          dockTab(dWindow, null, this.dockPosition);
        }
      }

      dockIndicator.setOpacity(0);
      dockIndicator.hide();
    }
  }

  private void onTabPressed(MouseEvent event) {
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

  private SplitPane findParentSplitPane(Node child) {
    for (SplitPane parent : splitPanes) {
      if (parent.getItems().contains(child)) {
        return parent;
      }
    }
    return null;
  }

  private void addTabToDocker(TabPane srcTabPane, TabPane destTabPane, DockPosition dockPosition) {
    // Get splitPanes
    SplitPane targetSplitPane = findParentSplitPane(destTabPane);
    targetSplitPane = (destTabPane == null) ? (SplitPane) mainStage.getScene().getRoot() : targetSplitPane == null ? new SplitPane() : targetSplitPane;

    // Initial divider positions
    double[] originalPositions = targetSplitPane.getDividerPositions();
    double[] newPositions = new double[originalPositions.length + 1];

    // Check dock position
    boolean isHorizontal = (dockPosition == DockPosition.LEFT || dockPosition == DockPosition.RIGHT);
    boolean shouldFrontInsert = (dockPosition == DockPosition.LEFT || dockPosition == DockPosition.TOP);
    boolean shouldInsertDirectly = targetSplitPane.getOrientation() == (isHorizontal ? Orientation.HORIZONTAL : Orientation.VERTICAL)
        && !targetSplitPane.getItems().isEmpty();

    // Update Index
    int index = (destTabPane == null) ? -1 : targetSplitPane.getItems().indexOf(destTabPane);
    index = (index != -1) ? index : shouldFrontInsert ? 0 : targetSplitPane.getItems().size() - 1;

    // Create a new SplitPane if the target SplitPane is not empty
    SplitPane newSplitPane = new SplitPane();
    newSplitPane.setStyle("-fx-background-color: gray;");

    // Check if the target SplitPane is empty
    if (shouldInsertDirectly) {
      targetSplitPane.getItems().add(shouldFrontInsert ? index : index + 1, srcTabPane);

      // New divider positions
      if (originalPositions.length != 0) {
        System.arraycopy(originalPositions, 0, newPositions, 0, index);
        double newDividerPosition = (index == 0) ? originalPositions[0] / 2.0 : (index == originalPositions.length) ? (originalPositions[index - 1] + 1.0) / 2.0 : (originalPositions[index - 1] + originalPositions[index]) / 2.0;
        newPositions[index] = newDividerPosition;
        System.arraycopy(originalPositions, index, newPositions, index + 1, originalPositions.length - index);
        targetSplitPane.setDividerPositions(newPositions);
      }
    } else {
      newSplitPane.setOrientation(isHorizontal ? Orientation.HORIZONTAL : Orientation.VERTICAL);
      newSplitPane.getItems().add(srcTabPane);

      if (destTabPane != null) {
        newSplitPane.getItems().add(shouldFrontInsert ? 1 : 0, destTabPane);
        if (!targetSplitPane.getItems().isEmpty()) {
          targetSplitPane.getItems().set(index, newSplitPane);
        }
      } else {
        newSplitPane.getItems().add(shouldFrontInsert ? 1 : 0, targetSplitPane);
      }

      // New divider positions
      if (originalPositions.length != 0) {
        targetSplitPane.setDividerPositions(originalPositions);
      }
    }

    // Remove the old SplitPane if it is empty
    if (targetSplitPane.getItems().isEmpty()) {
      newSplitPane.getItems().remove(targetSplitPane);
      splitPanes.remove(targetSplitPane);
    }

    // Set the new root of the main stage
    if (!newSplitPane.getItems().isEmpty()) {
      splitPanes.add(newSplitPane);
      if (destTabPane == null) {
        mainStage.getScene().setRoot(newSplitPane);
      }
    }
  }

  private void removeTabFromDocker(TabPane tabPane) {
    for (SplitPane splitPane : splitPanes) {
      if (splitPane.getItems().contains(tabPane)) {
        // Store the original positions and count
        double[] originalPositions = splitPane.getDividerPositions();
        int originalCount = splitPane.getItems().size();

        // Remove the tab pane from the split pane
        int removedIndex = splitPane.getItems().indexOf(tabPane);
        splitPane.getItems().remove(tabPane);

        // Restore the divider positions
        if (originalCount > 1) {
          double[] newPositions = new double[originalPositions.length - 1];
          for (int i = 0; i < newPositions.length; i++) {
            if (i < removedIndex - 1) {
              newPositions[i] = originalPositions[i];
            } else if (i == removedIndex - 1) {
              newPositions[i] = (originalPositions[i] + originalPositions[i + 1]) / 2;
            } else {
              newPositions[i] = originalPositions[i + 1];
            }
          }

          splitPane.setDividerPositions(newPositions);
        }
        return;
      }
    }
  }

  private void collapseSplitPanes() {
    // Recursively collapse SplitPanes
    Consumer<SplitPane> recursiveCollapse = new Consumer<SplitPane>() {
      @Override
      public void accept(SplitPane splitPane) {
        // Empty checks and collapsing single-child SplitPanes
        List<SplitPane> emptySplitPanes = new ArrayList<>();
        for (Node node : splitPane.getItems()) {
          if (!(node instanceof SplitPane childSplitPane)) {
            continue;
          }

          // Recursively collapse child SplitPanes
          accept(childSplitPane);

          // If the SplitPane is empty, mark it for removal
          if (childSplitPane.getItems().isEmpty()) {
            emptySplitPanes.add(childSplitPane);
          }

          // If the SplitPane has only one child, then promote that child
          else if (childSplitPane.getItems().size() == 1) {
            Node child = childSplitPane.getItems().getFirst();
            SplitPane parentSplitPane = findParentSplitPane(childSplitPane);

            // Restore the parent divider positions
            if (parentSplitPane != null) {
              int index = parentSplitPane.getItems().indexOf(childSplitPane);
              if (index != -1) {
                double[] parentDividerPositions = parentSplitPane.getDividerPositions();
                childSplitPane.getItems().remove(child);
                parentSplitPane.getItems().set(index, child);
                parentSplitPane.setDividerPositions(parentDividerPositions);

                emptySplitPanes.add(childSplitPane);
              }
            } else {
              childSplitPane.getItems().remove(child);
              mainStage.getScene().setRoot((SplitPane) child);
              emptySplitPanes.add(childSplitPane);
            }
          }
        }
        splitPanes.removeAll(emptySplitPanes);
        splitPane.getItems().removeAll(emptySplitPanes);
      }
    };

    // Get the main SplitPane
    SplitPane mainSplitPane = (SplitPane) mainStage.getScene().getRoot();
    recursiveCollapse.accept(mainSplitPane);

    // Promote the child SplitPane if the main SplitPane has only one child
    if (mainSplitPane.getItems().size() == 1 && mainSplitPane.getItems().getFirst() instanceof SplitPane childSplitPane) {
      mainSplitPane.setOrientation(childSplitPane.getOrientation());
      mainSplitPane.getItems().setAll(childSplitPane.getItems());
      mainSplitPane.setDividerPositions(childSplitPane.getDividerPositions());
      childSplitPane.getItems().clear();
      splitPanes.remove(childSplitPane);
    }
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
