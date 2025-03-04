package cellsociety.model.data.constants;


/**
 * Defines the possible grid shapes that can be used in the simulation. - {@code SQUARE}: A standard
 * grid where each cell has four orthogonal neighbors. - {@code HEX}: A hexagonal grid where each
 * cell has six neighbors. - {@code TRI}: A triangular grid where each cell has up to six neighbors,
 * depending on orientation. This enum is used to determine the raycasting and neighbor computation
 * strategy.
 *
 * @author Jacob You
 */
public enum GridShape {
  SQUARE,
  HEX,
  TRI;
}
