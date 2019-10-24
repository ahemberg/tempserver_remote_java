package eu.alehem.tempserver.remote;

import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Instant;

@Test
public class TemperatureTest {

  @Test
  public void testEquals() {
    final Instant now = Instant.now();
    UUID uuid = UUID.randomUUID();
    Temperature first = new Temperature(uuid, "1", 1.0, now);
    Temperature second = new Temperature(uuid, "1", 1.0, now);

    Assert.assertEquals(first, second);
    Assert.assertNotSame(first, second);
  }
}
