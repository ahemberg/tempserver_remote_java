package eu.alehem.tempserver.remote.core.argparser;

import org.apache.commons.cli.*;

import java.util.Arrays;
import java.util.Optional;

public class ArgParser {

  public static Optional<CommandLine> parseOptions(String... args) {

    final Options options = new Options();

    Arrays.stream(Arguments.values())
        .forEach(
            arg -> {
              Option option =
                  new Option(
                      arg.getShortOption(),
                      arg.getLongOption(),
                      arg.getParameters() != null,
                      arg.getHelpText());
              option.setRequired(arg.isRequired());
              options.addOption(option);
            });

    final CommandLineParser parser = new DefaultParser();
    final HelpFormatter formatter = new HelpFormatter();
    final CommandLine cmd;

    try {
      cmd = parser.parse(options, args);
      return Optional.of(cmd);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("todo-get-name-from-opt", options);
      return Optional.empty();
    }
  }
}
