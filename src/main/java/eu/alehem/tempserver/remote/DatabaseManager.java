package eu.alehem.tempserver.remote;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

final class DatabaseManager {

  private static final String DEFAULT_DATABASE_URL = "jdbc:sqlite:tempremote.db";
  private static final String DATABASE_SCHEMA =
      "CREATE TABLE IF NOT EXISTS saved_temperatures"
          + "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
          + " probe TEXT NOT NULL,"
          + " temp DOUBLE NOT NULL,"
          + " timestamp INTEGER NOT NULL,"
          + " unique (probe, temp, timestamp) )";

  private DatabaseManager() throws Exception {
    throw new Exception("Utility class, not to be instantiated");
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

  static void createDataBaseIfNotExists() throws SQLException {
    createDataBaseIfNotExists(DEFAULT_DATABASE_URL);
  }

  static void createDataBaseIfNotExists(String databaseUrl) throws SQLException {
    Connection c = DriverManager.getConnection(databaseUrl);
    Statement stmt = c.createStatement();
    stmt.executeUpdate(DATABASE_SCHEMA);
    stmt.close();
    c.close();
  }

  static void insertTemperatures(final Set<Temperature> temperatures) throws SQLException {
    insertTemperatures(temperatures, DEFAULT_DATABASE_URL);
  }

  static void insertTemperatures(final Set<Temperature> temperatures, final String databaseUrl)
      throws SQLException {
    Connection c = DriverManager.getConnection(databaseUrl);

    Statement stmt = c.createStatement();

    temperatures.forEach(
        throwingConsumerWrapper(
            t ->
                stmt.addBatch(
                    String.format(
                        "INSERT OR IGNORE INTO saved_temperatures(probe, temp, timestamp) VALUES('%s', %f, %d);",
                        t.getProbeSerial(), t.getTemperature(), t.getMeasurementTimeStamp()))));

    stmt.executeBatch();
    stmt.close();
    c.close();
  }

  static Set<Temperature> getTemperatures(final int limit) throws SQLException {
    return getTemperatures(limit, DEFAULT_DATABASE_URL);
  }

  static Set<Temperature> getTemperatures(final int limit, final String database_url)
      throws SQLException {
    Connection c = DriverManager.getConnection(database_url);

    String query = "SELECT * FROM saved_temperatures LIMIT " + limit;

    Statement stmt = c.createStatement();
    stmt.execute(query);
    ResultSet res = stmt.getResultSet();

    Set<Temperature> temperatures = new HashSet<>();
    while (res.next()) {
      temperatures.add(
          new Temperature(res.getString("probe"), res.getDouble("temp"), res.getInt("timestamp")));
    }
    res.close();
    stmt.close();
    c.close();
    return temperatures;
  }

  static void deleteTemperature(Temperature temperature) throws SQLException {
    deleteTemperature(temperature, DEFAULT_DATABASE_URL);
  }

  static void deleteTemperature(Temperature temperature, String databaseUrl) throws SQLException {
    Connection c = DriverManager.getConnection(databaseUrl);

    String query =
        String.format(
            "DELETE FROM saved_temperatures WHERE probe=%s AND temp=%f AND timestamp=%d",
            temperature.getProbeSerial(),
            temperature.getTemperature(),
            temperature.getMeasurementTimeStamp());

    Statement stmt = c.createStatement();
    stmt.execute(query);
    stmt.close();
    c.close();
  }

  static void deleteTemperatures(Set<Temperature> temperatures) throws SQLException {
    deleteTemperatures(temperatures, DEFAULT_DATABASE_URL);
  }

  static void deleteTemperatures(Set<Temperature> temperatures, String databaseUrl)
      throws SQLException {
    Connection c = DriverManager.getConnection(databaseUrl);

    Statement stmt = c.createStatement();

    temperatures.forEach(
        throwingConsumerWrapper(
            t ->
                stmt.addBatch(
                    String.format(
                        "DELETE FROM saved_temperatures WHERE probe=%s AND temp=%f AND timestamp=%d",
                        t.getProbeSerial(), t.getTemperature(), t.getMeasurementTimeStamp()))));

    stmt.executeBatch();
    stmt.close();
    c.close();
  }

  static int countMeasurementsInDb() throws SQLException {
    return countMeasurementsInDb(DEFAULT_DATABASE_URL);
  }

  static int countMeasurementsInDb(String databaseUrl) throws SQLException {
    Connection c = DriverManager.getConnection(databaseUrl);

    String query = "SELECT COUNT(*) FROM saved_temperatures";

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
