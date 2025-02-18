package cellsociety.model.config;

import java.util.Map;

/**@author Billy McCune
 * Immutable record representing a cell in the simulation grid.
 * <p>
 * Each cell is characterized by:
 * <ul>
 *   <li><b>State</b>: an integer representing the current state of the cell.</li>
 *   <li><b>Properties</b>: a map of additional properties associated with the cell, where each key is a property name and each value is its corresponding double value.</li>
 * </ul>
 *
 * @param state      the state of the cell
 * @param properties the additional properties of the cell
 */
public record CellRecord(int state, Map<String,Double> properties) {}
