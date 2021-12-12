package eu.alehem.tempserver.remote.core.workers;

import eu.alehem.tempserver.remote.core.DatabaseManager;
import eu.alehem.tempserver.remote.core.measurementsuppliers.TemperatureDS18B20Supplier;
import eu.alehem.tempserver.schema.proto.Tempserver;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Worker implements Runnable {

  //private static final Set<Tempserver.Measurement> measurementQueue = new HashSet<>();
  private final Sender sender;
  private final DatabaseFunction dbFunc;

  public Worker(final Sender sender, final DatabaseFunction dbFunc) {
    this.sender = sender;
    this.dbFunc = dbFunc;
  }

  @Override
  public void run() {

    // TODO Consider running database and measurement concurrently instead. This might speed things
    // up. also it is kind of useless to supply temperature in future and then wait for it to be
    // finished

    // Read temperature, join with current queue, backfill from db or pop and store in db,
    // overwrite current queue, send to server
    Set<String> savedTemperatureIds =
        CompletableFuture.supplyAsync(new TemperatureDS18B20Supplier())
            .thenApply(
                measurements -> {
                  log.debug("New measurements: " + measurements.size());
                  //log.debug("Existing in queue: " + measurementQueue.size());
                  //return Stream.concat(measurements.stream(), measurementQueue.stream())
                  //    .collect(Collectors.toSet());
                  return measurements;
                })
            //.thenApply(dbFunc)
            .thenApply(
                measurements -> {
                  //log.debug("Measurements in queue: " + measurementQueue.size());
                  log.debug("Measurements from db: " + measurements.size());
                  //measurementQueue.clear();
                  //measurementQueue.addAll(measurements);
                  return measurements;
                })
            .thenApply(sender)
            .join();

    log.debug("Measurements saved to server: ");
    savedTemperatureIds.forEach(log::debug);

    // Remove Ids in this set from the queue
    removeTemperaturesById(savedTemperatureIds);
    // Done
    //log.debug("Worker done. Queue now contains " + measurementQueue.size() + " entries");
  }

  public void terminate() {
    //try {
      log.debug("Dumping measurement to database");
      //DatabaseManager.insertMeasurements(measurementQueue);
      //measurementQueue.forEach(measurement -> log.debug(measurement.getId()));
    //} catch (SQLException e) {
      log.error("Failed to save queue, data was lost :(");
    //}
  }

  public void removeTemperaturesById(final Set<String> ids) {
    //final Set<Tempserver.Measurement> temperaturesToRemove =
        //measurementQueue.stream()
        //    .filter(m -> ids.stream().anyMatch(id -> id.equals(m.getId())))
        //    .collect(Collectors.toSet());
    //measurementQueue.removeAll(temperaturesToRemove);
  }
}
