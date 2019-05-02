package eu.alehem.tempserver.remote;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * Bean for json representation of temperature data in server posrt.
 * Json representation should look like this:
 *
 * {
 *  "remote_id":2,
 *  "remote_serial":"00000000786ef9ef",
 *  "temperatures":[
 *      {
 *          "id":83114,
 *          "measurement_time":"2019-05-02 07:54:02",
 *          "temp":5.0
 *       }
 *   ]
 *  }
 *
 */
class TemperatureMeasurement {
    @SerializedName("id") private int measurementId;
    @SerializedName("measurement_time") private String measurementTime;
    @SerializedName("temp") private double temperature;

    TemperatureMeasurement(int measurementId, String measurementTime, double temperature) {
        this.measurementId = measurementId;
        this.measurementTime = measurementTime;
        this.temperature = temperature;
    }

    public int getMeasurementId() {
        return measurementId;
    }

    public String getMeasurementTime() {
        return measurementTime;
    }

    public double getTemperature() {
        return temperature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemperatureMeasurement that = (TemperatureMeasurement) o;
        return measurementId == that.measurementId &&
                Double.compare(that.temperature, temperature) == 0 &&
                measurementTime.equals(that.measurementTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(measurementId, measurementTime, temperature);
    }
}
