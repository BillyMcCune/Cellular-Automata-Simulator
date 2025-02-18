package cellsociety.model.data.neighbors;

/**
 * A simple record to hold row and col as an integer coordinate pair.
 *
 * @param col The column number to store
 * @param row The row number to store
 * @author Jacob You
 */
public record Direction(int row, int col) {

  @Override
  public String toString() {
    return "%s,%s".formatted(row, col);
  }
}
