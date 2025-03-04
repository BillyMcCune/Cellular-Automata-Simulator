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

public class RaycastImplementor<T extends Enum<T> & State> {

  private GridShape shape;
  private EdgeType boundary;

  public RaycastImplementor(GridShape shape, EdgeType boundary) {
    this.shape = shape;
    this.boundary = boundary;
  }

  public void setShape(GridShape shape) {
    this.shape = shape;
  }

  public void setBoundary(EdgeType boundary) {
    this.boundary = boundary;
  }

  public RaycastStrategy<T> getStrategy() {
    try {
      String basePackage = "cellsociety.model.data.neighbors.raycasting.";
      String shapeName = shape.name().substring(0, 1).toUpperCase() +
          shape.name().substring(1).toLowerCase();
      String className = basePackage + shapeName + "RaycastStrategy";
      Class<?> clazz = Class.forName(className);
      return (RaycastStrategy<T>) clazz.getDeclaredConstructor().newInstance();
    }
    catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
      // Should never happen
      e.printStackTrace();
      return null;
    }
  }

  public Map<Direction, Cell<T>> raycast(Grid<T> grid,
      int startRow,
      int startCol,
      Direction rawDir,
      int steps){
    return getStrategy().doRaycast(grid, startRow, startCol, rawDir, steps, boundary);
  }

  public List<Direction> getDefaultRawDirections(int startRow, int startCol) {
    return getStrategy().getDefaultRawDirections(startRow, startCol);
  }
}
