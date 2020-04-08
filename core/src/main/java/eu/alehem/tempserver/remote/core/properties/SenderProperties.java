package eu.alehem.tempserver.remote.core.properties;

import eu.alehem.tempserver.remote.core.argparser.Arguments;
import eu.alehem.tempserver.remote.schema.JsonProperties;
import java.util.UUID;
import lombok.Getter;
import org.apache.commons.cli.CommandLine;

@Getter
public class SenderProperties implements Properties {

  private boolean verbose;
  private int batchSize;
  private int senderFrequency;
  private UUID remoteId;
  private String serverAddress;

  public SenderProperties(CommandLine cmd, JsonProperties json) {
    parseFromConfig();
    parseFromJsonProperties(json);
    parseFromCmdLine(cmd);
  }

  private void parseFromConfig() {
    ConfigProperties config = ConfigProperties.getInstance();
    senderFrequency = Integer.valueOf(config.getProperty("sender.frequency.seconds"));
    batchSize = Integer.valueOf(config.getProperty("sender.batch_size"));
  }

  private void parseFromCmdLine(CommandLine cmd) {
    if (cmd.hasOption(Arguments.VERBOSE.getShortOption())) {
      verbose = true;
    }

    if (cmd.hasOption(Arguments.SERVER_ADDRESS.getLongOption())) {
      remoteId = UUID.fromString(cmd.getOptionValue(Arguments.SERVER_ADDRESS.getLongOption()));
    }
  }

  private void parseFromJsonProperties(JsonProperties json) {
    remoteId = json.getRemoteId();
    serverAddress = json.getServerAddress();
  }
}
