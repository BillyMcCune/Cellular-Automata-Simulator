package cellsociety.model.config;

import java.util.Map;

/**
 * @author Billy McCune
 * Immutable record representing simulation parameters.
 * <p>
 * This record encapsulates two maps:
 * <ul>
 *   <li><b>Double Parameters</b>: A mapping of parameter names to their double values.</li>
 *   <li><b>String Parameters</b>: A mapping of parameter names to their string values.</li>
 * </ul>
 * These parameters are used to customize various aspects of the simulation.
 *
 * @param myDoubleParameters the mapping of parameter names to their double values
 * @param myStringParameters the mapping of parameter names to their string values
 */
    public record ParameterRecord(Map<String,Double> myDoubleParameters, Map<String,String> myStringParameters) {}

