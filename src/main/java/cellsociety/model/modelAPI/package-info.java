package cellsociety.model.modelAPI;
/**
 * Provides an API for managing simulation models and their lifecycle.
 *
 * <p>This package encapsulates the functionality required to initialize, update, and reset
 * simulation models. The core class, {@link cellsociety.model.modelAPI.ModelApi}, acts as a bridge
 * between the simulation configuration (via {@link cellsociety.model.config.ConfigInfo} and
 * {@link cellsociety.model.config.ParameterRecord}) and the simulation data and logic layers.
 *
 * <p>The API supports:
 * <ul>
 *   <li>Updating the simulation state by invoking the game logic update mechanism and synchronizing
 *       cell properties through the cell color manager.</li>
 *   <li>Resetting the simulation grid and reinitializing game logic using dynamic class loading based
 *       on the current simulation configuration.</li>
 *   <li>Retrieving and managing simulation parameters with built-in bounds and default values.</li>
 *   <li>Accessing grid data including cell states and properties, which are critical for rendering and
 *       further simulation computations.</li>
 *   <li>Customizing simulation styles, such as cell shapes, neighbor arrangements, and edge policies,
 *       via a dedicated style manager.</li>
 * </ul>
 *
 * <p>Other important components interacting within this package include:
 * <ul>
 *   <li>{@link cellsociety.model.config.ConfigInfo} – encapsulates simulation configuration details.</li>
 *   <li>{@link cellsociety.model.config.ParameterRecord} – stores the simulation parameters.</li>
 *   <li>{@link cellsociety.model.data.Grid} – represents the simulation grid structure.</li>
 *   <li>{@link cellsociety.model.data.cells.Cell} – represents individual cells in the grid.</li>
 *   <li>{@link cellsociety.model.logic.Logic} – defines the simulation logic for updating cell states.</li>
 * </ul>
 *
 * <p>This modular design allows the simulation framework to support multiple simulation types,
 * enabling dynamic reconfiguration and extension through reflection and configuration files.
 *
 * @author Billy
 */