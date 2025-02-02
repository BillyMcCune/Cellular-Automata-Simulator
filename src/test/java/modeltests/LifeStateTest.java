package modeltests;

import static org.junit.jupiter.api.Assertions.*;

import cellsociety.model.data.states.LifeState;
import cellsociety.model.data.states.State;
import org.junit.jupiter.api.Test;

public class LifeStateTest {

  @Test
  public void LifeState_fromInt_ValidInputs_ReturnsCorrectEnum() {
    assertEquals(LifeState.DEAD, State.fromInt(LifeState.class, 0));
    assertEquals(LifeState.ALIVE, State.fromInt(LifeState.class, 1));
  }

  @Test
  public void LifeState_fromInt_InvalidInput_ReturnsDefault() {
    assertEquals(LifeState.DEAD, State.fromInt(LifeState.class, -1));
    assertEquals(LifeState.DEAD, State.fromInt(LifeState.class, 5));
  }
}
