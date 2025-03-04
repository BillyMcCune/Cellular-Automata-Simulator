package modeltests.logic;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.config.CellRecord;
import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.cells.CellFactory;
import cellsociety.model.data.constants.EdgeType;
import cellsociety.model.data.constants.GridShape;
import cellsociety.model.data.constants.NeighborType;
import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.states.DarwinState;
import cellsociety.model.logic.DarwinLogic;
import cellsociety.model.logic.helpers.DarwinHelper.InstructionResult;
import cellsociety.model.logic.helpers.InfectionRecord;
import java.lang.reflect.Field;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DarwinLogicTest {

  private DarwinLogic darwinLogic;
  private DarwinLogic darwinBaseEdgeLogic;
  private Grid<DarwinState> grid;
  private Grid<DarwinState> baseEdgeGrid;
  private CellFactory<DarwinState> cellFactory;
  private ParameterRecord parameters;

  private final Map<Integer, List<String>> testSpeciesPrograms = new HashMap<>();

  @BeforeEach
  public void setUp() throws Exception {
    Map<String, Double> doubleParams = new HashMap<>();
    doubleParams.put("nearbyAhead", 2.0);
    Map<String, String> stringParams = new HashMap<>();
    parameters = new ParameterRecord(doubleParams, stringParams);

    // Create a 3x3 grid.
    List<List<CellRecord>> rawGrid = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      List<CellRecord> row = new ArrayList<>();
      for (int j = 0; j < 3; j++) {
        row.add(new CellRecord(0, new HashMap<>()));
      }
      rawGrid.add(row);
    }

    cellFactory = new CellFactory<>(DarwinState.class);
    grid = new Grid<>(rawGrid, cellFactory, GridShape.SQUARE, NeighborType.MOORE, EdgeType.TORUS);
    baseEdgeGrid = new Grid<>(rawGrid, cellFactory, GridShape.SQUARE, NeighborType.MOORE, EdgeType.BASE);

    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 0; j < grid.getNumCols(); j++) {
        Cell<DarwinState> cell = grid.getCell(i, j);
        cell.setProperty("speciesID", 0);
      }
    }

    Cell<DarwinState> testCell1 = grid.getCell(1, 1);
    testCell1.setProperty("speciesID", 1);
    testCell1.setProperty("instructionIndex", 1);
    testCell1.setProperty("orientation", 0);

    Cell<DarwinState> testBaseCell1 = baseEdgeGrid.getCell(1, 1);
    testBaseCell1.setProperty("speciesID", 1);
    testBaseCell1.setProperty("instructionIndex", 1);
    testBaseCell1.setProperty("orientation", 0);

    Cell<DarwinState> testCell2 = grid.getCell(1, 2);
    testCell2.setProperty("speciesID", 2);
    testCell2.setProperty("instructionIndex", 1);
    testCell2.setProperty("orientation", 0);

    darwinLogic = new DarwinLogic(grid, parameters);
    darwinBaseEdgeLogic = new DarwinLogic(baseEdgeGrid, parameters);
  }

  @Test
  public void DarwinLogic_CellMove1FacingUp_OriginalPropertiesMoveAndCellClears() {
    List<String> moveProgram = new ArrayList<>();
    moveProgram.add("MOVE 1");
    testSpeciesPrograms.put(1, moveProgram);
    darwinLogic.assignSpeciesPrograms(testSpeciesPrograms);

    darwinLogic.update();

    Cell<DarwinState> destination = grid.getCell(0, 1);

    Cell<DarwinState> original = grid.getCell(1, 1);
    Map<String, Double> originalProps = original.getAllProperties();
    assertEquals(1, destination.getProperty("speciesID"),
        "Destination cell should have speciesID 1 after MOVE instruction is executed.");
    assertTrue(originalProps == null || originalProps.isEmpty(),
        "Original cell should be cleared after movement.");
    assertEquals(2, destination.getProperty("instructionIndex"),
        "Destination cell should increment instructions");
    assertEquals(0, destination.getProperty("orientation"),
        "Original cell should have original orientation 0 after movement.");
  }

  @Test
  public void DarwinLogic_CellRotateLeft180_SetsOrientation180() throws Exception {
    List<String> rotateProgram = new ArrayList<>();
    rotateProgram.add("LEFT 180");
    rotateProgram.add("MOVE 1");
    rotateProgram.add("GO 1");
    testSpeciesPrograms.put(1, rotateProgram);
    darwinLogic.assignSpeciesPrograms(testSpeciesPrograms);

    darwinLogic.update();

    Cell<DarwinState> cell = grid.getCell(1, 1);

    assertEquals(180, cell.getProperty("orientation"),
        "Cell should have rotated to 180 degrees after LEFT 180 instruction.");

    darwinLogic.update();

    Cell<DarwinState> destination = grid.getCell(2, 1);
    assertEquals(1, destination.getProperty("speciesID"),
        "Cell should have moved down after rotation");
  }

  @Test
  public void DarwinLogic_3x3TorusMove2Forward_LoopsToBottom() throws Exception {
    List<String> rotateProgram = new ArrayList<>();
    rotateProgram.add("MOVE 2");
    testSpeciesPrograms.put(1, rotateProgram);
    darwinLogic.assignSpeciesPrograms(testSpeciesPrograms);

    darwinLogic.update();

    Cell<DarwinState> destination = grid.getCell(2, 1);
    assertEquals(1, destination.getProperty("speciesID"),
        "Cell should looped to the bottom after moving up twice");
  }

  @Test
  public void DarwinLogic_IfEnemyRotatesLeft90_SetsOrientation90() throws Exception {
    List<String> enemyRotateProgram = new ArrayList<>();
    enemyRotateProgram.add("RIGHT 90");
    enemyRotateProgram.add("IFENEMY 4");
    enemyRotateProgram.add("GO 1");
    enemyRotateProgram.add("LEFT 180");
    enemyRotateProgram.add("GO 1");
    testSpeciesPrograms.put(1, enemyRotateProgram);
    darwinLogic.assignSpeciesPrograms(testSpeciesPrograms);

    darwinLogic.update();
    Cell<DarwinState> cell = grid.getCell(1, 1);

    assertEquals(270, cell.getProperty("orientation"),
        "Cell should have rotated to 270 degrees after RIGHT 90.");

    darwinLogic.update();

    assertEquals(90, cell.getProperty("orientation"),
        "Cell should have rotated to 90 degrees after LEFT 180 due to enemy.");

    darwinLogic.update();

    assertEquals(0, cell.getProperty("orientation"),
        "Cell should have rotated to 0 degrees after RIGHT 90 after GO loop.");
  }

  @Test
  public void DarwinLogic_InfectNeighbor1Step_NeighborChangesSpecies1Step() throws Exception {
    List<String> moveProgram1 = new ArrayList<>();
    moveProgram1.add("RIGHT 90");
    moveProgram1.add("INFECT 2");
    moveProgram1.add("RIGHT 90");
    moveProgram1.add("RIGHT 0");
    moveProgram1.add("GO 4");
    testSpeciesPrograms.put(1, moveProgram1);
    List<String> moveProgram2 = new ArrayList<>();
    moveProgram2.add("LEFT 90");
    testSpeciesPrograms.put(2, moveProgram2);
    darwinLogic.assignSpeciesPrograms(testSpeciesPrograms);

    Cell<DarwinState> starting = grid.getCell(1, 1);
    Cell<DarwinState> infected = grid.getCell(1, 2);

    darwinLogic.update();
    assertEquals(90, infected.getProperty("orientation"),
        "Infected cell should have orientation 90 after LEFT 90.");
    assertEquals(270, starting.getProperty("orientation"),
        "Starting cell should have orientation 270 after RIGHT 90.");

    darwinLogic.update();
    assertEquals(1, infected.getProperty("speciesID"),
        "Infected cell should have speciesID 1 after INFECT instruction is executed.");
    assertEquals(1, infected.getProperty("instructionIndex"),
        "Infected cell should start instructions at 1");
    assertEquals(180, infected.getProperty("orientation"),
        "Infected cell should keep orientation 180 after LEFT 90 still happens.");

    darwinLogic.update();
    assertEquals(1, infected.getProperty("speciesID"),
        "Infected cell should have speciesID 1 after 2 step infection.");
    assertEquals(90, infected.getProperty("orientation"),
        "Infected cell should now have orientation 90 after RIGHT 90 from instruction set 1.");
    assertEquals(180, starting.getProperty("orientation"),
        "Starting cell should have orientation 180 after RIGHT 90.");

    darwinLogic.update();
    assertEquals(2, infected.getProperty("speciesID"),
        "Infected cell should have reverted to speciesID 2 after 2 step infection.");
    assertEquals(1, infected.getProperty("instructionIndex"),
        "Infected cell should revert to index 1 after 2 step infection.");
    assertEquals(90, infected.getProperty("orientation"),
        "Infected cell should still be at 90 after attempting INFECT");
  }

  @Test
  public void DarwinLogic_IfEmptyRotatesLeft90_WhenCellHasEmptyNeighbor() throws Exception {
    List<String> ifEmptyProgram = new ArrayList<>();
    ifEmptyProgram.add("IFEMPTY 2");
    ifEmptyProgram.add("LEFT 90");
    ifEmptyProgram.add("GO 1");
    testSpeciesPrograms.put(1, ifEmptyProgram);
    darwinLogic.assignSpeciesPrograms(testSpeciesPrograms);

    Cell<DarwinState> testCell = grid.getCell(1, 1);

    assertEquals(0, testCell.getProperty("orientation"),
        "Initial orientation should be 0 degrees.");

    darwinLogic.update();

    assertEquals(90, testCell.getProperty("orientation"),
        "Cell should rotate left to 90 degrees due to IFEMPTY.");
  }

  @Test
  public void DarwinLogic_IfEmptyNeighborOccupied_DoesNotTrigger() throws Exception {
    List<String> ifEmptyProgram = new ArrayList<>();
    ifEmptyProgram.add("RIGHT 90");
    ifEmptyProgram.add("IFEMPTY 4");
    ifEmptyProgram.add("LEFT 90");
    ifEmptyProgram.add("RIGHT 90");
    testSpeciesPrograms.put(1, ifEmptyProgram);
    darwinLogic.assignSpeciesPrograms(testSpeciesPrograms);

    Cell<DarwinState> testCell = grid.getCell(1, 1);

    darwinLogic.update();
    assertEquals(270, testCell.getProperty("orientation"),
        "Orientation should be 270 degrees from right 90.");

    darwinLogic.update();

    assertEquals(0, testCell.getProperty("orientation"),
        "Orientation should flip left 90 degrees because IFEMPTY is false");
  }

  @Test
  public void DarwinLogic_IfWallInTorusGrid_DoesNotTrigger() throws Exception {
    List<String> ifWallProgram = new ArrayList<>();
    ifWallProgram.add("IFWALL 3");
    ifWallProgram.add("LEFT 90");
    ifWallProgram.add("RIGHT 90");
    ifWallProgram.add("GO 1");
    testSpeciesPrograms.put(1, ifWallProgram);
    darwinLogic.assignSpeciesPrograms(testSpeciesPrograms);

    Cell<DarwinState> testCell = grid.getCell(1, 1);

    assertEquals(0, testCell.getProperty("orientation"),
        "Initial orientation should be 0 degrees.");

    darwinLogic.update();

    assertEquals(90, testCell.getProperty("orientation"),
        "Orientation should flip left 90 degrees because torus prevents walls from existing.");
  }

  @Test
  public void DarwinLogic_IfWall_InBoundaryGrid() throws Exception {
    List<String> ifWallProgram = new ArrayList<>();
    ifWallProgram.add("IFWALL 3");
    ifWallProgram.add("LEFT 90");
    ifWallProgram.add("RIGHT 90");
    ifWallProgram.add("GO 1");
    testSpeciesPrograms.put(1, ifWallProgram);
    darwinBaseEdgeLogic.assignSpeciesPrograms(testSpeciesPrograms);

    Cell<DarwinState> testCell = baseEdgeGrid.getCell(1, 1);

    assertEquals(0, testCell.getProperty("orientation"),
        "Initial orientation should be 0 degrees.");

    darwinBaseEdgeLogic.update();

    assertEquals(270, testCell.getProperty("orientation"),
        "Orientation should flip right 90 degrees because of the wall.");
  }

  @Test
  public void DarwinLogic_SmallAngleOrientation_AutoRoundToZero() throws Exception {
    List<String> moveProgram = new ArrayList<>();
    moveProgram.add("LEFT 1");
    moveProgram.add("MOVE 1");
    moveProgram.add("LEFT 43");
    moveProgram.add("MOVE 1");
    testSpeciesPrograms.put(1, moveProgram);
    darwinLogic.assignSpeciesPrograms(testSpeciesPrograms);

    Cell<DarwinState> testCell = grid.getCell(1, 1);
    Cell<DarwinState> destination = grid.getCell(0, 1);
    Cell<DarwinState> secondDestination = grid.getCell(2, 1);

    darwinLogic.update();
    assertEquals(1, testCell.getProperty("orientation"),
        "Orientation should remain 1 after LEFT 1");

    darwinLogic.update();
    assertEquals(1, destination.getProperty("speciesID"),
        "Cell should move up, as 1 is closer to up");

    darwinLogic.update();
    assertEquals(44, destination.getProperty("orientation"),
        "Orientation should become 44 after LEFT 43");

    darwinLogic.update();
    assertEquals(1, secondDestination.getProperty("speciesID"),
        "Cell should move up, as 44 is closer to up");
  }

  @Test
  public void DarwinLogic_SquareAngleAbove45_AutoRoundTo90() throws Exception {
    List<String> moveProgram = new ArrayList<>();
    moveProgram.add("LEFT 45");
    moveProgram.add("MOVE 1");
    moveProgram.add("LEFT 1");
    moveProgram.add("MOVE 1");
    testSpeciesPrograms.put(1, moveProgram);
    darwinLogic.assignSpeciesPrograms(testSpeciesPrograms);

    Cell<DarwinState> testCell = grid.getCell(1, 1);
    Cell<DarwinState> destination = grid.getCell(0, 1);
    Cell<DarwinState> secondDestination = grid.getCell(0, 0);

    darwinLogic.update();
    assertEquals(45, testCell.getProperty("orientation"),
        "Orientation should remain 45 after LEFT 45");

    darwinLogic.update();
    assertEquals(1, destination.getProperty("speciesID"),
        "Cell should move left, as 45 is closer to 0 in this implementation");

    darwinLogic.update();
    assertEquals(46, destination.getProperty("orientation"),
        "Orientation should become 46 after LEFT 46");

    darwinLogic.update();
    assertEquals(1, secondDestination.getProperty("speciesID"),
        "Cell should move left, as 46 is closer to 90 in this implementation");
  }
}
