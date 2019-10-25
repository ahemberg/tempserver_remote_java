package eu.alehem.tempserver.remote.properties;

import eu.alehem.tempserver.remote.argparser.Arguments;
import lombok.Getter;
import org.apache.commons.cli.CommandLine;

@Getter
public class PersistenceProperties implements Properties {

  private int maxQueueLength;
  private int deleteThreshold;
  private int addThreshold;
  private int batchSize;
  private int runfrequency;
  private boolean verbose;

  public PersistenceProperties(CommandLine cmd) {
    parseFromConfig();
    parseFromCmdLine(cmd);
  }

  private void parseFromCmdLine(CommandLine cmd) {
    if (cmd.hasOption(Arguments.VERBOSE.getShortOption())) {
      verbose = true;
    }
  }

  private void parseFromConfig() {
    ConfigProperties config = ConfigProperties.getInstance();
    runfrequency = Integer.valueOf(config.getProperty("persistence.frequency.seconds"));
    maxQueueLength = Integer.valueOf(config.getProperty("persistence.max_queue_len"));
    deleteThreshold = Integer.valueOf(config.getProperty("persistence.delete_threshold"));
    addThreshold = Integer.valueOf(config.getProperty("persistence.add_threshold"));
    batchSize = Integer.valueOf(config.getProperty("persistence.batch_size"));
  }
}
