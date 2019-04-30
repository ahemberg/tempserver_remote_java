package eu.alehem.tempserver.remote;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.component.temperature.impl.TmpDS18B20DeviceType;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class TempReader implements Runnable {

    private TempQueue queue = TempQueue.getInstance();
    private List<W1Device> probes = getTempProbes();

    @Override
    public void run() {
        probes.stream()
                .map(this::getTemperature)
                .collect(Collectors.toList())
                .forEach(temperature -> queue.addTemperature(temperature));
    }

    private List<W1Device> getTempProbes() {
        W1Master master = new W1Master();
        return master.getDevices(TmpDS18B20DeviceType.FAMILY_CODE);
    }

    private Temperature getTemperature(W1Device probe) {
        String probeSerial = probe.getName().trim();
        double temperature = ((TemperatureSensor) probe).getTemperature();
        Instant measurementTime = Instant.now();
        return new Temperature(probeSerial, temperature, measurementTime);
    }
}
