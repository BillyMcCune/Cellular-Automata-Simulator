# cell society

Photos: 

<img width="1296" height="761" alt="Screenshot 2025-07-27 at 12 35 34 PM" src="https://github.com/user-attachments/assets/1c4df93e-c7b8-475a-81be-3b79ca79c90c" />

<img width="1294" height="761" alt="Screenshot 2025-07-27 at 12 34 47 PM" src="https://github.com/user-attachments/assets/1578afdc-266c-46bd-bd75-a210f739368d" />



#### Billy McCune, Jacob You, Hsuan-Kai Liao

This repo implements a cellular automata simulator.

### Timeline

* Start Date: 01/23/25

* Finish Date: 03/05/25

* Hours Spent: ~121 hours per person

### Attributions

* Resources used for learning (including AI assistance)
    * Common design patterns - https://refactoring.guru/design-patterns/catalog
    * Testing naming
      conventions - https://osherove.com/blog/2005/4/3/naming-standards-for-unit-tests.html
    * XML Resources - https://en.wikipedia.org/wiki/XML_Resource
        - https://www.w3schools.com/xml/xml_whatis.asp
    * Enum properties and flexibility - ChatGPT 4o

### Running the Program

* Main class:

* Data files needed:
    * Language Properties Files:
        * English.properties
        * French.properties
        * Mandarin.properties
    * Model Properties Files:
        * CellNeighbor.properties
        * Parameters.properties
    * Style and Visual Properties Files (Non-language):
        * CellColor.properties
        * SimulationStyle.properties
    * Style Css Files
        * Dark Mode
            * docking.css
            * scene.css
            * widget.css
        * Day Mode
            * docking.css
            * scene.css
            * widget.css
        * Mystery Mode
            * docking.css
            * scene.css
            * widget.css
    * Version.properties

* Interesting data files:
    * There are error throwing data files that you can use to test our error throwing.
    * Species instructions for Darwin stored in resources/speciesdata
    * Style css files in resources/cellsociety/style/mystery that define color and style
    * Language property files in resources/cellsociety/lang have set values for each item for all
      locations
    * There are two (kind of) SimulationStyle.properties files:
        * One is stored within the properties file and acts as the default Simulation Styles for the
          User
        * The second one is dynamically made in the base directory to read and write the specific
          user Simulation Styles
            * This approach while not ideal works quite well and is what we ended up doing due to
              time constraints

* Key/Mouse inputs:
    * The only inputs needed are mouse clicking, mouse dragging, mouse scrolling and text inputs.
        * Mouse clicking are for the buttons, it is the main way user can interact with the
          controls.
        * Mouse dragging is for the sliders and window dragging. We implemented the docking system
          which we can drag the window to the edge of the screen and it will dock to the edge.
        * Mouse scrolling is for zooming in and out of the grid.
        * Text inputs are for the user to dynamically change the parameters for the simulation.

#### Configuration Assumptions/Simplifications

* All configuration files are XML.
    * It is possible to allow for non-XML configuration files its just that the XML constraint was
      made
* Saving a random total states or random total proportions will change the XML file to a normal one
    * The random total states and random total proportions allow for the randomly generated grid to
      be randomly generated.
      However, since the user is saving the grid, I am assuming they want to save the grid at that
      state. Thus, the states cannot be random.
      Do to this, the saved XML files will never be random total states nor random proportions.
* Darwin currently has 6 colors defined if a user wants to add a species they must change the cell
  color properties file
  and simulation properties files.

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

#### View Assumptions/Simplifications

* The view is designed to be as flexible as possible, with the ability to add new simulations and
  dynamically change interact with the backend with the showing GUI.
* The simulation begins with a splash screen, and the user can choose the start-up theme and
  start-up
  language.
* The main window has 5 sections:
    * The control panel: This is the main control panel that allows the user to start, pause, load,
      reset. It also allows the user to save the current simulation states to the file.
    * The style panel: This panel allows the user to change the style of the simulation. The user
      can
      change the theme and the language of the simulation, and also change the style of the grid:
      Cell Shape, Edge Policy, and Neighbour Arrangement for the backend.
    * The grid panel: This panel shows the grid of the simulation. The user can zoom in and out of
      the
      grid, and also drag the grid to see different parts of the grid.
    * The info panel: This panel shows the information of the current simulation. It shows the
      current
      iterations, the author of the simulation, and the description of the simulation.
    * The parameter panel: This panel shows the parameters of the simulation. The user can change
      the
      parameters and the color of each simulation type of the simulation dynamically. The user can
      also change the parameters of the
      simulation by dragging the sliders.
* The view implements a docking system, where all the secions mentioned above will be docked to the
  edge of the window when the user drags the window to the edge of the target edge.
* Customized css files are used to define the style of the simulation. The css files are stored in
  resources/cellsociety/style/. The css files define the color and style of the simulation.

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
    * Radius for neighborhoods
    * (not fully implemented) Ring neighborhood

* UI Features:
* Configuration Features:
    * Simulation Features:
    * UI Features:
    * Configuration Features:
        * Default Simulation Parameter Properties
        * Simulation Language Customization
        * Random Configuration by Probability Distribution
        * Random Configuration by Total States
        * File Format Validation
        * Grid Bounds Check
        * Invalid Cell State Check
        * Input Missing Parameters
        * Save Simulation State as XML
        * XML-Based Simulation Configuration
        * Load New Configuration File
        * Darwin Species Files
        * Simulation Styles
        * API's: configAPI and modelAPI

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

#### View Features:

* The auto-fitting grid outlines. The grid will automatically calculate the outside border of
  the current grid.
* The zooming in and out of the grid. The user can zoom in and out of the grid by the zooming
  gestures or scrolling the mouse wheel.
* The docking system. The user can dock the sections of the window to the edge of the window by
  dragging the window to the edge of the section windows.

#### Configuration Features:

* The two API abstraction.
    * The entire model and configuration packages and all their functionality (that we want to show)
      are managed by two API's.
        * The configAPI class manages all the configuration aspects of the project.
        * The modelAPI class manages all the model aspects of the project:
            * Meaning it handles: the grid, logic, cells, cell colors, and style preferences.
            * The API has helper classes which abstract out methods and increases the readability of
              the API.

