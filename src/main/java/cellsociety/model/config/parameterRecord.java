package cellsociety.model.config;

import java.util.Map;

public class parameterRecord {
    public record parameters(Map<String,Double> myDoubleParameters, Map<String,String> myStringParameters) {}
}
