package eu.alehem.tempserver.remote;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
class Temperature {

  private final UUID id;
  private final String probeSerial;
  private final double temperature;
  private final Instant measurementTime;

  Temperature(final String probeSerial, final double temperature, final long measurementTime) {
    this.id = UUID.randomUUID();
    this.probeSerial = probeSerial;
    this.temperature = temperature;
    this.measurementTime = Instant.ofEpochSecond(measurementTime);
  }

  long getMeasurementTimeStamp() {
    return measurementTime.getEpochSecond();
  }
}
