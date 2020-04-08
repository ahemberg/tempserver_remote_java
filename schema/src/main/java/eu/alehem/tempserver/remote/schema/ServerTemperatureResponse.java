package eu.alehem.tempserver.remote.schema;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Bean for server response to temperature save request
 *
 * <p>If connection was successful then the server will respond like this:
 *
 * <p>{ "status":1, "saved_data": [ {"id":83147,"measurement_time":"2019-05-02 09:00:02","temp":6}
 * ], "msg":"TEMP_SAVE_OK" }
 *
 * <p>saved data is a list containing all the measurements that were saved.
 */
@Data
@AllArgsConstructor
public class ServerTemperatureResponse {
  @SerializedName("save_successful")
  private boolean saveSuccessful;

  @SerializedName("saved_measurements")
  private List<UUID> savedMeasurements;

  @SerializedName("response_code")
  private int responseCode;

  @SerializedName("message")
  private String message;
}