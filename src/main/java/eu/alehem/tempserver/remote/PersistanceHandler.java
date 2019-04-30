package eu.alehem.tempserver.remote;

import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.util.Set;

public class PersistanceHandler implements Runnable {

    private final int MAX_QUEUE_LEN = 5;
    private final int DELETE_THRESHOLD = 10;
    private final int ADD_THRESHOLD = 2;
    private final int BATCH_SIZE = 1;

    private TempQueue queue = TempQueue.getInstance();

    public PersistanceHandler() throws SQLException {
        DatabaseManager.createDataBaseIfNotExists();
    }

    //TODO: CHANGE TO BATCH NOT WORKLING NOW

    @Override
    public void run() {
        int queLen = queue.getQueueLen();

        if (queLen < ADD_THRESHOLD) {
            System.out.println("PERSISTANCEHANDLER: Populating queue from db");
            try {
                Set<Temperature> temperatures = DatabaseManager.getTemperatures(BATCH_SIZE);
                queue.addTemperatures(temperatures);
                DatabaseManager.deleteTemperatures(temperatures);
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
            temperatures.forEach(t -> {
                try {
                    DatabaseManager.insertTemperature(t);
                } catch (SQLException e) {
                    System.out.println("FAILED. This will cause issues see TODO"); //TODO FAILS Anyway. Check db
                }
            });
            //DatabaseManager.insertTemperatures(temperatures);
        } catch (Throwable e) {
            System.out.println("PERSISTANCEHANDLER: Warning: Failed to save temperatures");
            return false;
        }
        return true;
    }
}
