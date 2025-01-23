# Rock Paper Scissors Lab Discussion
#### Billy McCune wrm29, Jacob You jay27, Hsuan-Kai Liao hl475


### High Level Design Goals

Weapon class: Contains data about the weapon and what it beats/loses to)
Player class: Contains data about player, name, hand, etc.
GameInput class: Takes in information from a config file
GameLogic class: Calculates who wins and who loses based on hands
GUI class: Shows the users who wins/loses and provides an input for each player
Main class: Start the program

### CRC Card Classes

This class's purpose is to represent the weapon class, containing what the weapons
wins and loses against.

| Weapon                                                                 |           |
|------------------------------------------------------------------------|-----------|
| Weapon(String name, ArrayList<> winsAgainst, ArrayList<> losesAgainst) | Main      |
| ArrayList getWinsAgainst()                                             | GameLogic |
| ArrayList getLosesAgainst()                                            | Weapon    |
| void setWinAgainst(ArrayList<> newWeapons)                             | GUI       |
| void setLoseAgainst(ArrayList<> newWeapons)                            |           |
| String toString()                                                      |           |

This class's purpose is to contain data about player name, player wins, player losses

| Player              | Collaborators |
|---------------------|---------------|
| Player(String name) | Main          |
| void setWeapon()    | GUI           |
| Weapon getWeapon()  |               |
| int getWins()       |               |
| int getLosses()     |               |

This class's purpose is to take in a file and pass information about weapons and players

| GameInput                            | Collaborators |
|--------------------------------------|---------------|
| HashMap<> parseFile(String fileName) | Main          |

This class's purpose is to handle game logic on who wins and who loses in one hand

| GameLogic                                     | Collaborators |
|-----------------------------------------------|---------------|
| HashMap<> getResults(HashMap<String, Weapon>) | GUI           |
|                                               | Main          |

This class's purpose is to act as the GUI, allowing users to throw weapons and see who wins and loses

| GUI                               | Collaborators |
|-----------------------------------|---------------|
| void showWinLoss(HashMap results) | Main          |
| String getWeaponInput()           | GameLogic     |
|                                   | Player        |

This class's purpose is to start the program, contains the game data passed by GameInput, and has the game loop

| Main                                  | Collaborators |
|---------------------------------------|---------------|
| static void main(String[] args)       | GameInput     |
| void start(Stage stage)               | GUI           |
| void step(double elapsedTime)         | Player        |
| void getWeaponInteractions(HashMap<>) | Weapon        |



This class's purpose or value is to represent a customer's order:
```java
public class Order {
     // returns whether or not the given items are available to order
     public boolean isInStock (OrderLine items)
     // sums the price of all the given items
     public double getTotalPrice (OrderLine items)
     // returns whether or not the customer's payment is valid
     public boolean isValidPayment (Customer customer)
     // dispatches the items to be ordered to the customer's selected address
     public void deliverTo (OrderLine items, Customer customer)
 }
 ```

This class's purpose or value is to manage something:
```java
public class Something {
     // sums the numbers in the given data
     public int getTotal (Collection<Integer> data)
	 // creates an order from the given data
     public Order makeOrder (String structuredData)
 }
```


### Use Cases

* A new game is started with five players, their scores are reset to 0.
 ```java
 Something thing = new Something();
 Order o = thing.makeOrder("coffee,large,black");
 o.update(13);
 ```

* A player chooses his RPS "weapon" with which he wants to play for this round.
 ```java
 Something thing = new Something();
 Order o = thing.makeOrder("coffee,large,black");
 o.update(13);
 ```

* Given three players' choices, one player wins the round, and their scores are updated.
 ```java
 Something thing = new Something();
 Order o = thing.makeOrder("coffee,large,black");
 o.update(13);
 ```

* A new choice is added to an existing game and its relationship to all the other choices is updated.
 ```java
 Something thing = new Something();
 Order o = thing.makeOrder("coffee,large,black");
 o.update(13);
 ```

* A new game is added to the system, with its own relationships for its all its "weapons".
 ```java
 Something thing = new Something();
 Order o = thing.makeOrder("coffee,large,black");
 o.update(13);
 ```