
# Refactoring Lab Discussion
#### NAMES: Billy McCune wrm29, Jacob You jay27, Hsuan-Kai Liao hl475
#### TEAM 1


## Design Principles

* Open/Close
  Billy’s Interpretation: Software when made should be expected to work for its given purpose. If the purpose can be extended it should. If you want a different purpose you should write different code without modifying your current code.
  Jacob’s Interpretation: Code should be able to be extended upon, and should allow for adding extra ability and implementation. However, the base core functionality and logic should not be changed.
  Aaron’s Interpretation: Do not make everything accessible and changeable, the class/method only does the thing it is assigned. If you want some other functionality, add another method to implement that.

* Liskov Substitution Principle
  Billy’s Interpretation: A superclass and subclass should follow the same structure and should seem the same within code. For example: if I had a sphere super class. I would hope that all the subclasses feel like spheres. No subclass should act like a cube.
  Jacob’s Interpretation: Objects of a superclass should be able to be replaced by any object of its subclasses and should work perfectly fine.
  Aaron’s Interpretation: The superclass should handle the objects’ main attributes and the subclasses should follow the structure to implement functionality that have been made in its superclass.


### Current Abstractions

#### Abstraction #1: Logic
* Current embodiment of principles: It updates the grid by calling update, which checks certain parameters before calling updateSingleCell. It stores the grid to run logic on.
* Improved embodiment of principles: Specific logic implementations do specific actions to cells depending on their logic defined by the simulation.

#### Abstraction #2: State
* Current embodiment of principles: It is an implementation for the storage of enumerated values. All states also should be able to convert integers integers into their enum values
* Improved embodiment of principles: Specific states have specific enum values depending on the simulation.


#### Abstraction #3: ConfigReader
* Current embodiment of principles: It abstracts the process of reading and parsing through the xml data files.

* Improved embodiment of principles: handle parsing for errors in the data



### New Abstractions

#### Abstraction #1
* Description:

* How it supports making it easier to implement new features:


#### Abstraction #2
* Description:

* How it supports making it easier to implement new features:


#### Abstraction #3
* Description:

* How it supports making it easier to implement new features:




