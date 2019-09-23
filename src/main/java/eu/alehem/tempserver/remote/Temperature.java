package eu.alehem.tempserver.remote;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Data
@AllArgsConstructor
class Temperature {

  private String probeSerial;
  private double temperature;
  private Instant measurementTime;

  Temperature(String probeSerial, double temperature, long measurementTime) {
    this.probeSerial = probeSerial;
    this.temperature = temperature;
    this.measurementTime = Instant.ofEpochSecond(measurementTime);
  }

  long getMeasurementTimeStamp() {
    return measurementTime.getEpochSecond();
  }

  String getMeasurementTimeServerFormat() {
    LocalDateTime ldt =
        LocalDateTime.ofInstant(measurementTime.truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC);
    Timestamp current = Timestamp.valueOf(ldt);
    return current.toString();
  }
}
