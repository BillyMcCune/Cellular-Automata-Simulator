package cellsociety.model.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Record version of ConfigInfo.
 * <p>
 * Note: This record is immutable and does not support setters or the singleton pattern.
 * All fields must be provided at construction.
 */
public record ConfigInfo(
    SimulationType myType,
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
) {

  public enum SimulationType {
    LIFE, PERCOLATION, FIRE, SEGREGATION, WATOR
  }

}
