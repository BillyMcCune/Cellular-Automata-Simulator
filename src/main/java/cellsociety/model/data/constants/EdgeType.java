package cellsociety.model.data.constants;

/**
 * Represents the different types of edge behaviors for a grid. - {@code BASE}: Standard grid edges
 * where cells beyond the boundary do not exist. - {@code TORUS}: Wraparound edges where the grid
 * behaves like a torus, connecting opposite edges. - {@code MIRROR}: Reflective edges where cells
 * at the boundary mirror their adjacent values. This enum is used to determine the behavior of
 * cells at the edges of the grid.
 *
 * @author Jacob You
 */
public enum EdgeType {
  BASE,
  TORUS,
  MIRROR;
}
