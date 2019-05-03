package eu.alehem.tempserver.remote;


import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Main {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main(String[] args) throws SQLException, InterruptedException {
        TempQueue queue = TempQueue.getInstance();

        TempReader tempReader = new TempReader();
        PersistenceHandler persistenceHandler = new PersistenceHandler();
        TempSender sender = new TempSender();
        Thread tempReaderThread = new Thread(tempReader, "TempReader");
        Thread persistenceHandlerThread = new Thread(persistenceHandler, "PersistenceHandler");
        Thread senderThread = new Thread(sender, "Sender");

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(tempReaderThread, 0, 2, TimeUnit.MINUTES);
        exec.scheduleAtFixedRate(persistenceHandlerThread, 3, 5, TimeUnit.SECONDS);
        exec.scheduleAtFixedRate(senderThread, 11, 10, TimeUnit.SECONDS);

        while (true) {
            LOGGER.info("Queue size: " + queue.getQueueLen());
            LOGGER.info("Entries in db: " + DatabaseManager.getTemperatures(99999));
            Thread.sleep(10000);
        }
    }
}
