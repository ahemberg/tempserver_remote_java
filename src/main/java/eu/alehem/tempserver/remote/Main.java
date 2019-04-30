package eu.alehem.tempserver.remote;


import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {


    public static void main(String[] args) throws SQLException, InterruptedException {
        TempQueue queue = TempQueue.getInstance();

        TempReader reader = new TempReader();
        PersistanceHandler persistanceHandler = new PersistanceHandler();
        Thread tr = new Thread(reader, "Tempreader");
        Thread tp = new Thread(persistanceHandler, "PersistanceHandler");


        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(tr, 0, 5, TimeUnit.SECONDS);

        ScheduledExecutorService exec2 = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(tp, 0, 1, TimeUnit.SECONDS);

        while (true) {
            System.out.println(queue.getQueueLen() +" Measurements in the queue");
            System.out.println(DatabaseManager.getTemperatures(1000).size() + "Measurements in db");
            Thread.sleep(5000);
        }


    }
}
