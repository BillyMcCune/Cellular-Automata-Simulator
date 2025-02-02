package modeltests;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.data.states.PercolationState;
import cellsociety.model.data.states.State;
import org.junit.jupiter.api.Test;

public class PercolationStateTest {

  @Test
  public void PercolationState_fromInt_ValidInputs_ReturnsCorrectEnum() {
    assertEquals(PercolationState.BLOCKED, State.fromInt(PercolationState.class, 0));
    assertEquals(PercolationState.OPEN, State.fromInt(PercolationState.class, 1));
    assertEquals(PercolationState.PERCOLATED, State.fromInt(PercolationState.class, 2));
  }

  @Test
  public void PercolationState_fromInt_InvalidInput_ReturnsDefault() {
    assertEquals(PercolationState.BLOCKED, State.fromInt(PercolationState.class, -1));
    assertEquals(PercolationState.BLOCKED, State.fromInt(PercolationState.class, 3));
  }
}
