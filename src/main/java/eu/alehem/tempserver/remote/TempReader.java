package eu.alehem.tempserver.remote;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.component.temperature.impl.TmpDS18B20DeviceType;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.java.Log;

@Log
public class TempReader implements Runnable {

  private final List<W1Device> probes = getTempProbes();
  private TempQueue queue = TempQueue.getInstance();

  @Override
  public void run() {
    log.info("Reading temperatures");
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
    return new Temperature(UUID.randomUUID(), probeSerial, temperature, Instant.now());
  }
}
