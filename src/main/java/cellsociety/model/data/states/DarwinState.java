package cellsociety.model.data.states;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * All actual species IDs and names are held in static structures, so you can
 * dynamically define them at startup.
 */
public enum DarwinState implements State {

  /**
   * The single actual enumerator.
   */
  DYNAMIC;

  private static final Map<Integer, SpeciesRecord> speciesMap = new HashMap<>();
  private static boolean locked = false;

  /**
   * Record to store species info: id + name + any extra fields you like.
   */
  private static class SpeciesRecord {
    final int id;
    final String name;
    SpeciesRecord(int id, String name) {
      this.id = id;
      this.name = name;
    }
  }

  /**
   * Creates or returns an existing species for the given id, name.
   * If called after lock(), throws an exception if new species is encountered.
   */
  public static void registerSpecies(int id, String name) {
    SpeciesRecord existing = speciesMap.get(id);
    if (existing != null) {
      return;
    }
    if (!locked) {
      speciesMap.put(id, new SpeciesRecord(id, name));
    }
  }

  /**
   * Returns the unique species id's record or null if not found.
   */
  private static SpeciesRecord getRecord(int id) {
    return speciesMap.get(id);
  }

  /**
   * Retrieves (or ensures) the species with the given ID is known.
   * If it doesn't exist, and locked==true, an exception is thrown.
   * If it doesn't exist, and locked==false, it's created with blank name.
   */
  public static synchronized DarwinState fromInt(int id) {
    SpeciesRecord rec = speciesMap.get(id);
    if (rec == null) {
      if (locked) {
        throw new IllegalStateException(
            "Unknown species " + id + " after locked! Must register before lock.");
      }
      registerSpecies(id, "Species" + id);
    }
    // Return the single enumerator, ignoring name. The dynamic data is in speciesMap
    return DYNAMIC;
  }

  @Override
  public int getValue() {
    return 0;
  }

  /**
   * Lookup the 'name' of a species by ID if needed.
   */
  public static String getNameOf(int id) {
    SpeciesRecord rec = getRecord(id);
    return (rec != null) ? rec.name : "";
  }

  /**
   * Returns an unmodifiable snapshot of all species IDs => name.
   */
  public static Map<Integer, String> getAllSpecies() {
    Map<Integer, String> snapshot = new HashMap<>();
    for (SpeciesRecord rec : speciesMap.values()) {
      snapshot.put(rec.id, rec.name);
    }
    return Collections.unmodifiableMap(snapshot);
  }
}
