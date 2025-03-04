package cellsociety.model.logic.helpers;

import cellsociety.model.data.cells.CellQueueRecord;

public class InfectionRecord extends CellQueueRecord {
  private double speciesID;
  private double duration;

  public InfectionRecord(double speciesID, double duration) {
    this.speciesID = speciesID;
    this.duration = duration;
  }

  public double getSpeciesID() {
    return speciesID;
  }

  public double getDuration() {
    return duration;
  }

  public boolean decrementDuration() {
    this.duration -= 1;
    return this.duration <= 0;
  }
}
