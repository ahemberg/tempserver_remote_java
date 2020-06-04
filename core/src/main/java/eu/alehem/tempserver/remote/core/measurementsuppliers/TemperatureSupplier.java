package eu.alehem.tempserver.remote.core.measurementsuppliers;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.component.temperature.impl.TmpDS18B20DeviceType;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;
import eu.alehem.tempserver.schema.proto.Tempserver;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
public class TemperatureSupplier implements MeasurementSupplier<Set<Tempserver.Measurement>> {

  private static final List<W1Device> PROBES = getTempProbes();

  @Override
  public Set<Tempserver.Measurement> get() {
    log.info("Reading temperature");
    return PROBES.stream().map(TemperatureSupplier::getTemperature).collect(Collectors.toSet());
  }

  private static List<W1Device> getTempProbes() {
    W1Master master = new W1Master();
    return master.getDevices(TmpDS18B20DeviceType.FAMILY_CODE);
  }

  private static Tempserver.Measurement getTemperature(W1Device probe) {
    String probeSerial = probe.getName().trim();
    log.debug("Probe serial " + probeSerial);
    double temperature = ((TemperatureSensor) probe).getTemperature();
    log.debug("Probe temperature " + temperature);
    return Tempserver.Measurement.newBuilder()
        .setType(Tempserver.MeasurementType.TEMPERATURE)
        .setId(UUID.randomUUID().toString())
        .setProbeserial(probeSerial)
        .setValue(temperature)
        .setTimestamp(Instant.now().toEpochMilli())
        .build();
  }
}