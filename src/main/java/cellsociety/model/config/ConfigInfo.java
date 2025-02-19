package cellsociety.model.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Billy McCune
 * Immutable record representing simulation configuration information.
 * <p>
 * This record encapsulates all data required to configure a simulation including simulation type,
 * title, author, description, grid dimensions, tick speed, initial grid state, simulation-specific parameters,
 * accepted cell states, and the source file name.
 * <p>
 * Note: This record is immutable and does not support setters or the singleton pattern.
 * All fields must be provided at construction.
 *
 * @param myType         the type of simulation
 * @param myTitle        the title of the simulation
 * @param myAuthor       the author of the simulation
 * @param myDescription  a description of the simulation
 * @param myGridWidth    the width of the simulation grid
 * @param myGridHeight   the height of the simulation grid
 * @param myTickSpeed    the default tick speed (simulation update rate)
 * @param myGrid         the initial grid configuration represented as a 2D list of {@link CellRecord}
 * @param myParameters   simulation-specific parameters encapsulated in a {@link ParameterRecord}
 * @param acceptedStates the set of accepted cell states for the simulation
 * @param myFileName     the file name from which the configuration was loaded
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

  /**
   * Enumeration of supported simulation types.
   */
  public enum SimulationType {
    LIFE, PERCOLATION, FIRE, SEGREGATION, WATOR, FALLING, SUGAR, BACTERIA
  }

}
