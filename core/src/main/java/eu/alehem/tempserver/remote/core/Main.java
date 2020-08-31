package eu.alehem.tempserver.remote.core;

import com.google.gson.Gson;
import com.pi4j.system.SystemInfo;
import eu.alehem.tempserver.remote.core.argparser.ArgParser;
import eu.alehem.tempserver.remote.core.argparser.Arguments;
import eu.alehem.tempserver.remote.core.exceptions.InvalidArgumentsException;
import eu.alehem.tempserver.remote.core.measurementsuppliers.TemperatureDS18B20Supplier;
import eu.alehem.tempserver.remote.core.workers.DatabaseFunction;
import eu.alehem.tempserver.remote.core.workers.Sender;
import eu.alehem.tempserver.remote.core.workers.Worker;
import eu.alehem.tempserver.remote.schema.JsonProperties;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
public class Main {

  private static final String MY_SERIAL;
  private static final ScheduledExecutorService EXEC = Executors.newScheduledThreadPool(4);

  private static String remoteId;
  private static String serverAddress;
  private static int measurementFrequency;

  static {
    String mySerial = "";
    try {
      mySerial = SystemInfo.getSerial();
    } catch (IOException | InterruptedException e) {
      log.error("Failed to read system serial. Will exit.", e);
      System.exit(1);
    }
    MY_SERIAL = mySerial;
  }

  public static void main(String... args) {
    try {
      parseArguments(args);
    } catch (InvalidArgumentsException | IOException e) {
      log.error("Failed to start", e);
      System.exit(1);
    }

    final Sender sender = new Sender(MY_SERIAL, remoteId, serverAddress);
    final DatabaseFunction dbFunc = new DatabaseFunction();

    //TODO: Make this list of suppliers be generated from a parameter so that it is possible to choose which sensors
    //to use.

    final TemperatureDS18B20Supplier temperatureSupplier = new TemperatureDS18B20Supplier();

    final Worker worker = new Worker(sender, dbFunc, temperatureSupplier);

    Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown(worker)));

    EXEC.scheduleAtFixedRate(
        new Thread(worker, "Worker"), 0, measurementFrequency, TimeUnit.SECONDS);
  }

  private static void parseArguments(String... args) throws InvalidArgumentsException, IOException {
    CommandLine cmd =
        ArgParser.parseOptions(args)
            .orElseThrow(() -> new InvalidArgumentsException("Failed to parse arguments"));

    if (cmd.hasOption(Arguments.SKIP_PROPERTIES_FILE.getShortOption())) {
      if (!cmd.hasOption(Arguments.SENDER_UUID.getLongOption())
          || !cmd.hasOption(Arguments.SERVER_ADDRESS.getLongOption())
          || !cmd.hasOption(Arguments.READ_FREQUENCY.getLongOption())) {
        throw new InvalidArgumentsException(
            "Must supply remote-id, server-address and read-freq when skipping json!");
      }
    } else {
      String propertiesPath =
          cmd.hasOption(Arguments.PROPERTIES_FILE.getShortOption())
              ? cmd.getOptionValue(Arguments.PROPERTIES_FILE.getShortOption())
              : "properties.json";
      final JsonProperties jsonProperties =
          new Gson().fromJson(new FileReader(propertiesPath), JsonProperties.class);
      serverAddress = jsonProperties.getServerAddress();
      remoteId = jsonProperties.getRemoteId();
      measurementFrequency = jsonProperties.getMeasurementFrequency();
    }

    // TODO: Read up on config and make this nicer
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();
    final LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

    if (cmd.hasOption(Arguments.VERBOSE.getShortOption())) {
      loggerConfig.setLevel(Level.TRACE);
    } else {
      loggerConfig.setLevel(Level.INFO);
    }
    ctx.updateLoggers();

    if (cmd.hasOption(Arguments.READ_FREQUENCY.getLongOption())) {
      measurementFrequency =
          Integer.parseInt(cmd.getOptionValue(Arguments.READ_FREQUENCY.getLongOption()));
    }
    if (cmd.hasOption(Arguments.SERVER_ADDRESS.getLongOption())) {
      serverAddress = cmd.getOptionValue(Arguments.SERVER_ADDRESS.getLongOption());
    }
    if (cmd.hasOption(Arguments.SENDER_UUID.getLongOption())) {
      remoteId = cmd.getOptionValue(Arguments.SENDER_UUID.getLongOption());
    }
  }

  private static final class Shutdown implements Runnable {

    private final Worker worker;

    public Shutdown(final Worker worker) {
      this.worker = worker;
    }

    @SneakyThrows
    @Override
    public void run() {
      log.info("Shutting down... (will try to save measurements to disk)");
      EXEC.shutdown();
      log.info("Waiting for workers to finish...");
      EXEC.awaitTermination(3, TimeUnit.SECONDS);
      log.info("Execution terminated. Dumping remaining queue to db");
      worker.terminate();
      log.info("Done, bye!");
    }
  }
}
