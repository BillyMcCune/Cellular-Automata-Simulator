package cellsociety.view.docking;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * The floating window of the docking system.
 * Users can modify the floating window's size, position, and content.
 * The UI style of the floating window is corresponding to the default style of the docker's mainStage.
 *
 * @author Hsuan-Kai Liao
 */
public class DWindow {
  final Stage floatingStage;
  final TabPane floatingTabPane;

  EventHandler<ActionEvent> onDockEvent;
  EventHandler<ActionEvent> onUndockEvent;

  /**
   * Constructs a floating window with the given stage and tab pane.
   * @param floatingStage the stage of the floating window
   * @param floatingTabPane the tab pane of the floating window that contains the content
   */
  DWindow(Stage floatingStage, TabPane floatingTabPane) {
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

  /**
   * Sets the event handler for the dock event.
   * @param onDockEvent the event handler for the dock event
   */
  public void setOnDockEvent(EventHandler<ActionEvent> onDockEvent) {
    this.onDockEvent = onDockEvent;
  }

  /**
   * Sets the event handler for the undock event.
   * @param onUndockEvent the event handler for the undock event
   */
  public void setOnUndockEvent(EventHandler<ActionEvent> onUndockEvent) {
    this.onUndockEvent = onUndockEvent;
  }
}
