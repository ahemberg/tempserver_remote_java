package eu.alehem.tempserver.remote.core.properties;

import eu.alehem.tempserver.remote.core.argparser.Arguments;
import eu.alehem.tempserver.remote.schema.JsonProperties;
import lombok.Getter;
import org.apache.commons.cli.CommandLine;

import java.util.UUID;

@Getter
public final class SenderProperties implements Properties {

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
    senderFrequency = Integer.parseInt(config.getProperty("sender.frequency.seconds"));
    batchSize = Integer.parseInt(config.getProperty("sender.batch_size"));
  }

  private void parseFromCmdLine(CommandLine cmd) {
    if (cmd.hasOption(Arguments.VERBOSE.getShortOption())) {
      verbose = true;
    }

    if (cmd.hasOption(Arguments.SERVER_ADDRESS.getLongOption())) {
      serverAddress = cmd.getOptionValue(Arguments.SERVER_ADDRESS.getLongOption());
    }

    if (cmd.hasOption(Arguments.SENDER_UUID.getLongOption())) {
      remoteId = UUID.fromString(cmd.getOptionValue(Arguments.SENDER_UUID.getLongOption()));
    }
  }

  private void parseFromJsonProperties(JsonProperties json) {
    remoteId = json.getRemoteId();
    serverAddress = json.getServerAddress();
  }
}
