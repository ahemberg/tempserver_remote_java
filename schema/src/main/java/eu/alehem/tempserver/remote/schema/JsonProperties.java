package eu.alehem.tempserver.remote.schema;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class JsonProperties {

  @SerializedName("remote_id")
  private UUID remoteId;

  @SerializedName("server_address")
  private String serverAddress;

  @SerializedName("read_freq")
  private Integer measurementFrequency;
}
