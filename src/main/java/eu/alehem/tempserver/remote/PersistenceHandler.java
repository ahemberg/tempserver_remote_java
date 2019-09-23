package eu.alehem.tempserver.remote;

import lombok.extern.java.Log;

import java.sql.SQLException;
import java.util.Set;

@Log
public class PersistenceHandler implements Runnable {

  private final int MAX_QUEUE_LEN = 60;
  private final int DELETE_THRESHOLD = 10000;
  private final int ADD_THRESHOLD = 10;
  private final int BATCH_SIZE = 10;
  private int entriesInDb;

  private TempQueue queue = TempQueue.getInstance();

  PersistenceHandler() throws SQLException {
    DatabaseManager.createDataBaseIfNotExists();
    entriesInDb = DatabaseManager.countMeasurementsInDb();
  }

  @Override
  public void run() {
    int queLen = queue.getQueueLen();

    if (queLen < ADD_THRESHOLD && entriesInDb != 0) {
      log.info("PERSISTENCEHANDLER: Populating queue from db");
      try {
        Set<Temperature> temperatures = DatabaseManager.getTemperatures(BATCH_SIZE);
        queue.addTemperatures(temperatures);
        DatabaseManager.deleteTemperatures(temperatures);
        entriesInDb = DatabaseManager.countMeasurementsInDb();
        log.info(
            "PERSISTENCEHANDLER: Successfully populated from db. DB now has "
                + entriesInDb
                + " entries");
        return;
      } catch (SQLException e) {
        log.warning("PERSISTENCEHANDLER: WARNING: failed to get temperatures from db");
        return;
      }
    }

    if (queLen > MAX_QUEUE_LEN) {
      log.info("PERSISTENCEHANDLER: Queue longer than max, saving to DB");
      Set<Temperature> temperatures = queue.getN(BATCH_SIZE);
      if (saveToDb(temperatures)) {
        temperatures.forEach(t -> queue.removeTemperature(t));
        return;
      }
    }

    if (queLen > DELETE_THRESHOLD) {
      log.warning("PERSISTENCEHANDLER: WARNING: QUEUE is too long, will start to delete entries");
      Set<Temperature> temperatures = queue.getN(BATCH_SIZE);
      temperatures.forEach(t -> queue.removeTemperature(t));
    }

    // TODO: This implies a database read on every run. No writes though so maybe OK
    try {
      entriesInDb = DatabaseManager.countMeasurementsInDb(); // TODO: Convoluted and hard to read.
    } catch (SQLException e) {
      log.warning("Failed to count elements in database");
    }
  }

  private boolean saveToDb(Set<Temperature> temperatures) {
    try {
      DatabaseManager.insertTemperatures(temperatures);
    } catch (Throwable e) {
      log.warning("PERSISTENCEHANDLER: Warning: Failed to save temperatures");
      return false;
    }
    return true;
  }
}
