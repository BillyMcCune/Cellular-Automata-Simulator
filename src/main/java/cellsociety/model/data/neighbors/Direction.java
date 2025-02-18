package cellsociety.model.data.neighbors;

/**
 * A simple record to hold dx and dy as an integer coordinate pair.
 *
 * @param dy The row offset to store
 * @param dx The col offset to store
 * @author Jacob You
 */
public record Direction(int dy, int dx) {

  @Override
  public String toString() {
    return "%s,%s".formatted(dy, dx);
  }
}
