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

![w:800 h:480 Screenshot of the Grid In Day and Dark Themes](/doc/presentation/images/theme.png)

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

# DEMO

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
```
---
```XML
  <parameters>
    <doubleParameter name="maxAnts">5</doubleParameter>
    <doubleParameter name="evaporationRate">10</doubleParameter>
    <doubleParameter name="maxHomePheromone">100</doubleParameter>
    <doubleParameter name="maxFoodPheromone">100</doubleParameter>
    <doubleParameter name="basePheromoneWeight">1</doubleParameter>
    <doubleParameter name="pheromoneSensitivity">2</doubleParameter>
    <doubleParameter name="pheromoneDiffusionDecay">1</doubleParameter>
  </parameters>
```
---
```XML
  <initialCells>
    <row>
      <cell state="0"/> <cell state="0"/> <cell state="0"/>
    </row>
    <row>
      <cell state="0"/> <cell state="1" searchingEntities="5"/> <cell state="0"/>
    </row>
    <row>
      <cell state="0"/> <cell state="3"/> <cell state="0"/>
    </row>
  </initialCells>
```
---
```XML
  <cellShapeType>Square</cellShapeType>
  <gridEdgeType>BASE</gridEdgeType>
  <neighborArrangementType>Moore</neighborArrangementType>
  <width>3</width>
  <height>3</height>
  <defaultSpeed>10</defaultSpeed>
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
---

# The APIs

![general model of the API](/doc/presentation/images/api.jpg)

---

## General goal of the API's

* To provide abstraction from the view.
* For the config API, to manage the config data parsing, storage, and file system.
    * This means abstracting away the different configuration classes and how the configuration file
      work.
* For the model (logic) API, to manage the model and simulation style properties.
---

---

## Config API

```java 
public class configAPI {
  public configAPI() {}
  
  public List<String> getFileNames() {}
  
  public void setModelAPI(ModelApi modelAPI) {}
  
  public void loadSimulation(String fileName)
      throws ParserConfigurationException, IOException, SAXException, IllegalArgumentException {}
  
  public String saveSimulation(String FilePath)
      throws ParserConfigurationException, IOException, TransformerException {}
```
---
```java
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
* Modular I/O:
Separates file parsing and saving into ConfigReader and ConfigWriter, allowing easy extensions.

* Model Integration:
Uses setModelAPI() to seamlessly link configuration changes with the simulation model.

---

### Support:

* Clear API Methods:
Well-named methods (e.g., loadSimulation, saveSimulation) guide users in configuration management.

* Encapsulation:
Hides XML parsing, file I/O, and data aggregation details to prevent misuse.

---

### Key Hidden Implementation details:

* Internal State Management:
Abstracts the handling of grid states, simulation details, and parameter records.

* Data Conversion:
Automatically aggregates simulation data into ConfigInfo objects.

---

### Use Case:

* Load:
Update configuration and propagate changes to the model.

* Save:
Retrieve grid data from the Model API, create a ConfigInfo, and invoke methods to persist settings.

* Parameter Updates:
Teammates can adjust parameters safely, without knowing the underlying implementation.

---

## Model API

```java
public class ModelApi {
  public ModelApi() {}

  public void setConfigInfo(ConfigInfo configInfo) {}

  public void updateSimulation() {}
  
  public void resetGrid(boolean wantsDefaultStyles) throws ClassNotFoundException {}
  
  public void resetParameters() {}

  public void resetModel() throws NoSuchMethodException {}

  // Get/set cell states, colors, cell shape, edge policy, etc.
}
```
---

### Extension:
* Dynamic Logic Loading:
  Uses reflection to load simulation-specific logic classes, implementing new simulation types.

* Pluggable Components:
  Supports various model components that can be replaced or extended to support new features.

* Flexible Parameter Management:
  Exposes Consumers that allow easy integration of new simulation parameters.

---

### Support:
* Clear Separation of Concerns:
  Divides responsibilities among specialized managers to maintain clean code organization.

* Robust Error Handling:
  Employs consistent try/catch blocks and specific exception types to provide clear error feedback.

* Encapsulated Configuration:
  Hides complex configuration and grid initialization details behind well-named methods.

---

### Key Hidden Implementation details:

* Internal Grid Management:
  The API encapsulates grid initialization, deep copying, and neighbor calculations.
* Reflection and Dynamic Instantiation:
  Abstracts away the complexity of using Java reflection for loading classes and instantiating simulation logic.
* Style and State Abstractions:
  Manages user style preferences and cell state details internally.

---

### Use Case:

* Parameter Updates:
  Teammates can adjust simulation parameters safely, without knowing the underlying implementation.

--- 

# Testing

![w:400 h:800testing meme](/doc/presentation/images/testing.png)

---

## Config/API Test 1

````java
@Test
  public void resetGrid_NullConfigInfo_DoesNothing() throws ClassNotFoundException {
    // Tested Method: resetGrid()
    // State: configInfo is null.
    // Expected Outcome: resetGrid() does nothing and grid remains null.
    ModelApi api = new ModelApi();
    api.resetGrid(true);
    assertNull(getPrivateField(api, "grid"));
  }
````
---

## Config/API Test 2
````java
 @Test
  void readConfig_EmptyRow_ThrowsParserConfigurationException() {
    Exception e = assertThrows(ParserConfigurationException.class, () -> configReader.readConfig("ErrorEmptyRow.xml"));
    System.out.println("[ErrorEmptyRow.xml] " + e.getMessage());
  }

````
---

## Config/API Test 3

````java
 @Test
    public void loadSimulation_ValidFile_SetsModelApiConfigInfo() throws ParserConfigurationException, IOException, SAXException {
      // Testing configAPI.loadSimulation() to ensure that DummyModelApi receives the dummy config.
      configAPI api = new configAPI();
      DummyModelApi dummyModel = new DummyModelApi();
      api.setModelAPI(dummyModel); // Set the model API before loading
      // Inject DummyConfigReader so that readConfig returns a dummy config instead of reading a file.
      setPrivateField(api, "configReader", new DummyConfigReader());
      api.loadSimulation("dummyFile.xml");
      assertEquals(createStaticDummyConfigInfo(), dummyModel.configInfoDummy, "After loadSimulation(), model API should have the dummy config info set");
    }
````
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
  
  Pane miniMapPane = (Pane) dragZoomViewUI.lookup(".mini-map-pane");
  Assertions.assertNotNull(miniMapPane);

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
    * Breaks abstraction
* Created a NeighborCalculator class, holding both methods
    * The Logic could call the corresponding calculator

---

## Unstable Design

* Create an abstract NeighborCalculator class
  * SugarCalculator overrode the standard calculator to implement its special logic
* Hexagons and Triangles implemented for grid shape
    * Store neighbors in a property class, then implement logic to handle different shapes

---

## Unstable Design

* Grid shape, neighborhood, and edge type became customizable
* Added style parameters into XML files
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
* Stored many properties in the Cell class instead

---

# Teamwork

---

## Significant Events

**Positive:** One of our meetings, we got together and drew out all of the connections between the
model and view, and drafted out an API that could handle all communication. This helped tremendously
with figuring out any potential overlap between the two sections.

**Negative:** Initially starting out, we weren't very familiar with git, and multiple times ended up
overwriting some of each other's code and progress. This caused lots of confusion, before we met
with our UTAs and ultimately learned a better workflow.

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


**Billy McCune**

* I enjoyed working with the team and more meetings would be appreciated but it worked out well. The conficts where much easier to fix as a team. 
* In the future, I would like to spend more time coding with others and having regularly schedules 
team meetings.
---