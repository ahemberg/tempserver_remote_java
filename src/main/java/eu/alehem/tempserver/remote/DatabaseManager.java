package eu.alehem.tempserver.remote;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public final class DatabaseManager {

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

  public static void createDataBaseIfNotExists() throws SQLException {
    Connection c = DriverManager.getConnection(DEFAULT_DATABASE_URL);
    Statement stmt = c.createStatement();
    stmt.executeUpdate(DATABASE_SCHEMA);
    stmt.close();
    c.close();
  }

  static void insertTemperatures(final Set<Temperature> temperatures) throws SQLException {
    insertTemperatures(temperatures, DEFAULT_DATABASE_URL);
  }

  static void insertTemperatures(final Set<Temperature> temperatures, final String databaseUrl) throws SQLException {
    Connection c = DriverManager.getConnection(databaseUrl);

    Statement stmt = c.createStatement();
    for (Temperature temp : temperatures) {
      stmt.addBatch(
          String.format(
              "INSERT OR IGNORE INTO saved_temperatures(probe, temp, timestamp) VALUES('%s', %f, %d);",
              temp.getProbeSerial(), temp.getTemperature(), temp.getMeasurementTimeStamp()));
    }

    stmt.executeBatch();
    stmt.close();
    c.close();
  }

  static Set<Temperature> getTemperatures(final int limit) throws SQLException {
    return getTemperatures(limit, DEFAULT_DATABASE_URL);
  }

  static Set<Temperature> getTemperatures(final int limit, final String database_url) throws SQLException {
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

  public static void deleteTemperature(Temperature temperature) throws SQLException {
    Connection c = DriverManager.getConnection(DEFAULT_DATABASE_URL);

    String query =
        "DELETE FROM saved_temperatures "
            + "WHERE probe="
            + temperature.getProbeSerial()
            + "AND temp="
            + temperature.getTemperature()
            + "AND timestamp="
            + temperature.getMeasurementTimeStamp();

    Statement stmt = c.createStatement();
    stmt.execute(query);
    stmt.close();
    c.close();
  }

  public static void deleteTemperatures(Set<Temperature> temperatures) throws SQLException {
    Connection c = DriverManager.getConnection(DEFAULT_DATABASE_URL);

    Statement stmt = c.createStatement();

    for (Temperature temperature : temperatures) {
      stmt.addBatch(
          "DELETE FROM saved_temperatures "
              + "WHERE probe='"
              + temperature.getProbeSerial()
              + "'"
              + " AND temp="
              + temperature.getTemperature()
              + " AND timestamp="
              + temperature.getMeasurementTimeStamp());
    }

    stmt.executeBatch();
    stmt.close();
    c.close();
  }

  public static int countMeasurementsInDb() throws SQLException {
    Connection c = DriverManager.getConnection(DEFAULT_DATABASE_URL);

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
