package eu.alehem.tempserver.remote;

import eu.alehem.tempserver.remote.properties.ApplicationProperties;
import java.sql.SQLException;
import java.util.Set;
import lombok.extern.java.Log;

@Log
public class PersistenceHandler implements Runnable {

  private final int MAX_QUEUE_LEN;
  private final int DELETE_THRESHOLD;
  private final int ADD_THRESHOLD;
  private final int BATCH_SIZE;
  private int entriesInDb;

  private TempQueue queue = TempQueue.getInstance();

  PersistenceHandler() throws SQLException {
    DatabaseManager.createDataBaseIfNotExists();
    entriesInDb = DatabaseManager.countMeasurementsInDb();
    ApplicationProperties properties = ApplicationProperties.getInstance();
    MAX_QUEUE_LEN = Integer.valueOf(properties.getProperty("persistencehandler.max_queue_len"));
    DELETE_THRESHOLD =
        Integer.valueOf(properties.getProperty("persistencehandler.delete_threshold"));
    ADD_THRESHOLD = Integer.valueOf(properties.getProperty("persistencehandler.add_threshold"));
    BATCH_SIZE = Integer.valueOf(properties.getProperty("persistencehandler.batch_size"));
  }

  private void populateQueueFromDb() throws SQLException {
    log.info("Populating queue from db");
    Set<Temperature> temperatures = DatabaseManager.getTemperatures(BATCH_SIZE);
    queue.addTemperatures(temperatures);
    DatabaseManager.deleteTemperatures(temperatures);
    entriesInDb = DatabaseManager.countMeasurementsInDb();
    log.info("Successfully populated from db. DB now has " + entriesInDb + " entries");
  }

  private void populateDbFromQueue() throws SQLException {
    log.info("Moving items from queue to DB");
    Set<Temperature> temperatures = queue.getN(BATCH_SIZE);
    DatabaseManager.insertTemperatures(temperatures);
    queue.removeTemperatures(temperatures);
  }

  @Override
  public void run() {
    final int queLen = queue.getQueueLen();

    try {
      entriesInDb = DatabaseManager.countMeasurementsInDb();
    } catch (SQLException e) {
      log.warning("Failed to count elements in database");
      return;
    }

    if (queLen < ADD_THRESHOLD && entriesInDb != 0) {
      try {
        populateQueueFromDb();
        return;
      } catch (SQLException e) {
        log.warning("Failed to get temperatures from db");
        log.warning(e.getMessage());
      }
    }

    if (queLen > MAX_QUEUE_LEN) {
      try {
        populateDbFromQueue();
        return;
      } catch (SQLException e) {
        log.warning("Failed to save temperatures to db");
        log.warning(e.getMessage());
      }
    }

    if (queLen > DELETE_THRESHOLD) {
      log.warning("WARNING: QUEUE is too long, will start to delete entries");
      Set<Temperature> temperatures = queue.getN(BATCH_SIZE);
      temperatures.forEach(t -> queue.removeTemperature(t));
    }
  }
}
