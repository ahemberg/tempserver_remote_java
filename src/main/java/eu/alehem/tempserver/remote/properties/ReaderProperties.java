package eu.alehem.tempserver.remote.properties;

import eu.alehem.tempserver.remote.argparser.Arguments;
import eu.alehem.tempserver.remote.json.JsonProperties;
import lombok.Getter;
import org.apache.commons.cli.CommandLine;

@Getter
public class ReaderProperties implements Properties {

  private boolean verbose = false;
  private int readerFrequency;

  public ReaderProperties(CommandLine cmd, JsonProperties json) {
    parseFromConfig();
    parseFromJsonProperties(json);
    parseFromCmdLine(cmd);
  }

  private void parseFromConfig() {
    ConfigProperties config = ConfigProperties.getInstance();
    readerFrequency = Integer.parseInt(config.getProperty("reader.frequency.seconds"));
  }

  private void parseFromCmdLine(CommandLine cmd) {
    if (cmd.hasOption(Arguments.VERBOSE.getShortOption())) {
      verbose = true;
    }

    if (cmd.hasOption(Arguments.READ_FREQUENCY.getLongOption())) {
      readerFrequency =
          Integer.parseInt(cmd.getOptionValue(Arguments.READ_FREQUENCY.getLongOption()));
    }
  }

  private void parseFromJsonProperties(JsonProperties json) {
    readerFrequency = json.getMeasurementFrequency();
  }
}
