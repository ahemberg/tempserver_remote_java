package eu.alehem.tempserver.remote;

import lombok.extern.java.Log;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log
public class Main {

  public static void main(String[] args) throws SQLException, InterruptedException {
    TempQueue queue = TempQueue.getInstance();

    ScheduledExecutorService exec = Executors.newScheduledThreadPool(3);
    exec.scheduleAtFixedRate(new Thread(new TempReader(), "TempReader"), 0, 2, TimeUnit.MINUTES);
    exec.scheduleAtFixedRate(
        new Thread(new PersistenceHandler(), "PersistenceHandler"), 3, 30, TimeUnit.SECONDS);
    exec.scheduleAtFixedRate(new Thread(new TempSender(), "Sender"), 11, 30, TimeUnit.SECONDS);

    while (true) {
      log.info("Queue size: " + queue.getQueueLen());
      log.info("Entries in db: " + DatabaseManager.countMeasurementsInDb());
      Thread.sleep(10000);
    }
  }
}
