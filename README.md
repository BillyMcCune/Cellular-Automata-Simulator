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
* BacteriaLogic and DarwinLogic use DUMMY enum variables instead of real enum states, as these
  simulations have a variable number of states. This makes implementing enums impossible, so the
  real value are stored in the cell properties instead under speciesID and coloredID.
* DarwinLogic implements the MOVE function by assuming one cell is one pixel. It did not make much
  sense to us to make species move pixels in a cell, as changing the size of the grid (either
  through settings or zooming in and out) would completely change the distance a species moved
  inside its cell.
    * Tracking the size of a cell also didn't make sense without a grid pixel size parameter. It
      would be possible with this parameter, but we decided to stick with the most
      basic implementation.
* In all models where objects or states move and can only hold one entity, it is assumed that
  they cannot move into where a entity was previously, even if the location would be empty on
  the next step.
    * In Wator, sharks eat fish before fishes can move.
    * In Darwin, species rotate first, then get infected, then move left or right if not infected.
* In Darwin, when a species reverts to its old species, it reverts to instruction 1.


* Known Bugs:

#### Logic Bugs

* FallingLogic looks for cells down left, down right, and down. With some implementations, shapes
  like hexagon do not have down left or down right on hexagons on the higher part of the row.
    * This means that cells can only go downward, despite there being a down left and down right,
      which are
      coded as Direction(0, -1) and Direction(0, 1) in neighbors instead. This is one of the
      aforementioned discrepancies with the square grid.
* When AntLogic looks for locations in the direction the ant is facing, it looks for all cells up,
  down, left, and right of the cell currently being looked at. For hexagon and triangle
  implementations, this might lead to unexpected possible directions.
* If DarwinLogic has an infinite loop, such as (GO 2, GO 1), the species breaks, and the code
  errors.
    * The idea I had was to set a timer in Logic, but this would require a bit more time to figure
      out.
* LifeLogic can't take in values bigger than 9, which could pose a problem for neighborhoods with
  more than 9 possible neighbors. Although it wouldn't error, it simply wouldn't take in the value.
    * With time, I would probably choose to implement letter values, like A = 10, B = 11, etc. For
      cells with very large neighborhoods greater than 36, I would most likely create a new way of
      implementing rulestrings that would take in comma seperated numbers.
* SugarScape can find the greatest value sugar pile in front of it, but due to changes made for
  Darwin, the sugar agent now teleports straight to the grid with the most sugar in its raycast.
    * The way that I would implement this would most likely be to raycast in all directions by using
      the getAllRaycastDirections, then see which direction contains the best direction, and go in
      that direction

* Features implemented:

#### Logic Implemented Features

* Simulations:
    * Game of Life
        * Rulestring parsing of S/B and B/S notation
    * Percolation
    * Spreading of Fire
    * Model of Segregation
    * Wa-Tor World
    * Falling Sand
    * Rock Paper Scissors (Bacteria)
    * Foraging Ants
    * SugarScape
    * Darwin
* Customization:
  * Moore and Von Neumann neighborhoods
  * Hexagon, Triangle, and Square shapes
  * Standard and Torus boundaries
  * Raycasting in a specific direction
  * (not fully implemented) Radius for neighborhoods
  * (not fully implemented) Ring neighborhood

* UI Features:
* Configuration Features:


* Features unimplemented:

#### Logic Unimplemented Features

* Radius for cell neighborhoods exists in XML files, but is not actually implemented in the API.
  The functionality also exists in Grid and NeighborCalculator, but it would take lots of time
  to implement. However, all functionality does exist (look at NeighborCalculatorTest).
    * This would essentially include the functionality for ExtendedMoore, and even go beyond it
* The ring for cell neighborhoods has not been implemented in the API. It is implemented in the XML
  and NeighborCalculator though. It would essentially get the ring of neighbors at radius x, and
  nothing else. This would fall under custom neighborhoods (logic used in NeighborCalculatorTest).
* Rulestrings greater than 9 for LifeLogic
* Non-double property values for Cells.
* SugarScape Spice simulation (Our choice for the 6th Generalize and Robust). It would theoretically
  just require adding more instance variables, setters, and getters, and a few slight modifications
  to Logic. We simply just did not have time.
* Edge Case grid mirroring has not been implemented, but would simply be another check and simple
  lines of logic in NeighborCalculator. The enums exist and would be handleable immediately upon
  implementation.
* Darwin instructions have not been translated to other languages. If they were, they would simply
  all be assigned to the english mapping used in naming the methods, essentially the same way that
  the shortened instruction names were translated to their longer names.

* Noteworthy Features:

#### Logic Features:

* Adding hexagons and triangles on a square grid are especially difficult, especially with differing
  neighborhoods and radii.
    * Hexagons go up and down in a row, depending on whether a cell
      is on an even or an odd indexed column. This means that the neighbor directions are different
      depending on the column. For triangles, the neighbor directions also change depending on
      whether the triangle faces up or faces down. This makes assigning neighbors rather difficult,
      as I would prefer not to hardcode a mapping for every permutation of edge type, neighborhood,
      cell shape, and radius.
    * I found that the difference between up and down hexagons and triangles is simply flipping
      their entire y value upside down. An upward facing MOORE triangle has a bottom left triangle
      of (1, -2) (the downward facing triangle down one, left two). This doesn't exist in a downward
      facing triangle, but the top left of the downward facing triangle's neighborhood is (1,-2). If
      you were to flip all the y coordinates, you would get the neighborhood for the other cell.
    * Using breadth first search, I can get all neighbors of a cell X cells away. I first find all
      of the neighbors of the cell. Then, I add all of those neighbors to a queue, and find all of
      the neighbors of each of those neighbors, essentially getting rings around the original
      neighbor. Through this process, I can find all neighbors x steps away from the original cell.
* Raycasting is essentially going out X cells in a specific direction.
    * This implementation is very easy for squares, but gets very difficult for triangles and
      hexagons. For triangles, what direction can you raycast? How do you define one direction? With
      the inconsistency of triangles having different directions for up and down on a square grid,
      how do you even go out in one direction?
    * In order to raycast, the neighbor calculator takes in the starting cell and row, and assigns
      the direction to a enum representing a direction based on the shape, and the starting row and
      starting column. It uses that enum as a key, and depending on the row and column of the next
      neighbor, choose the correct mapping of the enum direction to the Direction class, then add it
      as one of the neighbors.
    * Raycasting can be called in all directions (Sugarscape) or only in one direction (Darwin). It
      works with torus and base edge types, and can go out any variable amount of steps.
* DarwinHelper is a class that reads the species files, and parses instructions for a cell. It uses
  reflection in order to call specific methods in its class, passing in the correct argument. This
  way, it avoids a large case statement for instructions.

### Assignment Impressions

Jacob: Overall, I think this assignment really improved my current knowledge of proper coding
practices, and greatly enhanced how well designed I can make my code. Every update seemed to break
my code, but every time it still seemed to be MOSTLY doable. It made it very obvious how important
designing the project is, as simple misjudgements (using enums for states) can reverberate
throughout the entire project. I do wish that we got slight nudges or hints towards the different
ways that we would eventually have to implement our code (if we were asked stuff like what if the
number of states changed?) or had some understanding that there would eventually be new features
added on, but I also understand why this might not be the case. Overall, I think this was a great
(but slightly brutal) way to introduce us to standard design principles and coding practices.

