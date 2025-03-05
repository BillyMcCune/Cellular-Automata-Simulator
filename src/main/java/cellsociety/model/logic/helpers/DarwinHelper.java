package cellsociety.model.logic.helpers;

import cellsociety.model.data.Grid;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.neighbors.Direction;
import cellsociety.model.data.states.DarwinState;
import cellsociety.model.logic.DarwinLogic;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.*;

/**
 * Helper class to manage Darwin species and programs.
 *
 * @author Jacob You
 */
public class DarwinHelper {

  private static final String SPECIES_DIR = "src/main/resources/speciesdata/";
  private Map<Integer, List<String>> speciesPrograms = new HashMap<>();
  private final Map<String, Method> instructionMethods = new HashMap<>();
  private final DarwinLogic darwinLogic;
  private final Grid<DarwinState> grid;

  /**
   * Constructs a {@code DarwinHelper} instance with the specified {@link DarwinLogic} and
   * {@link Grid}. Loads species programs from files and registers instruction methods.
   *
   * @param darwinLogic the DarwinLogic instance controlling the simulation
   * @param grid        the grid containing DarwinState cells
   */
  public DarwinHelper(DarwinLogic darwinLogic, Grid<DarwinState> grid) {
    this.darwinLogic = darwinLogic;
    this.grid = grid;
    loadSpeciesPrograms();
    registerInstructions();
  }

  /**
   * Constructs a {@code DarwinHelper} instance with the provided species programs map,
   * {@link DarwinLogic}, and {@link Grid}.
   *
   * @param species     a map of species IDs to their program instructions
   * @param darwinLogic the DarwinLogic instance controlling the simulation
   * @param grid        the grid containing DarwinState cells
   */
  public DarwinHelper(Map<Integer, List<String>> species, DarwinLogic darwinLogic,
      Grid<DarwinState> grid) {
    this.darwinLogic = darwinLogic;
    this.grid = grid;
    this.speciesPrograms = species;
    registerInstructions();
  }

  /**
   * Reads all species programs from files and registers them. This method loads all .txt files from
   * the species data directory, reads the instructions, and assigns a species ID to each program.
   */
  private void loadSpeciesPrograms() {
    try {
      List<Path> speciesFiles = getSpeciesFiles();
      int speciesID = 1;
      for (Path file : speciesFiles) {
        List<String> instructions = readProgram(file);
        speciesPrograms.put(speciesID, instructions);
        speciesID++;
      }
    } catch (IOException e) {
      throw new RuntimeException("Error loading species programs", e);
    }
  }

  /**
   * Returns all files in the species data directory.
   *
   * @return a list of file paths containing species programs
   * @throws IOException if an I/O error occurs while accessing the directory
   */
  private List<Path> getSpeciesFiles() throws IOException {
    List<Path> files = new ArrayList<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(SPECIES_DIR), "*.txt")) {
      for (Path path : stream) {
        files.add(path);
      }
    }
    files.sort(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()));
    return files;
  }

  /**
   * Reads the program instructions from a file.
   *
   * @param file the path to the species program file
   * @return a list of instruction strings read from the file
   * @throws IOException if an I/O error occurs while reading the file
   */
  private List<String> readProgram(Path file) throws IOException {
    List<String> instructions = new ArrayList<>();
    try (BufferedReader reader = Files.newBufferedReader(file)) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!line.isEmpty() && !line.startsWith("#")) {
          instructions.add(line);
        }
      }
    }
    return instructions;
  }

  /**
   * Gets the program instructions for the specified species ID.
   *
   * @param speciesID the identifier of the species
   * @return a list of instruction strings for the species, or an empty list if not found
   */
  public List<String> getProgram(int speciesID) {
    return speciesPrograms.getOrDefault(speciesID, Collections.emptyList());
  }

  /**
   * Registers instruction methods by scanning all declared methods that start with "execute". Maps
   * each instruction name and its alias to the corresponding method.
   */
  private void registerInstructions() {
    for (Method method : this.getClass().getDeclaredMethods()) {
      if (method.getName().startsWith("execute")) {
        String instructionName = method.getName().substring(7).toUpperCase();
        instructionMethods.put(instructionName, method);
        instructionMethods.put(getAlias(instructionName), method);
      }
    }
  }

  /**
   * Converts an instruction name into its alias if one exists. Otherwise, returns the original
   * instruction.
   *
   * @param instruction the original instruction name
   * @return the alias for the instruction, or the original instruction if no alias exists
   */
  private String getAlias(String instruction) {
    return switch (instruction) {
      case "MOVE" -> "MV";
      case "LEFT" -> "LT";
      case "RIGHT" -> "RT";
      case "INFECT" -> "INF";
      case "IFEMPTY" -> "EMP?";
      case "IFWALL" -> "WL?";
      case "IFSAME" -> "SM?";
      case "IFENEMY" -> "EMY?";
      case "IFRANDOM" -> "RND?";
      case "GO" -> "GO";
      default -> instruction;
    };
  }

  /**
   * Processes a Darwin instruction dynamically. Splits the instruction line into command and
   * argument, looks up the corresponding method, and invokes it with the given cell and argument.
   *
   * @param instructionLine the instruction line to process
   * @param cell            the cell on which to execute the instruction
   * @return the result of processing the instruction
   * @throws IllegalArgumentException if the instruction format is invalid or the command is
   *                                  unknown
   */
  private InstructionResult processInstruction(String instructionLine, Cell<DarwinState> cell) {
    String[] parts = instructionLine.split(" ");
    String command = parts[0].toUpperCase();
    if (parts.length != 2 || !parts[1].matches("-?\\d+")) {
      throw new IllegalArgumentException("Invalid argument for command: " + instructionLine);
    }
    Method method = instructionMethods.get(command);
    if (method == null) {
      throw new IllegalArgumentException("Unknown command: " + command);
    }
    try {
      int argument = Integer.parseInt(parts[1]);
      method.setAccessible(true);
      return (InstructionResult) method.invoke(this, cell, argument);
    } catch (InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException("Failed to execute instruction: " + command, e);
    }
  }

  /**
   * Processes the current instruction for the given cell. Retrieves the species program based on
   * the cell's speciesID and executes the instruction corresponding to the cell's current
   * instructionIndex.
   *
   * @param cell the cell to process
   * @return the result of processing the cell's current instruction
   */
  public InstructionResult processCell(Cell<DarwinState> cell) {
    int speciesID = (int) cell.getProperty("speciesID");
    int instructionIndex = (int) cell.getProperty("instructionIndex");
    List<String> instructions = getProgram(speciesID);
    if (instructions == null || instructions.isEmpty()) {
      return new InstructionResult(0, null);
    }
    if (instructionIndex - 1 >= instructions.size()) {
      instructionIndex = 1;
      cell.setProperty("instructionIndex", instructionIndex);
    }
    String instruction = instructions.get(instructionIndex - 1);
    return processInstruction(instruction, cell);
  }

  /**
   * Executes the MOVE instruction.
   *
   * @param cell     the cell on which to execute the instruction
   * @param argument the move distance argument
   * @return an InstructionResult containing the move distance and null for the infected cell
   */
  private InstructionResult executeMove(Cell<DarwinState> cell, int argument) {
    return new InstructionResult(argument, null);
  }

  /**
   * Executes the LEFT instruction by rotating the cell's orientation to the left.
   *
   * @param cell     the cell on which to execute the instruction
   * @param argument the angle to rotate
   * @return an InstructionResult indicating successful execution with no move distance
   */
  private InstructionResult executeLeft(Cell<DarwinState> cell, int argument) {
    cell.setProperty("orientation", (cell.getProperty("orientation") + argument) % 360);
    return new InstructionResult(0, null);
  }

  /**
   * Executes the RIGHT instruction by rotating the cell's orientation to the right.
   *
   * @param cell     the cell on which to execute the instruction
   * @param argument the angle to rotate
   * @return an InstructionResult indicating successful execution with no move distance
   */
  private InstructionResult executeRight(Cell<DarwinState> cell, int argument) {
    cell.setProperty("orientation", (cell.getProperty("orientation") - argument + 360) % 360);
    return new InstructionResult(0, null);
  }

  /**
   * Executes the INFECT instruction, attempting to infect a neighboring cell.
   *
   * @param cell     the cell on which to execute the instruction
   * @param argument the infection duration argument
   * @return an InstructionResult containing zero move distance and the infected cell (if any)
   */
  private InstructionResult executeInfect(Cell<DarwinState> cell, int argument) {
    setNeighbors(cell);
    for (Cell<DarwinState> neighbor : cell.getNeighbors().values()) {
      if (neighbor.getProperty("speciesID") != 0 &&
          cell.getProperty("speciesID") != neighbor.getProperty("speciesID")) {
        neighbor.addQueueRecord(new InfectionRecord(cell.getProperty("speciesID"), argument));
        return new InstructionResult(0, neighbor);
      }
    }
    return new InstructionResult(0, null);
  }

  /**
   * Executes the IFEMPTY instruction. If any neighbor is non-empty, advances to the next
   * instruction; otherwise, jumps to the specified instruction.
   *
   * @param cell     the cell on which to execute the instruction
   * @param argument the instruction index to jump to if the condition is met
   * @return the result of executing the subsequent instruction
   */
  private InstructionResult executeIfempty(Cell<DarwinState> cell, int argument) {
    setNeighbors(cell);
    for (Cell<DarwinState> neighbor : cell.getNeighbors().values()) {
      if (neighbor.getProperty("speciesID") != 0) {
        return executeGo(cell, (int) (cell.getProperty("instructionIndex") + 1));
      }
    }
    return executeGo(cell, argument);
  }

  /**
   * Executes the IFWALL instruction. If the number of neighbors is not equal to the expected value,
   * jumps to the specified instruction; otherwise, advances to the next instruction.
   *
   * @param cell     the cell on which to execute the instruction
   * @param argument the instruction index to jump to if the condition is met
   * @return the result of executing the subsequent instruction
   */
  private InstructionResult executeIfwall(Cell<DarwinState> cell, int argument) {
    setNeighbors(cell);
    if (cell.getNeighbors().size() != darwinLogic.getNearbyAhead()) {
      return executeGo(cell, argument);
    }
    return executeGo(cell, (int) (cell.getProperty("instructionIndex") + 1));
  }

  /**
   * Executes the IFSAME instruction. If any neighbor has the same speciesID, jumps to the specified
   * instruction; otherwise, advances to the next instruction.
   *
   * @param cell     the cell on which to execute the instruction
   * @param argument the instruction index to jump to if the condition is met
   * @return the result of executing the subsequent instruction
   */
  private InstructionResult executeIfsame(Cell<DarwinState> cell, int argument) {
    setNeighbors(cell);
    for (Cell<DarwinState> neighbor : cell.getNeighbors().values()) {
      if (cell.getProperty("speciesID") == neighbor.getProperty("speciesID")) {
        return executeGo(cell, argument);
      }
    }
    return executeGo(cell, (int) (cell.getProperty("instructionIndex") + 1));
  }

  /**
   * Executes the IFENEMY instruction. If any neighbor has a different speciesID, jumps to the
   * specified instruction; otherwise, advances to the next instruction.
   *
   * @param cell     the cell on which to execute the instruction
   * @param argument the instruction index to jump to if the condition is met
   * @return the result of executing the subsequent instruction
   */
  private InstructionResult executeIfenemy(Cell<DarwinState> cell, int argument) {
    setNeighbors(cell);
    for (Cell<DarwinState> neighbor : cell.getNeighbors().values()) {
      if (cell.getProperty("speciesID") != neighbor.getProperty("speciesID")) {
        return executeGo(cell, argument);
      }
    }
    return executeGo(cell, (int) (cell.getProperty("instructionIndex") + 1));
  }

  /**
   * Executes the IFRANDOM instruction. With a 50% chance, jumps to the specified instruction;
   * otherwise, advances to the next instruction.
   *
   * @param cell     the cell on which to execute the instruction
   * @param argument the instruction index to jump to if the condition is met
   * @return the result of executing the subsequent instruction
   */
  private InstructionResult executeIfrandom(Cell<DarwinState> cell, int argument) {
    if (Math.random() < 0.5) {
      return executeGo(cell, argument);
    }
    return executeGo(cell, (int) (cell.getProperty("instructionIndex") + 1));
  }

  /**
   * Executes the GO instruction, setting the cell's instructionIndex to the specified argument and
   * processing the cell's next instruction.
   *
   * @param cell     the cell on which to execute the instruction
   * @param argument the instruction index to set
   * @return the result of processing the cell after updating its instruction index
   */
  private InstructionResult executeGo(Cell<DarwinState> cell, int argument) {
    cell.setProperty("instructionIndex", argument);
    return processCell(cell);
  }

  /**
   * Sets the neighbors for the cell based on its current orientation. Uses the cell's orientation
   * to perform a raycast and assign neighbors.
   *
   * @param cell the cell for which to set neighbors
   */
  private void setNeighbors(Cell<DarwinState> cell) {
    Direction facing = getOrientation(cell);
    grid.assignRaycastNeighbor(cell, facing, (int) darwinLogic.getNearbyAhead());
  }

  /**
   * Determines the best matching orientation for the cell based on its current orientation property
   * and available raycast directions.
   *
   * @param cell the cell whose orientation is to be determined
   * @return the best matching {@link Direction} for the cell's orientation
   */
  public Direction getOrientation(Cell<DarwinState> cell) {
    double orientation = (cell.getProperty("orientation") + 360) % 360;
    List<Direction> directions = grid.getAllRaycastDirections(cell);
    Direction bestDirection = null;
    double smallestDifference = Double.MAX_VALUE;
    for (Direction dir : directions) {
      double dirAngle = getAngleFromDirection(dir);
      double difference = Math.abs(orientation - dirAngle);
      if (difference < smallestDifference) {
        smallestDifference = difference;
        bestDirection = dir;
      }
    }
    return bestDirection;
  }

  /**
   * Calculates the angle (in degrees) from the given direction.
   *
   * @param dir the direction to convert
   * @return the angle corresponding to the direction, in degrees
   */
  private double getAngleFromDirection(Direction dir) {
    double angle = Math.toDegrees(Math.atan2(-dir.dx(), -dir.dy()));
    return (angle + 360) % 360;
  }

  /**
   * Immutable record representing the result of executing a Darwin instruction.
   *
   * @param moveDistance the distance to move as a result of the instruction
   * @param infectedCell the cell that was infected as a result of the instruction, if any
   */
  public record InstructionResult(int moveDistance, Cell<DarwinState> infectedCell) {

  }
}