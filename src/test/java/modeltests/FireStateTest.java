package modeltests;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.data.states.FireState;
import cellsociety.model.data.states.State;
import org.junit.jupiter.api.Test;

public class FireStateTest {

  @Test
  public void FireState_fromInt_ValidInputs_ReturnsCorrectEnum() {
    assertEquals(FireState.EMPTY, State.fromInt(FireState.class, 0));
    assertEquals(FireState.TREE, State.fromInt(FireState.class, 1));
    assertEquals(FireState.BURNING, State.fromInt(FireState.class, 2));
  }

  @Test
  public void FireState_fromInt_InvalidInput_ReturnsDefault() {
    assertEquals(FireState.EMPTY, State.fromInt(FireState.class, -1));
    assertEquals(FireState.EMPTY, State.fromInt(FireState.class, 3));
  }
}
