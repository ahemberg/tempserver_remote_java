package eu.alehem.tempserver.remote.argparser;

import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class ArgParser {

  public static Optional<CommandLine> parseOptions(String... args) {

    Options options = new Options();

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

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd;

    try {
      cmd = parser.parse(options, args);
      return Optional.of(cmd);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("todo-getname-from-opt", options);
      return Optional.empty();
    }
  }
}
