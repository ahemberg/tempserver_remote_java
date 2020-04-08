package eu.alehem.tempserver.remote.core;

import com.google.gson.Gson;
import com.pi4j.system.SystemInfo;
import eu.alehem.tempserver.remote.core.argparser.ArgParser;
import eu.alehem.tempserver.remote.core.argparser.Arguments;
import eu.alehem.tempserver.remote.core.exceptions.InvalidArgumentsException;
import eu.alehem.tempserver.remote.schema.JsonProperties;
import eu.alehem.tempserver.remote.core.properties.PersistenceProperties;
import eu.alehem.tempserver.remote.core.properties.ReaderProperties;
import eu.alehem.tempserver.remote.core.properties.SenderProperties;
import eu.alehem.tempserver.remote.core.workers.PersistenceHandler;
import eu.alehem.tempserver.remote.core.workers.StatusMonitor;
import eu.alehem.tempserver.remote.core.workers.TempReader;
import eu.alehem.tempserver.remote.core.workers.TempSender;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.commons.cli.CommandLine;

// TODO: Just a thought. Why save persistence to a database? Why not just serialize and dump the
// queue? Maybe makes things slimmer?

@Log
public class Main {

  private static ScheduledExecutorService exec = Executors.newScheduledThreadPool(4);

  public static void main(String... args) throws SQLException, InterruptedException {

    Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown()));

    // TODO: Move parse-logic to argparser
    try {
      CommandLine cmd =
          ArgParser.parseOptions(args)
              .orElseThrow(() -> new InvalidArgumentsException("Failed to parse arguments"));
      JsonProperties jsonProperties = readJsonProperties(cmd);
      ReaderProperties readerProperties = new ReaderProperties(cmd, jsonProperties);
      SenderProperties senderProperties = new SenderProperties(cmd, jsonProperties);
      PersistenceProperties persistenceProperties = new PersistenceProperties(cmd);

      final String piSerial = SystemInfo.getSerial();

      exec.scheduleAtFixedRate(
          new Thread(new TempReader(readerProperties), "TempReader"),
          0,
          readerProperties.getReaderFrequency(),
          TimeUnit.SECONDS);
      exec.scheduleAtFixedRate(
          new Thread(new PersistenceHandler(persistenceProperties), "PersistenceHandler"),
          3,
          persistenceProperties.getRunfrequency(),
          TimeUnit.SECONDS);
      exec.scheduleAtFixedRate(
          new Thread(new TempSender(piSerial, senderProperties), "Sender"),
          11,
          senderProperties.getSenderFrequency(),
          TimeUnit.SECONDS);

      if (cmd.hasOption(Arguments.VERBOSE.getShortOption())) {
        exec.scheduleAtFixedRate(
            new Thread(new StatusMonitor(), "Monitor"), 0, 10, TimeUnit.SECONDS);
      }

    } catch (InvalidArgumentsException | IOException e) {
      System.out.println(e.getMessage());
      System.exit(1);
    }
  }

  private static JsonProperties readJsonProperties(CommandLine cmd)
      throws InvalidArgumentsException, FileNotFoundException {
    JsonProperties jsonProperties;
    if (cmd.hasOption(Arguments.SKIP_PROPERTIES_FILE.getShortOption())) {
      if (!cmd.hasOption(Arguments.SENDER_UUID.getLongOption())
          || !cmd.hasOption(Arguments.SERVER_ADDRESS.getLongOption())
          || !cmd.hasOption(Arguments.READ_FREQUENCY.getLongOption())) {
        throw new InvalidArgumentsException(
            "Must supply remote-id, server-address and read-freq when skipping json!");
      }
      jsonProperties =
          new JsonProperties(
              UUID.fromString(cmd.getOptionValue(Arguments.SENDER_UUID.getLongOption())),
              cmd.getOptionValue(Arguments.SERVER_ADDRESS.getLongOption()),
              Integer.valueOf(Arguments.READ_FREQUENCY.getLongOption()));
    } else {
      String propertiesPath =
          cmd.hasOption(Arguments.PROPERTIES_FILE.getShortOption())
              ? cmd.getOptionValue(Arguments.PROPERTIES_FILE.getShortOption())
              : "properties.json";
      jsonProperties = new Gson().fromJson(new FileReader(propertiesPath), JsonProperties.class);
    }
    return jsonProperties;
  }

  private static final class Shutdown implements Runnable {

    @SneakyThrows
    @Override
    public void run() {
      log.info("Shutting down... (will try to save measurements to disk)");
      exec.shutdown();
      System.out.println("Waiting for workers to finish...");
      exec.awaitTermination(3, TimeUnit.SECONDS);
      final TempQueue queue = TempQueue.getInstance();
      if (queue.getQueueLen() == 0) {
        System.out.println("No backlog to clear, quitting");
        return;
      }

      System.out.println("Dumping queue to disk");
      DatabaseManager.createDataBaseIfNotExists();
      Set<Temperature> temperatures;
      while (queue.getQueueLen() > 0) {
        temperatures = queue.getN(Math.min(queue.getQueueLen(), 10_000));
        DatabaseManager.insertTemperatures(temperatures);
        queue.removeTemperatures(temperatures);
      }

      System.out.println("Done dumping to disk. Bye!");
    }
  }
}
