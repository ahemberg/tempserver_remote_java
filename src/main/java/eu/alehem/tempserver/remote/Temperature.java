package eu.alehem.tempserver.remote;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Data
@AllArgsConstructor
public class Temperature {

    private String probeSerial;
    private double temperature;
    private Instant measurementTime;

    public Temperature(String probeSerial, double temperature, long measurementTime) {
        this.probeSerial = probeSerial;
        this.temperature = temperature;
        this.measurementTime = Instant.ofEpochSecond(measurementTime);
    }

    public long getMeasurementTimeStamp() {
        return measurementTime.getEpochSecond();
    }

    public String getMeasurementTimeServerFormat() {
        LocalDateTime ldt = LocalDateTime.ofInstant(measurementTime.truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC);
        Timestamp current = Timestamp.valueOf(ldt);
        return current.toString();
    }
}
