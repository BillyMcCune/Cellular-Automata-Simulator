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
 */
public class DarwinHelper {

  private static final String SPECIES_DIR = "src/main/resources/speciesdata/";
  private Map<Integer, List<String>> speciesPrograms = new HashMap<>();
  private final Map<String, Method> instructionMethods = new HashMap<>();
  private final DarwinLogic darwinLogic;
  private final Grid<DarwinState> grid;

  /**
   * Constructs a {@code DarwinHelper} instance with the specified {@link DarwinLogic} and
   * {@link Grid}. This constructor loads species programs from the species data directory and
   * registers instruction methods.
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
   * Gets the program instructions for a given species ID.
   *
   * @param speciesID the identifier of the species
   * @return a list of instruction strings for the species, or an empty list if none are found
   */
  public List<String> getProgram(int speciesID) {
    return speciesPrograms.getOrDefault(speciesID, Collections.emptyList());
  }

  /**
   * Processes the current instruction for the specified cell. This method retrieves the species
   * program based on the cell's speciesID and executes the instruction corresponding to the cell's
   * current instructionIndex.
   *
   * @param cell the cell to process
   * @return an {@link InstructionResult} indicating the result of processing the instruction
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
   * Returns the best matching orientation for the specified cell based on its current orientation
   * property and the available raycast directions.
   *
   * @param cell the cell whose orientation is to be determined
   * @return the {@link Direction} that best matches the cell's orientation
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
   * Immutable record representing the result of executing a Darwin instruction. It contains the
   * move distance (if any) and the cell that was infected, if applicable.
   *
   * @param moveDistance the distance the cell should move
   * @param infectedCell the cell that was infected as a result of the instruction, or null if none
   */
  public record InstructionResult(int moveDistance, Cell<DarwinState> infectedCell) {

  }

  // Private methods below are not commented as they are not part of the public API.
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

  private void registerInstructions() {
    for (Method method : this.getClass().getDeclaredMethods()) {
      if (method.getName().startsWith("execute")) {
        String instructionName = method.getName().substring(7).toUpperCase();
        instructionMethods.put(instructionName, method);
        instructionMethods.put(getAlias(instructionName), method);
      }
    }
  }

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
      default -> instruction;
    };
  }

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

  private InstructionResult executeMove(Cell<DarwinState> cell, int argument) {
    return new InstructionResult(argument, null);
  }

  private InstructionResult executeLeft(Cell<DarwinState> cell, int argument) {
    cell.setProperty("orientation", (cell.getProperty("orientation") + argument) % 360);
    return new InstructionResult(0, null);
  }

  private InstructionResult executeRight(Cell<DarwinState> cell, int argument) {
    cell.setProperty("orientation", (cell.getProperty("orientation") - argument + 360) % 360);
    return new InstructionResult(0, null);
  }

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

  private void executeIfempty(Cell<DarwinState> cell, int argument) {
    setNeighbors(cell);
    for (Cell<DarwinState> neighbor : cell.getNeighbors().values()) {
      if (neighbor.getProperty("speciesID") != 0) {
        executeGo(cell, (int) (cell.getProperty("instructionIndex") + 1));
        return;
      }
    }
    executeGo(cell, argument);
  }

  private void executeIfwall(Cell<DarwinState> cell, int argument) {
    setNeighbors(cell);
    if (cell.getNeighbors().size() != darwinLogic.getNearbyAhead()) {
      executeGo(cell, argument);
      return;
    }
    executeGo(cell, (int) (cell.getProperty("instructionIndex") + 1));
  }

  private void executeIfsame(Cell<DarwinState> cell, int argument) {
    setNeighbors(cell);
    for (Cell<DarwinState> neighbor : cell.getNeighbors().values()) {
      if (cell.getProperty("speciesID") == neighbor.getProperty("speciesID")) {
        executeGo(cell, argument);
        return;
      }
    }
    executeGo(cell, (int) (cell.getProperty("instructionIndex") + 1));
  }

  private void executeIfenemy(Cell<DarwinState> cell, int argument) {
    setNeighbors(cell);
    for (Cell<DarwinState> neighbor : cell.getNeighbors().values()) {
      if (cell.getProperty("speciesID") != neighbor.getProperty("speciesID")) {
        executeGo(cell, argument);
        return;
      }
    }
    executeGo(cell, (int) (cell.getProperty("instructionIndex") + 1));
  }

  private void executeIfrandom(Cell<DarwinState> cell, int argument) {
    if (Math.random() < 0.5) {
      executeGo(cell, argument);
      return;
    }
    executeGo(cell, (int) (cell.getProperty("instructionIndex") + 1));
  }

  private void executeGo(Cell<DarwinState> cell, int argument) {
    cell.setProperty("instructionIndex", argument);
    processCell(cell);
  }

  private void setNeighbors(Cell<DarwinState> cell) {
    Direction facing = getOrientation(cell);
    grid.assignRaycastNeighbor(cell, facing, (int) darwinLogic.getNearbyAhead());
  }

  private double getAngleFromDirection(Direction dir) {
    double angle = Math.toDegrees(Math.atan2(-dir.dx(), -dir.dy()));
    return (angle + 360) % 360;
  }
}
