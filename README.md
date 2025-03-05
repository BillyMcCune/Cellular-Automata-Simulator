# cell society

## TEAM NUMBER 1

#### Billy McCune wrm29, Jacob You jay27, Hsuan-Kai Liao hl475

This project implements a cellular automata simulator.

### Timeline

* Start Date: 01/23/25

* Finish Date: 03/05/25

* Hours Spent: ~120 hours per person

### Attributions

* Resources used for learning (including AI assistance)
    * Common design patterns - https://refactoring.guru/design-patterns/catalog
    * Testing naming
      conventions - https://osherove.com/blog/2005/4/3/naming-standards-for-unit-tests.html
    * Enum properties and flexibility - ChatGPT 4o

* Resources used directly (including AI assistance)

### Running the Program

* Main class:

* Data files needed:

* Interesting data files:
    * There are error throwing data files that you can use to test our error throwing.
    * Species instructions for Darwin stored in resources/speciesdata
    * Style css files in resources/cellsociety/style/mystery that define color and style
    * Language property files in resources/cellsociety/lang have set values for each item for all
      locations
    * SimulationStyle.properties is stored in base directory, as it is a per user file that
      is accessed upon runtime. There may be other possible implementations, but this is the way we
      found
      with our current time remaining.

* Key/Mouse inputs:

### Notes/Assumptions

* Assumptions or Simplifications:

#### Logic Assumptions/Simplifications

* All Cell properties are doubles, and can only be doubles. When properties don't exist, they are
  set to zero automatically if pulled.
    * In the future, it would be better to use a record file. However, the need for properties other
      than doubles was not necessary until Darwin, when this property format was already heavily
      used.
* Grid is stored in a square format, even for triangle and hexagon. Triangle grids and hexagon grids
  were place onto the square grid.
    * This causes some discrepancies with Direction, as for example, up right for a hexagon is made
      to be Direction(-1, 1), when its actually a 60 degree angle. Despite this, all implementation
      should not break.


* Known Bugs:

#### Logic Bugs

* FallingState looks for cells down left, down right, and down. With some implementations, shapes
  like hexagon do not have down left or down right on hexagons on the higher part of the row. 
  * This means that cells can only go downward, despite there being a down left and down right, which are
    coded as Direction(0, -1) and Direction(0, 1) in neighbors instead. This is one of the
    aforementioned discrepancies with the square grid.


* Features implemented:

    * Simulation Features:
    * UI Features:
    * Configuration Features:


* Features unimplemented:

* Noteworthy Features:

### Assignment Impressions


