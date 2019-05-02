package eu.alehem.tempserver.remote;

import java.sql.SQLException;
import java.util.Set;

public class PersistanceHandler implements Runnable {

    private final int MAX_QUEUE_LEN = 100;
    private final int DELETE_THRESHOLD = 1000;
    private final int ADD_THRESHOLD = 30;
    private final int BATCH_SIZE = 10;
    private int entriesInDb;

    private TempQueue queue = TempQueue.getInstance();

    public PersistanceHandler() throws SQLException {
        DatabaseManager.createDataBaseIfNotExists();
        entriesInDb = DatabaseManager.countMeasurementsInDb();
    }

    @Override
    public void run() {
        int queLen = queue.getQueueLen();

        if (queLen < ADD_THRESHOLD && entriesInDb != 0) {
            System.out.println("PERSISTANCEHANDLER: Populating queue from db");
            try {
                Set<Temperature> temperatures = DatabaseManager.getTemperatures(BATCH_SIZE);
                queue.addTemperatures(temperatures);
                DatabaseManager.deleteTemperatures(temperatures);
                entriesInDb = DatabaseManager.countMeasurementsInDb();
                System.out.println("PERSISTANCEHANDLER: Successfully populated from db. DB now has " + entriesInDb + " entries");
                return;
            } catch (SQLException e) {
                System.out.println("PERSISTANCEHANDLER: WARNING: failed to get temperatures from db");
                return;
            }
        }

        if (queLen > MAX_QUEUE_LEN) {
            System.out.println("PERSISTANCEHANDLER: Queue longer than max, saving to DB");
            Set<Temperature> temperatures = queue.getN(BATCH_SIZE);
            if (saveToDb(temperatures)) {
                temperatures.forEach(t -> queue.removeTemperature(t));
                return;
            }
        }

        if (queLen > DELETE_THRESHOLD) {
            System.out.println("PERSISTANCEHANDLER: WARNING: QUEUE is too long, will start to delete entries");
            Set<Temperature> temperatures = queue.getN(BATCH_SIZE);
            temperatures.forEach(t -> queue.removeTemperature(t));
        }
    }

    private boolean saveToDb(Set<Temperature> temperatures) {
        try {
            DatabaseManager.insertTemperatures(temperatures);
        } catch (Throwable e) {
            System.out.println("PERSISTANCEHANDLER: Warning: Failed to save temperatures");
            return false;
        }
        return true;
    }
}
