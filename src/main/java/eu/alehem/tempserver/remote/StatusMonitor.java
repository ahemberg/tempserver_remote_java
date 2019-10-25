package eu.alehem.tempserver.remote;

import java.sql.SQLException;
import lombok.extern.java.Log;

@Log
public class StatusMonitor implements Runnable {

  @Override
  public void run() {
    TempQueue queue = TempQueue.getInstance();
    log.info("Queue size: " + queue.getQueueLen());
    try {
    log.info("Entries in db: " + DatabaseManager.countMeasurementsInDb());
    } catch (SQLException s) {
      log.warning("Failed to count entries in db");
    }
  }

}
