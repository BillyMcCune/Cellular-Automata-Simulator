package cellsociety.model.data.neighbors.raycasting;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.constants.EdgeType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.states.State;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * Handles the selection and execution of the appropriate raycasting strategy based on the given
 * grid shape and boundary type.
 *
 * @param <T> the enum type representing the cell state.
 * @author Jacob You
 */
public class RaycastImplementor<T extends Enum<T> & State> {

  private GridShape shape;
  private EdgeType boundary;

  /**
   * Constructs a RaycastImplementor with a specified grid shape and boundary type.
   *
   * @param shape    the shape of the grid.
   * @param boundary the type of boundary handling for raycasting.
   */
  public RaycastImplementor(GridShape shape, EdgeType boundary) {
    this.shape = shape;
    this.boundary = boundary;
  }

  /**
   * Updates the grid shape used for selecting the raycasting strategy.
   *
   * @param shape the new grid shape.
   */
  public void setShape(GridShape shape) {
    this.shape = shape;
  }

  /**
   * Updates the boundary type used for raycasting operations.
   *
   * @param boundary the new boundary type.
   */
  public void setBoundary(EdgeType boundary) {
    this.boundary = boundary;
  }

  /**
   * Dynamically retrieves and instantiates the appropriate raycasting strategy based on the current
   * grid shape.
   *
   * @return an instance of the appropriate {@link RaycastStrategy}.
   */
  public RaycastStrategy<T> getStrategy() {
    try {
      String basePackage = "cellsociety.model.data.neighbors.raycasting.";
      String shapeName = shape.name().substring(0, 1).toUpperCase() +
          shape.name().substring(1).toLowerCase();
      String className = basePackage + shapeName + "RaycastStrategy";
      Class<?> clazz = Class.forName(className);
      return (RaycastStrategy<T>) clazz.getDeclaredConstructor().newInstance();
    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
             InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Executes a raycasting operation using the selected strategy.
   *
   * @param grid     the grid to perform raycasting on.
   * @param startRow the starting row of the raycast.
   * @param startCol the starting column of the raycast.
   * @param rawDir   the direction to raycast towards.
   * @param steps    the number of steps to extend the raycast.
   * @return a map of directions to the cells reached by the raycast.
   */
  public Map<Direction, Cell<T>> raycast(Grid<T> grid,
      int startRow,
      int startCol,
      Direction rawDir,
      int steps) {
    return getStrategy().doRaycast(grid, startRow, startCol, rawDir, steps, boundary);
  }

  /**
   * Retrieves the default raycasting directions for a given starting position.
   *
   * @param startRow the row to get the default directions for.
   * @param startCol the column to get the default directions for.
   * @return a list of default raw directions for raycasting.
   */
  public List<Direction> getDefaultRawDirections(int startRow, int startCol) {
    return getStrategy().getDefaultRawDirections(startRow, startCol);
  }
}