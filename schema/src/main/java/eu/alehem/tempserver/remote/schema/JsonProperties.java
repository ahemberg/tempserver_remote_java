package eu.alehem.tempserver.remote.schema;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class JsonProperties {

  @SerializedName("remote_id")
  private final UUID remoteId;

  @SerializedName("server_address")
  private final String serverAddress;

  @SerializedName("read_freq")
  private final Integer measurementFrequency;
}
