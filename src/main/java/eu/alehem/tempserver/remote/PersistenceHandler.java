package eu.alehem.tempserver.remote;

import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Logger;

public class PersistenceHandler implements Runnable {

  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  private final int MAX_QUEUE_LEN = 60;
  private final int DELETE_THRESHOLD = 10000;
  private final int ADD_THRESHOLD = 10;
  private final int BATCH_SIZE = 10;
  private int entriesInDb;

  private TempQueue queue = TempQueue.getInstance();

  public PersistenceHandler() throws SQLException {
    DatabaseManager.createDataBaseIfNotExists();
    entriesInDb = DatabaseManager.countMeasurementsInDb();
  }

  @Override
  public void run() {
    int queLen = queue.getQueueLen();

    if (queLen < ADD_THRESHOLD && entriesInDb != 0) {
      LOGGER.info("PERSISTENCEHANDLER: Populating queue from db");
      try {
        Set<Temperature> temperatures = DatabaseManager.getTemperatures(BATCH_SIZE);
        queue.addTemperatures(temperatures);
        DatabaseManager.deleteTemperatures(temperatures);
        entriesInDb = DatabaseManager.countMeasurementsInDb();
        LOGGER.info(
            "PERSISTENCEHANDLER: Successfully populated from db. DB now has "
                + entriesInDb
                + " entries");
        return;
      } catch (SQLException e) {
        LOGGER.warning("PERSISTENCEHANDLER: WARNING: failed to get temperatures from db");
        return;
      }
    }

    if (queLen > MAX_QUEUE_LEN) {
      LOGGER.info("PERSISTENCEHANDLER: Queue longer than max, saving to DB");
      Set<Temperature> temperatures = queue.getN(BATCH_SIZE);
      if (saveToDb(temperatures)) {
        temperatures.forEach(t -> queue.removeTemperature(t));
        return;
      }
    }

    if (queLen > DELETE_THRESHOLD) {
      LOGGER.warning(
          "PERSISTENCEHANDLER: WARNING: QUEUE is too long, will start to delete entries");
      Set<Temperature> temperatures = queue.getN(BATCH_SIZE);
      temperatures.forEach(t -> queue.removeTemperature(t));
    }

    // TODO: This implies a database read on every run. No writes though so maybe OK
    try {
      entriesInDb = DatabaseManager.countMeasurementsInDb(); // TODO: Convoluted and hard to read.
    } catch (SQLException e) {
      LOGGER.warning("Failed to count elements in database");
    }
  }

  private boolean saveToDb(Set<Temperature> temperatures) {
    try {
      DatabaseManager.insertTemperatures(temperatures);
    } catch (Throwable e) {
      LOGGER.warning("PERSISTENCEHANDLER: Warning: Failed to save temperatures");
      return false;
    }
    return true;
  }
}
