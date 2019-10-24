package eu.alehem.tempserver.remote;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Bean for json representation of post. Json representation should look like this:
 *
 * <p>{ "remote_id":2, "remote_serial":"00000000786ef9ef", "temperatures":[ { "id":83114,
 * "measurement_time":"2019-05-02 07:54:02", "temp":5.0 } ] }
 */
@Data
@AllArgsConstructor
class TemperaturePost {

  @SerializedName("id")
  private UUID remoteId;

  @SerializedName("remote_serial")
  private String remoteSerial;

  @SerializedName("temperatures")
  private List<TemperatureMeasurement> temperatures;
}
