package eu.alehem.tempserver.remote.core.workers;

import eu.alehem.tempserver.remote.core.DatabaseManager;
import eu.alehem.tempserver.remote.core.TempQueue;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatusMonitor implements Runnable {

  @Override
  public void run() {
    TempQueue queue = TempQueue.getInstance();
    log.info("Queue size: " + queue.getQueueLen());
    try {
    log.info("Entries in db: " + DatabaseManager.countMeasurementsInDb());
    } catch (SQLException e) {
      log.warn("Failed to count entries in db", e);
    }
  }

}
