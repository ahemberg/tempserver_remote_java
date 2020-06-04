package eu.alehem.tempserver.remote.schema;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public final class JsonProperties {

  @SerializedName("remote_id")
  private final String remoteId;

  @SerializedName("server_address")
  private final String serverAddress;

  @SerializedName("read_freq")
  private final Integer measurementFrequency;
}
