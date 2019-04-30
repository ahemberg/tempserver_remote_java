package eu.alehem.tempserver.remote;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Instant;

@Test
public class TemperatureTest {

    @Test
    public void testEquals() {
        Instant now = Instant.now();
        Temperature first = new Temperature("1", 1.0, now);
        Temperature second = new Temperature("1", 1.0, now);

        Assert.assertEquals(first, second);
        Assert.assertNotSame(first, second);
    }
}
