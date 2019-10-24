package eu.alehem.tempserver.remote;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
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

  private static final UUID[] uuidArr = {
      UUID.fromString("3a161f42-84a7-479c-884b-496af3e97308"),
      UUID.fromString("9e83fafa-51a6-4ace-b106-c5c5717629bf"),
      UUID.fromString("d9eb45f6-e509-4228-bf3f-9679cf8f4637"),
      UUID.fromString("1b8d7b12-e16e-467a-8be2-d282612fa6e8"),
      UUID.fromString("b79fdf93-5562-4c25-9c3d-cc5a473086e5"),
      UUID.fromString("b015913c-b522-4f0c-bfa9-97afd023a063"),
      UUID.fromString("43728129-87dc-47dc-bd2d-5f35936f86ef"),
      UUID.fromString("efe98e05-81d3-44e4-ba75-3c5425420e40"),
      UUID.fromString("aea267bd-099d-42da-94c4-eccc695d962b"),
      UUID.fromString("1c7c8b60-2920-4a90-ad16-eae1bec48663"),
      UUID.fromString("c2d01f03-aa07-49c6-aea5-eb954139ddc8"),
      UUID.fromString("6c8a2114-8ba8-4dc7-b47c-f89becd2d927"),
      UUID.fromString("3df3291d-330a-4558-a16a-6e77348f6753"),
      UUID.fromString("cdc9f989-cfe7-4a75-8b95-34daaaf03cfd"),
      UUID.fromString("30335681-685e-45ff-a61d-a3e72bc379be"),
      UUID.fromString("417c695b-e4e5-4cf1-aec7-67ddc96c8fcf"),
      UUID.fromString("7b8c8a3d-65bc-4c26-891d-72ff6c4b3e50"),
      UUID.fromString("c984042b-2831-4bd4-812f-5243889c28ae"),
      UUID.fromString("34968107-1606-4893-a790-cde4d985230f"),
      UUID.fromString("13f46732-7f15-48fe-a069-4656d8f6eb87")
  };

  private final Set<Temperature> REFERENCE_TEMPERATURES =
      Stream.of(
              new Temperature(uuidArr[0],"1", 2.0, Instant.ofEpochSecond(1)),
              new Temperature(uuidArr[1],"2", 2.0, Instant.ofEpochSecond(2)),
              new Temperature(uuidArr[2],"3", 2.0, Instant.ofEpochSecond(3)),
              new Temperature(uuidArr[3],"4", 2.0, Instant.ofEpochSecond(4)),
              new Temperature(uuidArr[4],"5", 2.0, Instant.ofEpochSecond(5)),
              new Temperature(uuidArr[5],"6", 2.0, Instant.ofEpochSecond(6)),
              new Temperature(uuidArr[6],"7", 2.0, Instant.ofEpochSecond(7)),
              new Temperature(uuidArr[7],"8", 2.0, Instant.ofEpochSecond(8)),
              new Temperature(uuidArr[8],"9", 2.0, Instant.ofEpochSecond(9)),
              new Temperature(uuidArr[9],"10", 2.0, Instant.ofEpochSecond(10)))
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
        Stream.of(new Temperature(UUID.randomUUID(),"1", 2.0, Instant.ofEpochSecond(1)), new Temperature(UUID.randomUUID(), "1", 2.0, Instant.ofEpochSecond(2)))
            .collect(Collectors.toSet()),
        TEST_DATABASE_URL);
    assertEquals(DatabaseManager.countMeasurementsInDb(TEST_DATABASE_URL), 2);
  }

  @Test
  public void testAddingIdenticalMeasurementsTwiceYieldsOneEntry() throws SQLException {
    UUID uuid = UUID.randomUUID();
    Set<Temperature> temperatureSet =
        Stream.of(new Temperature(uuid, "1", 2.0, Instant.ofEpochSecond(1)), new Temperature(uuid,"1", 2.0, Instant.ofEpochSecond(1)))
            .collect(Collectors.toSet());

    DatabaseManager.insertTemperatures(temperatureSet, TEST_DATABASE_URL);
    Set<Temperature> tempsInDb = DatabaseManager.getTemperatures(9999, TEST_DATABASE_URL);
    assertEquals(tempsInDb.size(), 1);
    assertEquals(DatabaseManager.countMeasurementsInDb(TEST_DATABASE_URL), 1);
  }

  @Test
  public void testDeleteTemperature() throws SQLException {
    Temperature toRemove = new Temperature(uuidArr[0], "1", 2.0, Instant.ofEpochSecond(1));

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
                new Temperature(uuidArr[0], "1", 2.0, Instant.ofEpochSecond(1)),
                new Temperature(uuidArr[1], "2", 2.0, Instant.ofEpochSecond(2)),
                new Temperature(uuidArr[2], "3", 2.0, Instant.ofEpochSecond(3)))
            .collect(Collectors.toSet());

    DatabaseManager.insertTemperatures(REFERENCE_TEMPERATURES, TEST_DATABASE_URL);
    DatabaseManager.deleteTemperatures(toRemove, TEST_DATABASE_URL);
    assertEquals(
        DatabaseManager.countMeasurementsInDb(TEST_DATABASE_URL),
        REFERENCE_TEMPERATURES.size() - 3);
    assertFalse(DatabaseManager.getTemperatures(9999, TEST_DATABASE_URL).containsAll(toRemove));
  }

  @Test
  public void testDeleteTemperaturesWithWrongIdFails() throws SQLException {

    Set<Temperature> toRemove =
        Stream.of(
            new Temperature(uuidArr[3], "not_in_db", 2.0, Instant.ofEpochSecond(1)),
            new Temperature(uuidArr[4], "not_in_db", 2.0, Instant.ofEpochSecond(2)),
            new Temperature(uuidArr[5], "not_in_db", 2.0, Instant.ofEpochSecond(3)))
            .collect(Collectors.toSet());

    DatabaseManager.insertTemperatures(REFERENCE_TEMPERATURES, TEST_DATABASE_URL);
    DatabaseManager.deleteTemperatures(toRemove, TEST_DATABASE_URL);

    assertEquals(
        DatabaseManager.countMeasurementsInDb(TEST_DATABASE_URL),
        REFERENCE_TEMPERATURES.size());
    assertFalse(DatabaseManager.getTemperatures(9999, TEST_DATABASE_URL).containsAll(toRemove));
  }
}
