package eu.alehem.tempserver.remote;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Test
public class DatabaseManagerTest {

    private final String url = "jdbc:sqlite:src/test/resources/test.db";

    private final String DATABASE_SCHEMA =
            "CREATE TABLE IF NOT EXISTS saved_temperatures"
                    + "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " probe TEXT NOT NULL,"
                    + " temp DOUBLE NOT NULL,"
                    + " timestamp INTEGER NOT NULL,"
                    + " unique (probe, temp, timestamp) )";

    @BeforeClass
    public void beforeClass() {
        //Create Database
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(DATABASE_SCHEMA);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @AfterClass
    public void afterClass() {
        //Delete Database
    }

    @Test
    public void testInsertTemperatures() throws SQLException {
        Set<Temperature> temperatureSet = Stream.of(
        new Temperature("1", 2.0, Instant.now()),
        new Temperature("2", 2.0, Instant.now()),
        new Temperature("3", 2.0, Instant.now()),
        new Temperature("4", 2.0, Instant.now()),
        new Temperature("5", 2.0, Instant.now()),
        new Temperature("6", 2.0, Instant.now()),
        new Temperature("7", 2.0, Instant.now()),
        new Temperature("8", 2.0, Instant.now()),
        new Temperature("9", 2.0, Instant.now()),
        new Temperature("10", 2.0, Instant.now())).collect(Collectors.toSet());

        DatabaseManager.insertTemperatures(temperatureSet, url);
    }

    @Test
    public void fooTest() {
        assertThat(1, is(1));
    }
}
