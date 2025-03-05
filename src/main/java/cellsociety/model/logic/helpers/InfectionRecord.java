package cellsociety.model.logic.helpers;

import cellsociety.model.data.cells.CellQueueRecord;

/**
 * Represents an infection record containing information about the species responsible for an
 * infection and the remaining duration of that infection. This record is used to track infection
 * states within the simulation.
 */
public class InfectionRecord extends CellQueueRecord {

  private double speciesID;
  private double duration;

  /**
   * Constructs an {@code InfectionRecord} with the specified species ID and infection duration.
   *
   * @param speciesID the ID of the species causing the infection
   * @param duration  the duration of the infection
   */
  public InfectionRecord(double speciesID, double duration) {
    this.speciesID = speciesID;
    this.duration = duration;
  }

  /**
   * Returns the species ID responsible for the infection.
   *
   * @return the species ID
   */
  public double getSpeciesID() {
    return speciesID;
  }

  /**
   * Returns the remaining duration of the infection.
   *
   * @return the infection duration
   */
  public double getDuration() {
    return duration;
  }

  /**
   * Decrements the duration of the infection by one unit.
   *
   * @return {@code true} if the duration is less than or equal to 0 after decrementing, indicating
   * that the infection has ended; {@code false} otherwise.
   */
  public boolean decrementDuration() {
    this.duration -= 1;
    return this.duration <= 0;
  }
}
