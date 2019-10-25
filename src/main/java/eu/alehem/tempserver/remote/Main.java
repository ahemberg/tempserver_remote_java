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

  private static int tempReaderFrequency;
  private static int persistenceHandlerFrequency;
  private static int senderFrequency;
  private static int monitorFrequency;
  private static int corePoolSize;
  private static String piSerial;

  private static void setUp() throws IOException, InterruptedException {
    ApplicationProperties properties = ApplicationProperties.getInstance();

    tempReaderFrequency = Integer.valueOf(properties.getProperty("tempreader.frequency.seconds"));
    persistenceHandlerFrequency =
        Integer.valueOf(properties.getProperty("persistencehandler.frequency.seconds"));
    senderFrequency = Integer.valueOf(properties.getProperty("sender.frequency.seconds"));
    corePoolSize = Integer.valueOf(properties.getProperty("main.core_pool_size"));
    monitorFrequency = Integer.valueOf(properties.getProperty("main.monitor_frequency"));
    piSerial = SystemInfo.getSerial();
  }

  public static void main(String[] args) throws SQLException, InterruptedException, IOException {
    setUp();
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
    exec.scheduleAtFixedRate(
        new Thread(new StatusMonitor(), "Monitor"), 0, monitorFrequency, TimeUnit.SECONDS);
  }
}
