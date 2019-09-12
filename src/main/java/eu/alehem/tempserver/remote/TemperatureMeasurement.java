package eu.alehem.tempserver.remote;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

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

@Data
@AllArgsConstructor
class TemperatureMeasurement {
    @SerializedName("id") private int measurementId;
    @SerializedName("measurement_time") private String measurementTime;
    @SerializedName("temp") private double temperature;
}
