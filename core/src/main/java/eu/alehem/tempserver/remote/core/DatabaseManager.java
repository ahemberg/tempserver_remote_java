package eu.alehem.tempserver.remote.core;

import com.google.protobuf.InvalidProtocolBufferException;
import eu.alehem.tempserver.schema.proto.Tempserver;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class DatabaseManager {

  private DatabaseManager() {}

  private static final String DEFAULT_DATABASE_URL = "jdbc:sqlite:tempremote-new.db";
  private static final String SAVE_QUERY =
      "INSERT OR IGNORE INTO measurements (measurement_id, data) VALUES (?, ?)";
  private static final String GET_QUERY = "SELECT * FROM measurements LIMIT ?";
  private static final String DELETE_QUERY = "DELETE FROM measurements WHERE measurement_id = ?";

  private static final String DATABASE_SCHEMA =
      "CREATE TABLE IF NOT EXISTS measurements"
          + " (id INTEGER PRIMARY KEY AUTOINCREMENT,"
          + " measurement_id STRING NOT NULL,"
          + " data BLOB NOT NULL,"
          + " unique (measurement_id, data))";

  public static void createDataBaseIfNotExists() throws SQLException {
    Connection c = DriverManager.getConnection(DEFAULT_DATABASE_URL);
    Statement stmt = c.createStatement();
    stmt.executeUpdate(DATABASE_SCHEMA);
    stmt.close();
    c.close();
  }

  private static <T> Consumer<T> throwingConsumerWrapper(
      ThrowingConsumer<T, SQLException> throwingConsumer) {
    return i -> {
      try {
        throwingConsumer.accept(i);
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
    };
  }

  public static void insertMeasurements(final Set<Tempserver.Measurement> measurements)
      throws SQLException {
    Connection c = DriverManager.getConnection(DEFAULT_DATABASE_URL);
    PreparedStatement stmt = c.prepareStatement(SAVE_QUERY);

    measurements.forEach(
        throwingConsumerWrapper(
            m -> {
              stmt.setString(1, m.getId());
              stmt.setBytes(2, m.toByteArray());
              stmt.addBatch();
            }));
    stmt.executeBatch();
    stmt.close();
    c.close();
  }

  // TODO if one meaurement is corrupt then nothing will be returned. Might lock up the database.
  // Either just ignore the failed measurements or run a database-cleaner that removes such entries.
  public static Set<Tempserver.Measurement> getMeasurements(final int numToGet)
      throws SQLException, InvalidProtocolBufferException {
    Connection c = DriverManager.getConnection(DEFAULT_DATABASE_URL);
    PreparedStatement stmt = c.prepareStatement(GET_QUERY);
    stmt.setInt(1, numToGet);
    stmt.execute();
    ResultSet resultSet = stmt.getResultSet();

    final Set<Tempserver.Measurement> measurements = new HashSet<>();
    while (resultSet.next()) {
      final byte[] bytes = resultSet.getBytes("data");
      measurements.add(Tempserver.Measurement.parseFrom(bytes));
    }
    return measurements;
  }

  public static void deleteMeasurements(final Set<String> idsToDelete) throws SQLException {
    Connection c = DriverManager.getConnection(DEFAULT_DATABASE_URL);
    PreparedStatement stmt = c.prepareStatement(DELETE_QUERY);

    idsToDelete.forEach(
        throwingConsumerWrapper(
            id -> {
              stmt.setString(1, id);
              stmt.addBatch();
            }));

    stmt.executeBatch();
    stmt.close();
    c.close();
  }

  public static int countMeasurementsInDb() throws SQLException {
    Connection c = DriverManager.getConnection(DEFAULT_DATABASE_URL);

    String query = "SELECT COUNT(*) FROM measurements";

    Statement stmt = c.createStatement();
    stmt.execute(query);
    ResultSet res = stmt.getResultSet();

    res.next();
    int count = res.getInt("COUNT(*)");
    res.close();
    stmt.close();
    c.close();

    return count;
  }
}
