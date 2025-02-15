package cellsociety.model.config;

import java.util.Map;

public class cellRecord {
  public record CellPropertyRecord(int state, Map<String,Double> properties) {
  }
}
