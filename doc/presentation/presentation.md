---
marp: true
theme: uncover
paginate: true
class: invert
---

# Cell Society

TEAM 1 - Jacob You, Billy McCune, Hsuan-Kai Liao

---

## General project goals:

* Develop cellular automata that function as expected
* Build a customizable and user-friendly GUI
* Utilize smart design to encourage extensibility and flexibility
* Comprehensive error handling and logging

---

# The View

![w:800 h:480 Screenshot of the view in its default state](/doc/presentation/images/view.png)

---

## General Goals for View

* The interface is clean and visually appealing,
  * Good UX, intuitive, and easy to understand
* A modular widget system, where most major components are created through 
  `SceneUIWidgetFactory` and have independent unit tests.
* Callbacks are completely separated from the model, with communication between callbacks and APIs 
  handled through SceneController

---

# DEMO

---

## Stylizing with CSS and properties

![Screenshot of the Grid In Day and Dark Themes](/doc/presentation/images/theme.png)

---

### Theme Css

Most components in the interface support theme customization. We use CSS files to classify different
themes. Below is an example of the minimaps:
```css
/* DAY */
.mini-map-pane {
  -fx-background-color: #d1d1d1;
  -fx-border-color: #020202;
}
/* DARK */
.mini-map-pane {
  -fx-background-color: #333333;
  -fx-border-color: #9e9e9e;
  -fx-border-width: 3px;
}
/* MYSTERY */
.mini-map-pane {
  -fx-background-color: #333333;
  -fx-border-color: #583c85;
  -fx-border-width: 3px;
}
```
---

### Language Properties

The language is determined by looking up keys stored in property files.
```properties
# English Property Example
controls-panel=Controls
parameters-panel=Parameters
info-panel=Information
log-panel=Log
styles-panel=Styles
colors-panel=Colors
```
The implementation involves
a LanguageController, which creates a separate StringProperty for each key in these files.
All UI components that require text are then bound to the corresponding StringProperty.


```java
// Initialize the String Properties
public LanguageEnumConstructor() {
  for (String key : properties.stringPropertyNames()) {
    translation.put(key, properties.getProperty(key));

    if (!translations.containsKey(key)) {
      translations.put(key, new SimpleStringProperty("??")); // Unmatched Keys
    }
  }
}

// Switch the language
public static void switchLanguage(Language lang) {
  for (Map.Entry<String, StringProperty> entry : translations.entrySet()) {
    entry.getValue().setValue(lang.translation.get(entry.getKey()));
  }
}
```

---

# The Model

![Gosper glider gun](/doc/presentation/images/logic.png)

---

## General Goals for Model
- Update the grid according to a general predefined algorithm 
  - Adjustable mid-simulation through user input
- Easy to implement new logic systems of any kind
- Completely unrelated to view/JavaFX

---

## The Necessities
- A grid that can hold an array of data
- Cells that can store the data of any state or property
- A logic system that can update the states and data of the cells
  - Somehow, there must be a way to find the neighbors of a cell

---

## The Grid
```java
public class Grid<T extends Enum<T> & State> {
  private List<List<Cell<T>>> grid = new ArrayList<>();

  public Cell<T> getCell(int row, int col) {
    return grid.get(row).get(col);
  }

  public void updateGrid() {
    for (List<Cell<T>> row : grid) {
      for (Cell<T> cell : row) {
        cell.update();
      }
    }
  }
}
```
---

## The State

In order to have a class to abstract enums, we must make a State interface

```java
public interface State {
  
  int getValue();
  static <T extends Enum<T> & State> T fromInt(Class<T> enumClass, int value) {}
}
```
---

## The State Implementation

```java
public enum LifeState implements State {
  DEAD(0),
  ALIVE(1);

  private final int value;

  LifeState(int value) {
    this.value = value;
  }

  @Override
  public int getValue() {
    return value;
  }
}
```
---

## The Logic

```java
public abstract class Logic<T extends Enum<T> & State> {
  
  public void update() {
    int numRows = grid.getNumRows();
    int numCols = grid.getNumCols();

    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        Cell<T> cell = grid.getCell(row, col);
        updateSingleCell(cell);
      }
    }
    grid.updateGrid();
  }

  protected abstract void updateSingleCell(Cell<T> cell);
}
```
---

## The Logic Implementation

```java
@Override
protected void updateSingleCell(Cell<LifeState> cell) {
  if (currentState == LifeState.ALIVE) {
    if (!survivalRequirement.contains(liveNeighbors)) {
      cell.setNextState(LifeState.DEAD);
    }
  } else {
    if (birthRequirement.contains(liveNeighbors)) {
      cell.setNextState(LifeState.ALIVE);
    }
  }
}
```
---

## The NeighborCalculator

* Uses Breadth First Search to find all neighbors within X-steps away
  * Neighbors stored in CellNeighbor.properties
```
SQUARE_MOORE=-1,-1; 1,0; 1,1; 0,1; -1,1; -1,0; -1,-1; -1,0
SQUARE_NEUMANN=-1,0; 1,0; 0,1; 0,-1
```

* Grid calls the NeighborCalculator to assign each cell a neighbor

```java
public void assignNeighbors() {
    for (int row = 0; row < getNumRows(); row++) {
      for (int col = 0; col < getNumCols(); col++) {
        getCell(row, col).setNeighbors(
            neighborCalculator.getNeighbors(this, row, col));
      }
    }
}
```
---
## Edge Cases
- Variable number of states: Store states in Cell properties
- Hexagon/Triangle: Store different neighbor directions assigned to a 2D grid
- Get neighbors in 1 direction X steps away (Darwin/Sugar): DFS in one direction
- Ensure it doesn't affect encapsulation/abstraction
---

# DEMO

---

## Darwin

* Build an interpreter to read/parse instructions
  * Map instructions to a general form (MV -> MOVE)
* Use reflection to call instructions:
```aiignore
if (method.getName().startsWith("execute")) {
  String instructionName = method.getName().substring(7).toUpperCase();
  instructionMethods.put(instructionName, method);
}
```
* Instructions just like assembly code, 
  * GO X: branch X
  * IFENEMY, IFWALL, etc.: beq, beqz, bneq
---
## Example Darwin Files
```aiignore
# Example Flytrap
ifenemy 4
left 90
go 1
infect 12
go 1
```
---
```aiignore
# Student
ifenemy 4
left 90
go 1
infect 8
left 90
go 1
```

```aiignore
# Professor
LEFT 0
GO 1
```
---
# The Config

---

# The APIs

![general model of the API](/doc/presentation/images/api.jpg)

---

## General goal of the API

---

# Testing

![testing meme](/doc/presentation/images/testing.png)

---
## CellFactory

* Throw every possible value at it, 0, -1, MAX_VALUE, MIN_VALUE, "42", etc.
```java
@Test
public void CellFactory_CreateCell_InvalidInputMinInt_ReturnsDefaultState() {
  CellFactory<TestState> factory = new CellFactory<>(TestState.class);
  Cell<TestState> cell = factory.createCell(Integer.MIN_VALUE);
  assertEquals(TestState.ZERO, cell.getCurrentState(),
      "Invalid input Integer.MIN_VALUE should return default state ZERO.");
}
```
---
## Grid

```java
@Test
public void givenTriMoore6x6_whenSteps1_thenContainsExpectedDirections() {
  Grid<DummyState> g = createGrid(6, 6, GridShape.TRI, NeighborType.MOORE, EdgeType.BASE);
  NeighborCalculator<DummyState> calc = g.getNeighborCalculator();
  calc.setSteps(1);
  Map<Direction, Cell<DummyState>> neighbors = calc.getNeighbors(g, 3, 3);

  Direction[] expectedDirections = {
      new Direction(-1, -2), new Direction(-1, -1), new Direction(-1, 0),
      new Direction(-1, 1), new Direction(-1, 2),
      new Direction(0, -2), new Direction(0, -1), new Direction(0, 1), new Direction(0, 2),
      new Direction(1, -1), new Direction(1, 0), new Direction(1, 1)
  };

  for (Direction d : expectedDirections) {
    assertTrue(neighbors.containsKey(d), "Missing direction: " + d);
  }
}
```

---
## Darwin
```java
  public void DarwinLogic_IfEmptyRotatesLeft90_WhenCellHasEmptyNeighbor() throws Exception {
    List<String> ifEmptyProgram = new ArrayList<>();
    ifEmptyProgram.add("IFEMPTY 2");
    ifEmptyProgram.add("LEFT 90");
    ifEmptyProgram.add("GO 1");
    testSpeciesPrograms.put(1, ifEmptyProgram);
    darwinLogic.assignSpeciesPrograms(testSpeciesPrograms);

    Cell<DarwinState> testCell = grid.getCell(1, 1);

    assertEquals(0, testCell.getProperty("orientation"),
        "Initial orientation should be 0 degrees.");

    darwinLogic.update();

    assertEquals(90, testCell.getProperty("orientation"),
        "Cell should rotate left to 90 degrees due to IFEMPTY.");
  }
```
---
## Slider UI
```java
  @Test
  public void createRangeUI_DoubleInput_ValidAndInValidWrite() {
    HBox rangeUI = SceneUIWidgetFactory.createRangeUI(
        0,
        10,
        5,
        DUMMY_LABEL,
        DUMMY_TOOLTIP,
        DUMMY_DOUBLE_CONSUMER
    );
    Button finishButton = createBasicSplashScreen(rangeUI, "Double Range UI");
  
    // Assertions for UI initialization
    Slider slider = (Slider) rangeUI.lookup(".slider");
    Node thumb = slider.lookup(".thumb");
    TextField rangeTextField = (TextField) rangeUI.lookup(".range-text-field");
    Assertions.assertEquals("5.0", rangeTextField.getText());
    Assertions.assertEquals(5.0, slider.getValue());
    Assertions.assertNotNull(thumb);
  
    // Action Test
    drag(thumb).moveBy(50, 0);
    drag(thumb).moveBy(-100, 0);
    drag(thumb).drop();
    writeInputTo(rangeTextField, "7.5"); // Valid input
    press(KeyCode.ENTER);
    writeInputTo(rangeTextField, "###"); // Invalid input
    press(KeyCode.ENTER);
  
    // Close
    clickOn(finishButton);
  }
```

---

## Color Selector UI
```java
  @Test
  public void createColorSelectorUI_CreateBasicWidget_PickColorInPickerAndTextInput() {
    HBox colorSelectorUI = SceneUIWidgetFactory.createColorSelectorUI(
        "#AABBCC",
        DUMMY_LABEL,
        DUMMY_TOOLTIP,
        DUMMY_STRING_CONSUMER
    );
    Button finishButton = createBasicSplashScreen(colorSelectorUI, "Color Selector UI");

    // Assertions for UI initialization
    TextField colorTextField = (TextField) colorSelectorUI.lookup(".color-text-field");
    Assertions.assertEquals("#AABBCC", colorTextField.getText());
    ColorPicker colorPicker = (ColorPicker) colorSelectorUI.lookup(".color-picker");
    Assertions.assertEquals("0xaabbccff", colorPicker.getValue().toString());

    // Action Test
    clickOn(colorPicker).moveBy(0, 100).clickOn(MouseButton.PRIMARY);
    writeInputTo(colorTextField, "#123456"); // Valid input
    press(KeyCode.ENTER);
    writeInputTo(colorTextField, "###"); // Invalid input
    press(KeyCode.ENTER);

    // Close
    clickOn(finishButton);
  }
```

---

## Zoom-able Pane UI

```java
  @Test
  public void dragZoomViewUI_CreateBasicWidget_ZoomAndDrag() {
    Pane dragZoomViewUI = SceneUIWidgetFactory.dragZoomViewUI(
        DUMMY_RECTANGLE,
        DUMMY_RECTANGLE_2
    );
    StackPane container = new StackPane(dragZoomViewUI);
    container.setMaxHeight(300);
    container.setMinHeight(300);
    Button finishButton = createBasicSplashScreen(container, "Drag Zoom View UI");

    // Assertions for UI initialization
    Pane miniMapPane = (Pane) dragZoomViewUI.lookup(".mini-map-pane");
    Assertions.assertNotNull(miniMapPane);

    // Action Test
    drag(dragZoomViewUI).moveBy(50, 50);
    drag(dragZoomViewUI).moveBy(-100, -100);
    drag(dragZoomViewUI).drop();
    scroll(VerticalDirection.UP);
    scroll(VerticalDirection.DOWN);

    // Close
    clickOn(finishButton);
  }
```

---