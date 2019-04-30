package eu.alehem.tempserver.remote;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseManager {

    private static final String DATABASE_URL = "jdbc:sqlite:test.db";
    private static final String DATABASE_SCHEMA =
            "CREATE TABLE IF NOT EXISTS saved_temperatures" +
            "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
            " probe TEXT NOT NULL,"+
            " temp DOUBLE NOT NULL," +
            " timestamp INTEGER NOT NULL )";

    private DatabaseManager() throws Exception {
        throw new Exception("Utility class, not to be instantiated");
    }

    public static void createDataBaseIfNotExists() throws SQLException {
        Connection c = DriverManager.getConnection(DATABASE_URL);
        Statement stmt = c.createStatement();
        stmt.executeUpdate(DATABASE_SCHEMA);
        stmt.close();
        c.close();
    }

    public static void insertTemperature(Temperature temp) throws SQLException {
        Connection c = DriverManager.getConnection(DATABASE_URL);

        String query = String.format(
                "INSERT INTO saved_temperatures(probe, temp, timestamp) VALUES(%s, %f, %d)",
                temp.getProbeSerial(),
                temp.getTemperature(),
                temp.getMeasurementTimeStamp()
        );

        Statement stmt = c.createStatement();

        stmt.executeUpdate(query);
        stmt.close();
        c.close();
    }

    public static List<Temperature> getTemperatures() throws SQLException {
        Connection c = DriverManager.getConnection(DATABASE_URL);

        String query = "SELECT * FROM saved_temperatures";

        Statement stmt = c.createStatement();
        stmt.execute(query);
        ResultSet res = stmt.getResultSet();

        List<Temperature> temperatures = new ArrayList<>();
        while (res.next()) {
            temperatures.add(
                    new Temperature(
                            res.getString("probe"),
                            res.getDouble("temp"),
                            res.getInt("timestamp")
                    )
            );
        }

        res.close();
        stmt.close();
        c.close();
        return temperatures;
    }
}
