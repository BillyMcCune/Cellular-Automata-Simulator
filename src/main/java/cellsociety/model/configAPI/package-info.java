package cellsociety.model.configAPI;
/**
 * Provides an API for simulation configuration operations.
 *
 * <p>This package contains classes that facilitate the reading, writing, and management of
 * simulation configuration files. The main entry point is the {@link cellsociety.model.configAPI.configAPI}
 * class, which acts as a facade to the underlying configuration operations.
 *
 * <p>The API supports:
 * <ul>
 *   <li>Retrieving available configuration file names via the {@code ConfigReader}.</li>
 *   <li>Loading simulation configuration data, including grid states and simulation metadata,
 *       using XML parsing mechanisms.</li>
 *   <li>Saving the current simulation state by gathering grid data and parameters from the
 *       simulation model and writing them out using the {@code ConfigWriter}.</li>
 *   <li>Accessing and modifying simulation details such as grid dimensions, tick speed, accepted states,
 *       and simulation information (author, title, description).</li>
 * </ul>
 *
 * <p>The package interacts with several supporting classes:
 * <ul>
 *   <li>{@link cellsociety.model.config.ConfigReader} – for reading configuration files.</li>
 *   <li>{@link cellsociety.model.config.ConfigWriter} – for saving simulation state to a file.</li>
 *   <li>{@link cellsociety.model.config.ConfigInfo} – which encapsulates configuration details.</li>
 *   <li>{@link cellsociety.model.config.CellRecord} – which represents cell state and properties.</li>
 *   <li>{@link cellsociety.model.config.ParameterRecord} – for storing simulation parameters.</li>
 * </ul>
 *
 * <p>Using this API, clients can abstract away the file and XML parsing details while working with
 * the simulation configuration. It integrates with the simulation model via the {@link cellsociety.model.modelAPI.ModelApi},
 * enabling both the loading of pre-defined simulations and the saving of current simulation states.
 *
 * @author Billy McCune
 */
