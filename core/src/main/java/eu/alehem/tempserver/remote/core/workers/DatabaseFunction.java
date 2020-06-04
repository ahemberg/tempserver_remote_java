package eu.alehem.tempserver.remote.core.workers;

import com.google.protobuf.InvalidProtocolBufferException;
import eu.alehem.tempserver.remote.core.DatabaseManager;
import eu.alehem.tempserver.schema.proto.Tempserver;
import lombok.extern.log4j.Log4j2;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Takes a set of temperatures to be sent and makes sure it has the correct size by adding or
 * removing entries from/to local storage
 */
@Log4j2
public class DatabaseFunction
    implements Function<Set<Tempserver.Measurement>, Set<Tempserver.Measurement>> {

  private static final int MAX_QUEUE_LEN;
  private static final boolean IS_VERBOSE;

  static {
    // TODO read from properties on boot
    MAX_QUEUE_LEN = 10;
    IS_VERBOSE = true;
  }

  @Override
  public Set<Tempserver.Measurement> apply(final Set<Tempserver.Measurement> measurements) {
    try {
      DatabaseManager.createDataBaseIfNotExists();
    } catch (SQLException e) {
      log.warn("Failed to create database!", e);
    }
    if (measurements.size() < MAX_QUEUE_LEN) {
      log.debug("Measurement size shorter than maximum allowed. Trying to backfill");
      log.debug("Measurements length before backfill " + measurements.size());
      log.debug("Getting " + (MAX_QUEUE_LEN - measurements.size()));
      measurements.addAll(getAndRemove(MAX_QUEUE_LEN - measurements.size()));
      log.debug("Measurements length after backfill " + measurements.size());
    } else if (measurements.size() > MAX_QUEUE_LEN) {
      log.debug("Measurement size longer than maximum allowed. Moving some to db");
      log.debug("Moving " + (measurements.size() - MAX_QUEUE_LEN) + " measurements");
      log.debug("Measurements length before empty " + measurements.size());

      final Set<Tempserver.Measurement> toSave =
          new HashSet<>(
              new ArrayList<>(measurements).subList(0, measurements.size() - MAX_QUEUE_LEN));
      log.debug("toSaveSize " + measurements.size());
      try {
        DatabaseManager.insertMeasurements(toSave);
        measurements.removeAll(toSave);
      } catch (SQLException e) {
        log.warn("Failed to save temperatures", e);
      }
      log.debug("Measurements length after emptying " + measurements.size());
    } else {
      log.debug("Queue length is exactly max queue length");
    }
    return measurements;
  }

  private Set<Tempserver.Measurement> getAndRemove(final int numberToGet) {
    try {
      if (IS_VERBOSE) log.info("Populating queue from db");
      final Set<Tempserver.Measurement> measurements = DatabaseManager.getMeasurements(numberToGet);
      DatabaseManager.deleteMeasurements(
          measurements.stream().map(Tempserver.Measurement::getId).collect(Collectors.toSet()));
      final int entriesInDb = DatabaseManager.countMeasurementsInDb();
      if (IS_VERBOSE) {
        log.info("Successfully populated from db. DB now has " + entriesInDb + " entries");
      }
      return measurements;
    } catch (SQLException | InvalidProtocolBufferException e) {
      log.warn("Failed to get from database", e);
      return new HashSet<>();
    }
  }
}
