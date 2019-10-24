package eu.alehem.tempserver.remote;

import com.pi4j.system.SystemInfo;
import eu.alehem.tempserver.remote.properties.ApplicationProperties;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.java.Log;

@Log
public class Main {

  public static void main(String[] args) throws SQLException, InterruptedException, IOException {
    ApplicationProperties properties = ApplicationProperties.getInstance();

    final int tempReaderFrequency =
        Integer.valueOf(properties.getProperty("tempreader.frequency.seconds"));
    final int persistenceHandlerFrequency =
        Integer.valueOf(properties.getProperty("persistencehandler.frequency.seconds"));
    final int senderFrequency = Integer.valueOf(properties.getProperty("sender.frequency.seconds"));
    final int corePoolSize = Integer.valueOf(properties.getProperty("main.core_pool_size"));

    final String piSerial = SystemInfo.getSerial();

    TempQueue queue = TempQueue.getInstance();

    ScheduledExecutorService exec = Executors.newScheduledThreadPool(corePoolSize);
    exec.scheduleAtFixedRate(
        new Thread(new TempReader(), "TempReader"), 0, tempReaderFrequency, TimeUnit.SECONDS);
    exec.scheduleAtFixedRate(
        new Thread(new PersistenceHandler(), "PersistenceHandler"),
        3,
        persistenceHandlerFrequency,
        TimeUnit.SECONDS);
    exec.scheduleAtFixedRate(
        new Thread(new TempSender(piSerial), "Sender"), 11, senderFrequency, TimeUnit.SECONDS);

    while (true) {
      log.info("Queue size: " + queue.getQueueLen());
      log.info("Entries in db: " + DatabaseManager.countMeasurementsInDb());
      Thread.sleep(10000);
    }
  }
}
