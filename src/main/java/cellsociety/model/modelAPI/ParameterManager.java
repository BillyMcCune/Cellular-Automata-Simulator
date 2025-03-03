package cellsociety.model.modelAPI;

import cellsociety.model.config.ParameterRecord;
import cellsociety.model.logic.Logic;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;

/**
 * The ParameterManager class is responsible for managing simulation parameters.
 * <p>
 * It provides functionality to reset the parameters by iterating over the public setter methods of
 * the game logic and updating the parameter record with the default values obtained from the corresponding
 * getter methods.
 * </p>
 */
public class ParameterManager {

  private final Logic<?> gameLogic;
  private final ParameterRecord parameterRecord;

  /**
   * Constructs a new ParameterManager with the specified game logic and parameter record.
   *
   * @param gameLogic       the game logic instance used by the simulation
   * @param parameterRecord the parameter record containing simulation parameters
   */
  public ParameterManager(Logic<?> gameLogic, ParameterRecord parameterRecord) {
    this.gameLogic = gameLogic;
    this.parameterRecord = parameterRecord;
  }

  /**
   * Resets the simulation parameters by iterating over the public setter methods of the game logic.
   * <p>
   * For each setter method (starting with "set" and accepting one parameter), the corresponding getter
   * method is invoked to obtain the default value, and the parameter record is updated accordingly.
   * </p>
   *
   * @throws NoSuchMethodException     if a required getter method is not found
   * @throws InvocationTargetException if a getter method cannot be invoked
   * @throws IllegalAccessException    if a getter method cannot be accessed
   * @throws IllegalStateException     if the game logic is not initialized
   */
  public void resetParameters() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    if (gameLogic == null) {
      throw new IllegalStateException("Game logic is not initialized.");
    }
    Class<?> logicClass = gameLogic.getClass();
    for (Method setterMethod : logicClass.getMethods()) {
      String methodName = setterMethod.getName();
      // Only consider public setter methods that start with "set" and take one parameter.
      if (!methodName.startsWith("set") || setterMethod.getParameterCount() != 1) {
        continue;
      }
      // Derive the parameter name from the setter method (e.g., "setSpeed" -> "speed").
      String paramName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
      // Retrieve the corresponding getter method (e.g., "getSpeed").
      Method getterMethod = logicClass.getMethod("get" + methodName.substring(3));
      Class<?> paramType = setterMethod.getParameterTypes()[0];

      if (paramType == double.class) {
        double defaultValue = (double) getterMethod.invoke(gameLogic);
        parameterRecord.myDoubleParameters().put(paramName, defaultValue);
      } else if (paramType == String.class) {
        String defaultValue = (String) getterMethod.invoke(gameLogic);
        parameterRecord.myStringParameters().put(paramName, defaultValue);
      }
    }
  }
}
