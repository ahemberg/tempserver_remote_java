package eu.alehem.tempserver.remote.core.argparser;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum Arguments {

  // TODO: More of a note: Will not add frequency options for send/persistence. This is
  // because these daemons should be notified on when they need to run instead.

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

  Arguments(
      final String shortOption,
      final String longOption,
      final String parameters,
      final String helpText,
      final boolean isRequired) {
    this.shortOption = shortOption;
    this.longOption = longOption;
    this.parameters = parameters;
    this.helpText = helpText;
    this.isRequired = isRequired;
  }

  public static Optional<Arguments> getArgumentByShortOption(String option) {
    return Arrays.stream(Arguments.values())
        .filter(arg -> arg.getShortOption().equals(option))
        .findFirst();
  }
}
