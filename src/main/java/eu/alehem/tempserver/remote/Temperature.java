package eu.alehem.tempserver.remote;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Temperature {

    private double temperature;
    private Instant measurementTime;
    private String probeSerial;

    public Temperature(String probeSerial, double temperature, Instant measurementTime) {
        this.probeSerial = probeSerial;
        this.temperature = temperature;
        this.measurementTime = measurementTime;
    }

    public Temperature(String probeSerial, double temperature, long measurementTime) {
        this.probeSerial = probeSerial;
        this.temperature = temperature;
        this.measurementTime = Instant.ofEpochSecond(measurementTime);
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public Instant getMeasurementTime() {
        return measurementTime;
    }

    public long getMeasurementTimeStamp() {
        return measurementTime.getEpochSecond();
    }

    public String getMeasurementTimeServerFormat() {
        LocalDateTime ldt = LocalDateTime.ofInstant(measurementTime.truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC);
        Timestamp current = Timestamp.valueOf(ldt);
        return current.toString();
    }

    public void setMeasurementTime(Instant measurementTime) {
        this.measurementTime = measurementTime;
    }

    public String getProbeSerial() {
        return probeSerial;
    }

    public void setProbeSerial(String probeSerial) {
        this.probeSerial = probeSerial;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Temperature that = (Temperature) o;
        return Double.compare(that.temperature, temperature) == 0 &&
                measurementTime.equals(that.measurementTime) &&
                probeSerial.equals(that.probeSerial);
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperature, measurementTime, probeSerial);
    }
}
