package viewtests;

import cellsociety.view.docking.DWindow;
import cellsociety.view.docking.Docker;
import cellsociety.view.docking.Docker.DockPosition;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import util.DukeApplicationTest;

public class DockingTest extends DukeApplicationTest {
  private static final String DUMMY_WINDOW_NAME = "DUMMY DWindow";
  private static final Rectangle DUMMY_RECTANGLE = new Rectangle();

  private Stage mainStage;
  private Docker docker;
  private DWindow dWindowTest;

  @Override
  public void start(Stage primaryStage) {
    mainStage = primaryStage;
    docker = new Docker(mainStage);

    dWindowTest = docker.createDWindow(
        new SimpleStringProperty(DUMMY_WINDOW_NAME),
        DUMMY_RECTANGLE,
        DockPosition.TOP,
        null
    );
  }

  @Test
  public void Docker_CreateDWindowNullDocking() {
    runAsJFXAction(() -> {
      docker.createDWindow(
          new SimpleStringProperty(DUMMY_WINDOW_NAME),
          DUMMY_RECTANGLE,
          DockPosition.NONE,
          null
      );
    });
  }

  @Test
  public void Docker_CreateDWindowTopDocking() {
    runAsJFXAction(() -> {
      docker.createDWindow(
          new SimpleStringProperty(DUMMY_WINDOW_NAME),
          DUMMY_RECTANGLE,
          DockPosition.TOP,
          null
      );
    });
  }

  @Test
  public void Docker_CreateDWindowBottomDocking() {
    runAsJFXAction(() -> {
      docker.createDWindow(
          new SimpleStringProperty(DUMMY_WINDOW_NAME),
          DUMMY_RECTANGLE,
          DockPosition.BOTTOM,
          null
      );
    });
  }

  @Test
  public void Docker_CreateDWindowLeftDocking() {
    runAsJFXAction(() -> {
      docker.createDWindow(
          new SimpleStringProperty(DUMMY_WINDOW_NAME),
          DUMMY_RECTANGLE,
          DockPosition.LEFT,
          null
      );
    });
  }

  @Test
  public void Docker_CreateDWindowRightDocking() {
    runAsJFXAction(() -> {
      docker.createDWindow(
          new SimpleStringProperty(DUMMY_WINDOW_NAME),
          DUMMY_RECTANGLE,
          DockPosition.RIGHT,
          null
      );
    });
  }

  @Test
  public void Docker_ReformatChildDWindow() {
    runAsJFXAction(() -> {
      docker.reformat();
    });
  }

  @Test
  public void Docker_AddAndClearStyleSheet() {
    runAsJFXAction(() -> {
      docker.clearStyleSheets();
      docker.addStyleSheet("test.css");
    });
  }

  @Test
  public void Docker_CreateDWindowTabPaneCenterDocking() {
    runAsJFXAction(() -> {
      docker.createDWindow(
          new SimpleStringProperty(DUMMY_WINDOW_NAME),
          DUMMY_RECTANGLE,
          DockPosition.CENTER,
          dWindowTest
      );
    });
  }

  @Test
  public void Docker_CreateDWindowTabPaneTopDocking() {
    runAsJFXAction(() -> {
      docker.createDWindow(
          new SimpleStringProperty(DUMMY_WINDOW_NAME),
          DUMMY_RECTANGLE,
          DockPosition.TOP,
          dWindowTest
      );
    });
  }

  @Test
  public void Docker_CreateDWindowTabPaneBottomDocking() {
    runAsJFXAction(() -> {
      docker.createDWindow(
          new SimpleStringProperty(DUMMY_WINDOW_NAME),
          DUMMY_RECTANGLE,
          DockPosition.BOTTOM,
          dWindowTest
      );
    });
  }

  @Test
  public void Docker_CreateDWindowTabPaneLeftDocking() {
    runAsJFXAction(() -> {
      docker.createDWindow(
          new SimpleStringProperty(DUMMY_WINDOW_NAME),
          DUMMY_RECTANGLE,
          DockPosition.LEFT,
          dWindowTest
      );
    });
  }

  @Test
  public void Docker_CreateDWindowTabPaneRightDocking() {
    runAsJFXAction(() -> {
      docker.createDWindow(
          new SimpleStringProperty(DUMMY_WINDOW_NAME),
          DUMMY_RECTANGLE,
          DockPosition.RIGHT,
          dWindowTest
      );
    });
  }

  @Test
  public void Docker_DockerSettingsAdjustments() {
    runAsJFXAction(() -> {
      Assertions.assertEquals(mainStage, docker.getMainStage());
      docker.setWindowOpaqueOnDragging(true);
    });
  }

}
