package cellsociety.model.logic;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.data.cells.Cell;
import cellsociety.model.data.Grid;
import cellsociety.model.data.states.LifeState;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Concrete implementation of {@link Logic} for Conway's Game of Life.
 *
 * @author Jacob You
 */
public class LifeLogic extends Logic<LifeState> {

  private List<Integer> birthRequirement;
  private List<Integer> survivalRequirement;
  private String rulestring;

  /**
   * Constructs a {@code LifeLogic} instance with the specified grid.
   *
   * @param grid       the grid representing the current state of grid
   * @param parameters the parameters for the simulation
   * @throws IllegalArgumentException if the rulestring is invalid
   */
  public LifeLogic(Grid<LifeState> grid, ParameterRecord parameters)
      throws IllegalArgumentException {
    super(grid, parameters);
    birthRequirement = List.of(3);
    survivalRequirement = List.of(2, 3);
    setRulestring(getStringParamOrFallback("rulestring"));
  }

  /**
   * Sets the rulestring for the simulation.
   *
   * @param rulestring the rulestring defining birth and survival conditions
   * @throws IllegalArgumentException if the rulestring format is invalid
   */
  public void setRulestring(String rulestring) throws IllegalArgumentException {
    parseRulestring(rulestring);
    this.rulestring = rulestring;
  }

  /**
   * Gets the current rulestring used in the simulation.
   *
   * @return the current rulestring
   */
  public String getRulestring() {
    return rulestring;
  }

  private void parseRulestring(String rulestring) throws IllegalArgumentException {
    if (rulestring.contains("B") || rulestring.contains("S")) {
      parseBSNotation(rulestring);
    } else {
      parseSBNotation(rulestring);
    }
  }

  private void parseBSNotation(String rulestring) throws IllegalArgumentException {
    Pattern p = Pattern.compile("^B(\\d*)/S(\\d*)$");
    Matcher m = p.matcher(rulestring.trim());
    if (!m.matches()) {
      throw new IllegalArgumentException();
    }
    birthRequirement = parseDigitsToList(m.group(1));
    survivalRequirement = parseDigitsToList(m.group(2));
  }

  private void parseSBNotation(String rulestring) throws IllegalArgumentException {
    Pattern p = Pattern.compile("^([0-9]*)/([0-9]*)$");
    Matcher m = p.matcher(rulestring.trim());
    if (!m.matches()) {
      throw new IllegalArgumentException();
    }
    survivalRequirement = parseDigitsToList(m.group(1));
    birthRequirement = parseDigitsToList(m.group(2));
  }

  private List<Integer> parseDigitsToList(String digits) throws IllegalArgumentException {
    List<Integer> result = new ArrayList<>();
    if (digits == null || digits.isEmpty()) {
      return result;
    }
    for (char c : digits.toCharArray()) {
      if (!Character.isDigit(c)) {
        throw new IllegalArgumentException();
      }
      result.add(Character.getNumericValue(c));
    }
    return result;
  }

  @Override
  protected void updateSingleCell(Cell<LifeState> cell) {
    LifeState currentState = cell.getCurrentState();
    int liveNeighbors = countLiveNeighbors(cell);

    if (currentState == LifeState.ALIVE) {
      if (!survivalRequirement.contains(liveNeighbors)) {
        cell.setNextState(LifeState.DEAD);
      }
    } else {
      if (birthRequirement.contains(liveNeighbors)) {
        cell.setNextState(LifeState.ALIVE);
      }
    }
  }

  private int countLiveNeighbors(Cell<LifeState> cell) {
    int liveCount = 0;
    for (Cell<LifeState> neighbor : cell.getNeighbors().values()) {
      if (neighbor.getCurrentState() == LifeState.ALIVE) {
        liveCount++;
      }
    }
    return liveCount;
  }
}
