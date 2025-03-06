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

* 1
* 2
* 3, etc.

---

# DEMO

---

## Stylizing with CSS and properties

* CSS
* Language (you can put these on different slides)
```aiignore
THIS IS HOW YOU TYPE IN CODE. PLEASE DO THIS WHEN NECESSARY.
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

# The APIs

![general model of the API](/doc/presentation/images/api.jpg)

---

## General goal of the API

---

# Testing

![w:400 h:800testing meme](/doc/presentation/images/testing.png)

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

## Helped By Good Design

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
(makes the Darwin work)

---