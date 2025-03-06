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

## Theme Css

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

## Language Properties

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

--- 

The implementation involves
a LanguageController, which creates a separate StringProperty for each key in these files.
All UI components that require text are then bound to the corresponding StringProperty.

```java
// Initialize the String Properties
public LanguageEnumConstructor() {
  for (String key : properties.stringPropertyNames()) {
    translation.put(key, properties.getProperty(key));

    if (!translations.containsKey(key)) {
      translations.put(key, new SimpleStringProperty("??"));
    }
  }
}

public static void switchLanguage(Language lang) {
  for (Map.Entry<String, StringProperty> entry : translations.entrySet()) {
    entry.getValue().setValue(lang.translation.get(entry.getKey()));
  }
}
```

---

# The Model

![w:800 h:480 Gosper glider gun](/doc/presentation/images/logic.png)

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

  static <T extends Enum<T> & State> T fromInt(Class<T> enumClass, int value) {
  }
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

---

## The NeighborCalculator

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

## General Goals for the Config
 * Abstract away the file system from the model and view
 * Intuitive and readable configuration files with ability for variation 
   * The files should also be flexible with implementation 
 * Provide correct data to the model and implement proper error checking
 * Create a relatively flexible way to save files
---
## The Necessities 
* Use XML files for simulation data storage
* A basic and flexible structure for the configuration files
* An easy way to save files and manage data

---

## ConfigInfo

``` java
public record ConfigInfo(
    SimulationType myType,
    cellShapeType myCellShapeType,
    gridEdgeType myGridEdgeType,
    neighborArrangementType myneighborArrangementType,
    Integer neighborRadius,
    String myTitle,
    String myAuthor,
    String myDescription,
    int myGridWidth,
    int myGridHeight,
    int myTickSpeed,
    List<List<CellRecord>> myGrid,
    ParameterRecord myParameters,
    Set<Integer> acceptedStates,
    String myFileName
)
```

---
## Example Configuration File
  The following is a xml file for the SugarScape Simulation:
```XML
<?xml version="1.0" ?>
<simulation>
  <type>ANT</type>
  <title>Tiny Foraging Ants Simulation</title>
  <author>Jacob You</author>
  <description>
    A tiny grid with one nest containing ants and one food cell.
  </description>
  <parameters>
    <doubleParameter name="maxAnts">5</doubleParameter>
    <doubleParameter name="evaporationRate">10</doubleParameter>
    <doubleParameter name="maxHomePheromone">100</doubleParameter>
    <doubleParameter name="maxFoodPheromone">100</doubleParameter>
    <doubleParameter name="basePheromoneWeight">1</doubleParameter>
    <doubleParameter name="pheromoneSensitivity">2</doubleParameter>
    <doubleParameter name="pheromoneDiffusionDecay">1</doubleParameter>
  </parameters>
  <cellShapeType>Square</cellShapeType>
  <gridEdgeType>BASE</gridEdgeType>
  <neighborArrangementType>Moore</neighborArrangementType>
  <width>3</width>
  <height>3</height>
  <defaultSpeed>10</defaultSpeed>
  <initialCells>
    <row>
      <cell state="0"/>
      <cell state="0"/>
      <cell state="0"/>
    </row>
    <row>
      <cell state="0"/>
      <cell state="1" searchingEntities="5"/>
      <cell state="0"/>
    </row>
    <row>
      <cell state="0"/>
      <cell state="3"/>
      <cell state="0"/>
    </row>
  </initialCells>
  <acceptedStates>
    0 1 3
  </acceptedStates>
  <neighborRadius>1</neighborRadius>
</simulation>
```
---

## The Config Reader
```` java
 public ConfigInfo readConfig(String fileName)
      throws ParserConfigurationException, IOException, SAXException, IllegalArgumentException {
    if (!fileMap.containsKey(fileName)) {
      createListOfConfigFiles();
    }
    try {
      File dataFile = fileMap.get(fileName);
      Log.trace("Looking for file at: " + System.getProperty("user.dir") + DATA_FILE_FOLDER);
      return getConfigInformation(dataFile, fileName);
    } catch (ParserConfigurationException | SAXException | IOException |
             IllegalArgumentException e) {
      throw new ParserConfigurationException(e.getMessage());
    }
  }
````
---

## The Config Writer

```` java
 public void saveCurrentConfig(ConfigInfo myNewConfigInfo, String path)
      throws NullPointerException, ParserConfigurationException, IOException, TransformerException {
    if (myNewConfigInfo == null) {
      throw new NullPointerException("error-nullConfigInfo");
    }
    if (path == null) {
      throw new NullPointerException("error-nullPath");
    }
    myConfigInfo = myNewConfigInfo;
    Document xmlDocument = createXMLDocument();
    File outputFile = createOutputFile(path);
    populateXMLDocument(xmlDocument);
    writeXMLDocument(xmlDocument, outputFile);
  }
````




# The APIs

![general model of the API](/doc/presentation/images/api.jpg)

---

## General goal of the API's
* To provide abstraction from the view.
* For the config API, to manage the config data parsing, storage, and file system.
  * This means abstracting away the different configuration classes and how the configuration file work. 
* For the model (logic) API, to manage the model and simulation style properties.
---

---

## Config API 
```java 
package cellsociety.model.configAPI;

import cellsociety.model.config.CellRecord;
import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigReader;
import cellsociety.model.config.ConfigWriter;
import cellsociety.model.config.ParameterRecord;
import cellsociety.model.modelAPI.ModelApi;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * The configAPI is responsible for managing the interactions between the configuration and the
 * Scene Controller. It deals with the functions of the ConfigReader and ConfigWriter.
 *
 * @author Billy McCune
 */
public class configAPI {

  private ConfigReader configReader;
  private ConfigWriter configWriter;
  private ConfigInfo configInfo;
  private ParameterRecord parameterRecord;
  private boolean isLoaded;
  private ModelApi myModelApi;
  private List<List<Integer>> myGridStates;
  private List<List<Map<String, Double>>> myGridProperties;


  public configAPI() {}
  
  public List<String> getFileNames() {}
  
  public void setModelAPI(ModelApi modelAPI) {}
  
  public void loadSimulation(String fileName)
      throws ParserConfigurationException, IOException, SAXException, IllegalArgumentException {}
  
  public String saveSimulation(String FilePath)
      throws ParserConfigurationException, IOException, TransformerException {}
  
  public Set<Integer> getAcceptedStates() {}
  
  public int getGridWidth() throws NullPointerException {}
  
  public int getGridHeight() throws NullPointerException {}
  
  public Map<String, String> getSimulationInformation() throws NullPointerException {}
  
  public void setSimulationInformation(Map<String, String> simulationDetails)
      throws NullPointerException {}
  
  public double getConfigSpeed() throws NullPointerException {}

}

```
---

### Extension:

### Support:

### Key Hidden Implementation details:

### Use Case:

---

## Model API

```` java
package cellsociety.model.modelAPI;

import cellsociety.model.config.CellRecord;
import cellsociety.model.config.ConfigInfo;
import cellsociety.model.config.ConfigInfo.SimulationType;
import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.constants.EdgeType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.neighbors.NeighborCalculator;
import cellsociety.model.logic.Logic;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * The ModelApi is responsible for managing all interactions with the model and the SceneController.
 * It also manages the user style preferences.
 *
 * @author Billy McCune
 */
public class ModelApi {

  private static final String LOGIC_PACKAGE = "cellsociety.model.logic";
  private static final String STATE_PACKAGE = "cellsociety.model.data.states";
  private ParameterRecord myParameterRecord;
  private ConfigInfo configInfo;

  ParameterManager myParameterManager;
  CellColorManager myCellColorManager;
  StyleManager myStyleManager;

  // Model
  private Grid<?> grid;
  private CellFactory<?> cellFactory;
  private Logic<?> gameLogic;
  private NeighborCalculator<?> myNeighborCalculator;


  public ModelApi() {}

  public void setConfigInfo(ConfigInfo configInfo) {}

  public void updateSimulation() {}
  
  public void resetGrid(boolean wantsDefaultStyles) throws ClassNotFoundException {}

  public Map<String, Double> getDoubleParameters() {}

  public Map<String, String> getStringParameters() {}

  public String getCellColor(int row, int col, boolean wantDefaultColor)
      throws NullPointerException {}

  public void resetParameters()
      throws IllegalArgumentException, NullPointerException, IllegalStateException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {}

  public void resetModel() throws NoSuchMethodException {}

  public List<List<Integer>> getCellStates() {}

  public List<List<Map<String, Double>>> getCellProperties() {}

  public Consumer<Double> getDoubleParameterConsumer(String paramName) {}

  public Consumer<String> getStringParameterConsumer(String paramName) {}

  public double[] getParameterBounds(String paramName){}

  public Map<String, String> getCellTypesAndDefaultColors(String SimulationType) {}

  public void setNewColorPreference(String stateName, String newColor) {}

  public String getColorFromPreferences(String stateName) {}

  public String getDefaultColorByState(String stateName) {}

  public void setNeighborArrangement(String neighborArrangement) throws NullPointerException {}

  public void setEdgePolicy(String edgePolicy) throws NullPointerException {}

  public void setCellShape(String cellShape) throws NullPointerException {}

  public void setGridOutlinePreference(boolean wantsGridOutline) {}

  public boolean getGridOutlinePreference() {}

  public List<String> getPossibleNeighborArrangements() {}

  public List<String> getPossibleEdgePolicies() {}

  public List<String> getPossibleCellShapes() {}

  public String getDefaultNeighborArrangement() {}

  public String getDefaultEdgePolicy() {}

  public String getDefaultCellShape() {}


}


````


---

### Extension: 

### Support:

### Key Hidden Implementation details:

### Use Case: 

--- 

# Testing

![w:400 h:800testing meme](/doc/presentation/images/testing.png)

---

## Config/API Test 1

---

## Config/API Test 2

---

## Config/API Test 3

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
# DESIGN
---

## Stable Design: Logic

* Logic's only job is to update the state of the grid, cells, and parameters
* Any necessary changes/additions usually happens in grid, cell, neighbor calculator, etc.
    * Logic's design never substantially changes, it just uses the new design of its subcomponents
* The only addition was the restricting of parameters loaded in from a property file

---

## Unstable Design: Neighbor Calculator

* Grid used to have an if statement
    * If type = WATOR, switch to TorusNeighbors
    * Breaks abstraction
* Created a NeighborCalculator class, holding both methods
    * The Logic could call the corresponding calculator
* Using abstraction, create LifeNeighborCalculator, PercolationNeighborCalculator
    * Have the default calculator be set in constructor of the class
    * SugarCalculator overrode the standard calculator to implement its special logic

---

## Unstable Design

* Hexagons and Triangles implemented for grid shape
    * Store neighbors in a property class, then implement logic to handle different shapes
* Grid shape, neighborhood, and edge type became customizable
    * Make setters/getters for each value, change superclasses to input these parameters instead
* Added style parameters into XML files
    * Superclasses became essentially obsolete, as the defaults were set in the XML
* Darwin and Sugarscape have unique neighbor calculations in one direction
    * Implement a Raycasting helper class, and add new methods to access raycasting neighbors

---

## Implementation Helped By Good Design

---

## Implementation Challenged by Hidden Assumptions: States

* Initially, enums seemed perfect for states
    * Unchanging, unalterable, clearly defined states that can have integer values
* Enums CANNOT be dynamically set
    * Bacteria and Darwin have a variable number of states
* Enums CANNOT store extensive data
    * Shark energy, orientation of ant, etc.
* Looked for workaround, like setting a map in the enum
* Stored many properties in the Cell class instead

---

# Teamwork

(Teamwork makes the Darwin work)

---

## Significant Events

**Positive**

**Negative**

---

## Teamwork That Worked Well

**Great:** Scheduling blocks of time to work together is incredibly helpful.

**Improvable:** Be more proactive in scheduling meetings and assigning deadlines to avoid 30 hours
of work the weekend before.

---

## Improving Teamwork

**Jacob:**

* I personally tried to always have a next team meeting lined up at the end of the current
  meeting. I found team meetings to be incredibly helpful, giving me a time to solely focus on a
  task, as well as a space to talk through problems with multiple complex sections.
* In the future, the consistency, number, and length of productive team meetings could be used as
  evidence.

**Hsuan-Kai Liao**

* I enjoyed how we figure out the conflicts and issues when we meet. I think we can improve by
  having more meetings and discussing more about the project. The in-person can solve the problems
  way faster than texting or through merging reviews.
* In the future, I would like to have more and highly-efficient meetings for designing APIs and 
  abstractions, which helps us to better separate the view and the model.


**3**
---