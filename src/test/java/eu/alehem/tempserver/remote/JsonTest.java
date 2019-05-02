package eu.alehem.tempserver.remote;

import com.google.gson.Gson;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests serialization and deserialization and comparison between sent/responses.
 * TODO: Rename this class as it evolves
 */
@Test
public class JsonTest {

    @Test
    public void testSerializationOfTemperatureMeasurement() {
        final String json = "{\"id\":1,\"measurement_time\":\"1556787821\",\"temp\":7.4}";
        TemperatureMeasurement meas = new TemperatureMeasurement(1, "1556787821", 7.4);
        Gson g = new Gson();
        String serialized = g.toJson(meas);
        Assert.assertEquals(serialized, json);
    }

    @Test
    public void testDeserializationOfTemperatureMeasurement() {
        final String json = "{\"id\":1,\"measurement_time\":\"1556787821\",\"temp\":7.4}";
        TemperatureMeasurement meas = new TemperatureMeasurement(1, "1556787821", 7.4);
        Gson g = new Gson();
        TemperatureMeasurement deserialized = g.fromJson(json, TemperatureMeasurement.class);
        Assert.assertEquals(deserialized, meas);
    }

    @Test
    public void testSerializationOfTemperaturePost() {
        final String json = "{\"remote_id\":1,\"remote_serial\":\"1234567\",\"temperatures\":[{\"id\":1,\"measurement_time\":\"1556788476\",\"temp\":7.4},{\"id\":2,\"measurement_time\":\"1556787821\",\"temp\":7.4}]}";
        List<TemperatureMeasurement> temperatureMeasurements = new ArrayList<>();
        temperatureMeasurements.add(new TemperatureMeasurement(1, "1556788476", 7.4));
        temperatureMeasurements.add(new TemperatureMeasurement(2, "1556787821", 7.4));

        TemperaturePost post = new TemperaturePost(1, "1234567", temperatureMeasurements);

        Gson g = new Gson();
        String serialized = g.toJson(post);
        Assert.assertEquals(serialized, json);
    }

    @Test
    public void testDeserializationOfTemperaturePost() {
        final String json = "{\"remote_id\":1,\"remote_serial\":\"1234567\",\"temperatures\":[{\"id\":1,\"measurement_time\":\"1556788476\",\"temp\":7.4},{\"id\":2,\"measurement_time\":\"1556787821\",\"temp\":7.4}]}";
        List<TemperatureMeasurement> temperatureMeasurements = new ArrayList<>();
        temperatureMeasurements.add(new TemperatureMeasurement(1, "1556788476", 7.4));
        temperatureMeasurements.add(new TemperatureMeasurement(2, "1556787821", 7.4));

        TemperaturePost post = new TemperaturePost(1, "1234567", temperatureMeasurements);
        Gson g = new Gson();
        TemperaturePost deserialized = g.fromJson(json, TemperaturePost.class);

        Assert.assertEquals(deserialized, post);
    }
}
