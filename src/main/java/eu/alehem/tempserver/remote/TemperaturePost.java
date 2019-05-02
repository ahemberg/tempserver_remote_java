package eu.alehem.tempserver.remote;


import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Bean for json representation of post.
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
public class TemperaturePost {

    @SerializedName("remote_id") private int remoteId;
    @SerializedName("remote_serial") private String remoteSerial;
    @SerializedName("temperatures") private List<TemperatureMeasurement> temperatures;

    TemperaturePost(int remoteId, String remoteSerial, List<TemperatureMeasurement> temperatures) {
        this.remoteId = remoteId;
        this.remoteSerial = remoteSerial;
        this.temperatures = temperatures;
    }

    public int getRemoteId() {
        return remoteId;
    }

    public String getRemoteSerial() {
        return remoteSerial;
    }

    public List<TemperatureMeasurement> getTemperatures() {
        return temperatures;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemperaturePost that = (TemperaturePost) o;
        return remoteId == that.remoteId &&
                remoteSerial.equals(that.remoteSerial) &&
                temperatures.equals(that.temperatures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(remoteId, remoteSerial, temperatures);
    }

    @Override
    public String toString() {
        return "TemperaturePost{" +
                "remoteId=" + remoteId +
                ", remoteSerial='" + remoteSerial + '\'' +
                ", temperatures=" + temperatures +
                '}';
    }
}