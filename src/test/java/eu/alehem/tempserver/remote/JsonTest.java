package eu.alehem.tempserver.remote;

import com.google.gson.Gson;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests serialization and deserialization and comparison between sent/responses. TODO: Rename this
 * class as it evolves
 */
@Test
public class JsonTest {

  private static final String[] UUIDStrings = {
    "728c0433-faa4-498a-b9c5-0465dcfea88a",
    "2d7e7e46-18b8-4476-9ef3-6847ca6aee25",
    "a6c23cb5-1b70-4bd8-914f-df854c08e94d"
  };

  @Test
  public void testSerializationOfTemperatureMeasurement() {
    final String json = "{\"id\":1,\"measurement_time\":\"1556787821\",\"temp\":7.4}";
    TemperatureMeasurement meas =
        new TemperatureMeasurement(UUID.fromString(UUIDStrings[0]), Instant.ofEpochSecond(0), 7.4);
    Gson g = new Gson();
    String serialized = g.toJson(meas);
    Assert.assertEquals(serialized, json);
  }

  @Test
  public void testDeserializationOfTemperatureMeasurement() {
    final String json = "{\"id\":1,\"measurement_time\":\"1556787821\",\"temp\":7.4}";
    TemperatureMeasurement meas =
        new TemperatureMeasurement(UUID.fromString(UUIDStrings[0]), Instant.ofEpochSecond(0), 7.4);
    Gson g = new Gson();
    TemperatureMeasurement deserialized = g.fromJson(json, TemperatureMeasurement.class);
    Assert.assertEquals(deserialized, meas);
  }

  @Test
  public void testSerializationOfTemperaturePost() {
    final String json =
        "{\"remote_id\":1,\"remote_serial\":\"1234567\",\"temperatures\":[{\"id\":1,\"measurement_time\":\"1556788476\",\"temp\":7.4},{\"id\":2,\"measurement_time\":\"1556787821\",\"temp\":7.4}]}";
    List<TemperatureMeasurement> temperatureMeasurements = new ArrayList<>();
    temperatureMeasurements.add(
        new TemperatureMeasurement(UUID.fromString(UUIDStrings[0]), Instant.ofEpochSecond(0), 7.4));
    temperatureMeasurements.add(
        new TemperatureMeasurement(
            UUID.fromString(UUIDStrings[1]), Instant.ofEpochSecond(60), 7.4));

    TemperaturePost post =
        new TemperaturePost(UUID.fromString(UUIDStrings[2]), "1234567", temperatureMeasurements);

    Gson g = new Gson();
    String serialized = g.toJson(post);
    Assert.assertEquals(serialized, json);
  }

  @Test
  public void testDeserializationOfTemperaturePost() {
    final String json =
        "{\"remote_id\":1,\"remote_serial\":\"1234567\",\"temperatures\":[{\"id\":1,\"measurement_time\":\"1556788476\",\"temp\":7.4},{\"id\":2,\"measurement_time\":\"1556787821\",\"temp\":7.4}]}";
    List<TemperatureMeasurement> temperatureMeasurements = new ArrayList<>();
    temperatureMeasurements.add(
        new TemperatureMeasurement(UUID.fromString(UUIDStrings[0]), Instant.ofEpochSecond(0), 7.4));
    temperatureMeasurements.add(
        new TemperatureMeasurement(
            UUID.fromString(UUIDStrings[1]), Instant.ofEpochSecond(60), 7.4));

    TemperaturePost post =
        new TemperaturePost(UUID.fromString(UUIDStrings[2]), "1234567", temperatureMeasurements);
    Gson g = new Gson();
    TemperaturePost deserialized = g.fromJson(json, TemperaturePost.class);

    Assert.assertEquals(deserialized, post);
  }
}
