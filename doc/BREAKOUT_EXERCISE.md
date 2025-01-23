# Breakout Abstractions Lab Discussion
#### Billy McCune, Hsuan-Kai Liao, Jacob You


### Block

This superclass's purpose as an abstraction:
```java
public abstract class Block { 
  public abstract void hit();
  // When the block is hit, perform the event 
  // depending on the block (e.g decrement hitpoints, powerup)
  
  public abstract boolean isDestroyed();
  // Returns whether the block was destroyed
}
```

#### Subclasses

Each subclass's high-level behavorial differences from the superclass

For each block, hit() can have a different effect. For shielded blocks, it could decrement hitpoints.
isDestroyed() can also be different. For powerup blocks, it could call a method to activate a powerup,
or for an bomb block, it could activate an explosion.

#### Affect on Game/Level class

Which methods are simplified by using this abstraction and why

Methods used to classify blocks such as hasPowerup() or isExploding() 
would no longer be necessary with subclasses.


### Power-up

This superclass's purpose as an abstraction:
```java
public class PowerUp { 
  public abstract void powerup();
  // applies the powerup to the modified object
  
  public abstract void removePowerup();
  // removes the powerup from the modified object
}
```

#### Subclasses

Each subclass's high-level behavorial differences from the superclass

The powerup() method would do different things, depending on the subclass.
The removePowerup() method would also do different things, removing the powerup from the object it modified

#### Affect on Game/Level class

Which methods are simplified by using this abstraction and why

powerup() is simplified, as you do not have to check what powerup the powerup is every time.
removePowerup() is also simplified, as with each powerup, you know which object to modify



### Others?
