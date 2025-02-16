package cellsociety.model.config;

import java.util.Map;

    public record ParameterRecord(Map<String,Double> myDoubleParameters, Map<String,String> myStringParameters) {}

