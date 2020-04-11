package eu.alehem.tempserver.remote.core;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class Temperature {

  private final UUID id;
  private final String probeSerial;
  private final double temperature;
  private final Instant measurementTime;

  long getMeasurementTimeStamp() {
    return measurementTime.getEpochSecond();
  }
}