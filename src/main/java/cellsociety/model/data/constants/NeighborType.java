package cellsociety.model.data.constants;

/**
 * Defines the types of neighborhood structures used for determining neighboring cells in a grid. -
 * {@code MOORE}: Includes all adjacent cells, considering diagonal neighbors. - {@code NEUMANN}:
 * Includes only orthogonal neighbors (up, down, left, right). This enum is used to determine how
 * neighbor calculations are performed in various grid-based simulations.
 *
 * @author Jacob You
 */
public enum NeighborType {
  MOORE,
  NEUMANN;
}
