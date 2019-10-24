package eu.alehem.tempserver.remote;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class DatabaseManagerTest {

  private final String DATABASE_PATH = "src/test/resources/test.db";
  private final String TEST_DATABASE_URL = "jdbc:sqlite:src/test/resources/test.db";

  private final Set<Temperature> REFERENCE_TEMPERATURES =
      Stream.of(
              new Temperature("1", 2.0, 1),
              new Temperature("2", 2.0, 2),
              new Temperature("3", 2.0, 3),
              new Temperature("4", 2.0, 4),
              new Temperature("5", 2.0, 5),
              new Temperature("6", 2.0, 6),
              new Temperature("7", 2.0, 7),
              new Temperature("8", 2.0, 8),
              new Temperature("9", 2.0, 9),
              new Temperature("10", 2.0, 10))
          .collect(Collectors.toSet());

  private final String DATABASE_SCHEMA =
      "CREATE TABLE IF NOT EXISTS saved_temperatures"
          + "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
          + " temp_id TEXT NOT NULL,"
          + " probe TEXT NOT NULL,"
          + " temp DOUBLE NOT NULL,"
          + " timestamp INTEGER NOT NULL,"
          + " unique (probe, temp, timestamp) )";

  @BeforeClass
  public void beforeClass() throws SQLException {
    Connection c = DriverManager.getConnection(TEST_DATABASE_URL);
    Statement stmt = c.createStatement();
    stmt.executeUpdate(DATABASE_SCHEMA);
    stmt.close();
    c.close();
  }

  @AfterClass
  public void afterClass() {
    // Delete Database
    new File(DATABASE_PATH).delete();
  }

  @AfterMethod
  public void afterMethod() throws SQLException {
    Connection c = DriverManager.getConnection(TEST_DATABASE_URL);
    Statement stmt = c.createStatement();
    stmt.executeUpdate("DELETE FROM saved_temperatures WHERE 1=1");
    stmt.close();
    c.close();
  }

  @Test
  public void testCreateDatabaseIfDoesNotExist() throws SQLException {
    String url = "jdbc:sqlite:src/test/resources/test2.db";
    String path = "src/test/resources/test2.db";
    DatabaseManager.createDataBaseIfNotExists(url);
    boolean deleted = new File(path).delete();
    assertTrue(deleted);
  }

  @Test
  public void testInsertAndGetTemperatures() throws SQLException {
    DatabaseManager.insertTemperatures(REFERENCE_TEMPERATURES, TEST_DATABASE_URL);
    Set<Temperature> fromDatabase = DatabaseManager.getTemperatures(10, TEST_DATABASE_URL);
    assertTrue(fromDatabase.containsAll(REFERENCE_TEMPERATURES));
  }

  @Test
  public void testCountMeasurementsInDb() throws SQLException {
    assertEquals(DatabaseManager.countMeasurementsInDb(TEST_DATABASE_URL), 0);

    DatabaseManager.insertTemperatures(
        Stream.of(new Temperature("1", 2.0, 1), new Temperature("1", 2.0, 2))
            .collect(Collectors.toSet()),
        TEST_DATABASE_URL);
    assertEquals(DatabaseManager.countMeasurementsInDb(TEST_DATABASE_URL), 2);
  }

  @Test
  public void testAddingIdenticalMeasurementsTwiceYieldsOneEntry() throws SQLException {
    Set<Temperature> temperatureSet =
        Stream.of(new Temperature("1", 2.0, 1), new Temperature("1", 2.0, 1))
            .collect(Collectors.toSet());

    DatabaseManager.insertTemperatures(temperatureSet, TEST_DATABASE_URL);
    Set<Temperature> tempsInDb = DatabaseManager.getTemperatures(9999, TEST_DATABASE_URL);
    assertEquals(tempsInDb.size(), 1);
    assertEquals(DatabaseManager.countMeasurementsInDb(TEST_DATABASE_URL), 1);
  }

  @Test
  public void testDeleteTemperature() throws SQLException {
    Temperature toRemove = new Temperature("1", 2.0, 1);

    DatabaseManager.insertTemperatures(REFERENCE_TEMPERATURES, TEST_DATABASE_URL);
    DatabaseManager.deleteTemperature(toRemove, TEST_DATABASE_URL);
    assertEquals(
        DatabaseManager.countMeasurementsInDb(TEST_DATABASE_URL),
        REFERENCE_TEMPERATURES.size() - 1);
    assertFalse(DatabaseManager.getTemperatures(9999, TEST_DATABASE_URL).contains(toRemove));
  }

  @Test
  public void testDeleteTemperatures() throws SQLException {

    Set<Temperature> toRemove =
        Stream.of(
                new Temperature("1", 2.0, 1),
                new Temperature("2", 2.0, 2),
                new Temperature("3", 2.0, 3))
            .collect(Collectors.toSet());

    DatabaseManager.insertTemperatures(REFERENCE_TEMPERATURES, TEST_DATABASE_URL);
    DatabaseManager.deleteTemperatures(toRemove, TEST_DATABASE_URL);
    assertEquals(
        DatabaseManager.countMeasurementsInDb(TEST_DATABASE_URL),
        REFERENCE_TEMPERATURES.size() - 3);
    assertFalse(DatabaseManager.getTemperatures(9999, TEST_DATABASE_URL).containsAll(toRemove));
  }
}
