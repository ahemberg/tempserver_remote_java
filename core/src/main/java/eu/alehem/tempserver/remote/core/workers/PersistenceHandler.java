package eu.alehem.tempserver.remote.core.workers;

import eu.alehem.tempserver.remote.core.DatabaseManager;
import eu.alehem.tempserver.remote.core.TempQueue;
import eu.alehem.tempserver.remote.core.Temperature;
import eu.alehem.tempserver.remote.core.properties.PersistenceProperties;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Set;

@Slf4j
public class PersistenceHandler implements Runnable {

  private final PersistenceProperties properties;
  private int entriesInDb;
  private TempQueue queue = TempQueue.getInstance();

  public PersistenceHandler(PersistenceProperties properties) throws SQLException {
    this.properties = properties;
    DatabaseManager.createDataBaseIfNotExists();
    entriesInDb = DatabaseManager.countMeasurementsInDb();
  }

  private void populateQueueFromDb() throws SQLException {
    if (properties.isVerbose()) log.info("Populating queue from db");
    Set<Temperature> temperatures = DatabaseManager.getTemperatures(properties.getBatchSize());
    queue.addTemperatures(temperatures);
    DatabaseManager.deleteTemperatures(temperatures);
    entriesInDb = DatabaseManager.countMeasurementsInDb();
    if (properties.isVerbose())
      log.info("Successfully populated from db. DB now has " + entriesInDb + " entries");
  }

  private void populateDbFromQueue() throws SQLException {
    if (properties.isVerbose()) log.info("Moving items from queue to DB");
    Set<Temperature> temperatures = queue.getN(properties.getBatchSize());
    DatabaseManager.insertTemperatures(temperatures);
    queue.removeTemperatures(temperatures);
  }

  @Override
  public void run() {
    final int queLen = queue.getQueueLen();

    try {
      entriesInDb = DatabaseManager.countMeasurementsInDb();
    } catch (SQLException e) {
      log.warn("Failed to count elements in database", e);
      return;
    }

    if (queLen < properties.getAddThreshold() && entriesInDb != 0) {
      try {
        populateQueueFromDb();
        return;
      } catch (SQLException e) {
        log.warn("Failed to get temperatures from db", e);
      }
    }

    if (queLen > properties.getMaxQueueLength()) {
      try {
        populateDbFromQueue();
        return;
      } catch (SQLException e) {
        log.warn("Failed to save temperatures to db", e);
      }
    }

    if (queLen > properties.getMaxQueueLength()) {
      log.warn("WARNING: QUEUE is too long, will start to delete entries");
      Set<Temperature> temperatures = queue.getN(properties.getBatchSize());
      temperatures.forEach(t -> queue.removeTemperature(t));
    }
  }
}
