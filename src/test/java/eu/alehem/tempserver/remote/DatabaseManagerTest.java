package eu.alehem.tempserver.remote;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

@Test
public class DatabaseManagerTest {

    private final String DATABASE_PATH = "src/test/resources/test.db";
    private final String url = "jdbc:sqlite:src/test/resources/test.db";

    private final String DATABASE_SCHEMA =
            "CREATE TABLE IF NOT EXISTS saved_temperatures"
                    + "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " probe TEXT NOT NULL,"
                    + " temp DOUBLE NOT NULL,"
                    + " timestamp INTEGER NOT NULL,"
                    + " unique (probe, temp, timestamp) )";

    @BeforeClass
    public void beforeClass() throws SQLException {
        Connection c = DriverManager.getConnection(url);
        Statement stmt = c.createStatement();
        stmt.executeUpdate(DATABASE_SCHEMA);
        stmt.close();
        c.close();
    }

    @AfterClass
    public void afterClass() {
        //Delete Database
        new File(DATABASE_PATH).delete();
    }

    @AfterMethod
    public void afterMethod() throws SQLException {
        Connection c = DriverManager.getConnection(url);
        Statement stmt = c.createStatement();
        stmt.executeUpdate("DELETE FROM saved_temperatures WHERE 1=1");
        stmt.close();
        c.close();
    }

    @Test
    public void testInsertAndGetTemperatures() throws SQLException {
        Set<Temperature> temperatureSet = Stream.of(
        new Temperature("1", 2.0, Instant.ofEpochSecond(1)),
        new Temperature("2", 2.0, Instant.ofEpochSecond(2)),
        new Temperature("3", 2.0, Instant.ofEpochSecond(3)),
        new Temperature("4", 2.0, Instant.ofEpochSecond(4)),
        new Temperature("5", 2.0, Instant.ofEpochSecond(5)),
        new Temperature("6", 2.0, Instant.ofEpochSecond(6)),
        new Temperature("7", 2.0, Instant.ofEpochSecond(7)),
        new Temperature("8", 2.0, Instant.ofEpochSecond(8)),
        new Temperature("9", 2.0, Instant.ofEpochSecond(9)),
        new Temperature("10", 2.0, Instant.ofEpochSecond(10))).collect(Collectors.toSet());

        DatabaseManager.insertTemperatures(temperatureSet, url);
        Set<Temperature> fromDatabase = DatabaseManager.getTemperatures(10, url);
        assertTrue(fromDatabase.containsAll(temperatureSet));
    }

    @Test
    public void fooTest() {
        assertThat(1, is(1));
    }
}
