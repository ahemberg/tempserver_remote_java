package eu.alehem.tempserver.remote;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.component.temperature.impl.TmpDS18B20DeviceType;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {


    public static void main(String[] args) throws InterruptedException {
        TempQueue queue = TempQueue.getInstance();

        TempReader reader = new TempReader();
        Thread t = new Thread(reader, "Tempreader");

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(t, 0, 2, TimeUnit.MINUTES);

        /*
        try {
            DatabaseManager.createDataBaseIfNotExists();
            DatabaseManager.insertTemperature(t);
            DatabaseManager.getTemperatures().forEach(m -> System.out.println(t.getTemperature() + "C: " + t.getMeasurementTime()));

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

         */
    }
}
