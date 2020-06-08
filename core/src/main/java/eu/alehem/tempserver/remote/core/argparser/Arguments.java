package eu.alehem.tempserver.remote.core.argparser;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Arguments {
  VERBOSE("v", "verbose", null, "Show verbose output", false),
  READ_FREQUENCY(
      null, "read-freq", "Frequency", "Specifies how often to read the temperature", false),
  SENDER_UUID(null, "remote-id", "UUID", "UUID of sender (server token)", false),
  SERVER_ADDRESS(null, "server-address", "URL", "Server URL", false),
  PROPERTIES_FILE("p", "properties-file", "PATH", "Path to properties-file", false),
  SKIP_PROPERTIES_FILE(
      "s",
      "skip-properties",
      null,
      "Specify to skip properties file. If specified then remote address and remote id must be specified by options",
      false);

  private final String shortOption;
  private final String longOption;
  private final String parameters;
  private final String helpText;
  private final boolean isRequired;
}
