package eu.alehem.tempserver.remote;

import com.google.gson.Gson;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {


    public static void main(String[] args) throws SQLException, InterruptedException {
        TempQueue queue = TempQueue.getInstance();

        TempReader reader = new TempReader();
        PersistanceHandler persistanceHandler = new PersistanceHandler();
        TempSender sender = new TempSender();
        Thread tr = new Thread(reader, "Tempreader");
        Thread tp = new Thread(persistanceHandler, "PersistanceHandler");
        Thread ts = new Thread(sender, "Sender");

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(tr, 0, 5000, TimeUnit.MILLISECONDS);

        ScheduledExecutorService exec2 = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(tp, 0, 1, TimeUnit.SECONDS);

        ScheduledExecutorService exec3 = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(ts, 0, 30, TimeUnit.SECONDS);

        while (true) {
            System.out.println(queue.getQueueLen() +" Measurements in the queue");
            System.out.println(DatabaseManager.getTemperatures(1000).size() + "Measurements in db");
            Thread.sleep(10000);
        }
    }
}
