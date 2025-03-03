package cellsociety.model.data.neighbors.raycasting;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.constants.BoundaryType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.neighbors.raycasting.RaycastStrategy;
import cellsociety.model.data.states.State;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class RaycastImplementor<T extends Enum<T> & State> {

  private GridShape shape;
  private BoundaryType boundary;

  public RaycastImplementor(GridShape shape, BoundaryType boundary) {
    this.shape = shape;
    this.boundary = boundary;
  }

  public void setShape(GridShape shape) {
    this.shape = shape;
  }

  public void setBoundary(BoundaryType boundary) {
    this.boundary = boundary;
  }

  public RaycastStrategy<T> getStrategy()
      throws ClassNotFoundException, NoSuchMethodException,
      InvocationTargetException, InstantiationException, IllegalAccessException {
    String basePackage = "cellsociety.model.data.neighbors.raycasting.";
    String shapeName = shape.name().substring(0, 1).toUpperCase() +
        shape.name().substring(1).toLowerCase();
    String className = basePackage + shapeName + "RaycastStrategy";
    Class<?> clazz = Class.forName(className);
    return (RaycastStrategy<T>) clazz.getDeclaredConstructor().newInstance();
  }

  public List<Cell<T>> raycast(Grid<T> grid,
      int startRow,
      int startCol,
      Direction rawDir,
      int steps)
      throws ClassNotFoundException, NoSuchMethodException,
      InvocationTargetException, InstantiationException, IllegalAccessException {

    return getStrategy().doRaycast(grid, startRow, startCol, rawDir, steps, boundary);
  }

  public List<Direction> getDefaultRawDirections(int startRow, int startCol)
      throws ClassNotFoundException, NoSuchMethodException,
      InvocationTargetException, InstantiationException, IllegalAccessException {
    return getStrategy().getDefaultRawDirections(startRow, startCol);
  }
}
