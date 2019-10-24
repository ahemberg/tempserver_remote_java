package eu.alehem.tempserver.remote;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class TempQueueTest {

  @Test
  public void testAddIdenticalToQueue() {

    TempQueue queue = TempQueue.getInstance();

    final long now = Instant.now().getEpochSecond();
    Temperature first = new Temperature("1", 1.0, now);
    Temperature second = new Temperature("1", 1.0, now);

    queue.addTemperature(first);
    queue.addTemperature(second);

    Assert.assertEquals(queue.getQueueLen(), 1);
    queue.removeTemperature(first);
    Assert.assertEquals(queue.getQueueLen(), 0);
  }

  @Test
  public void testAddSimilarToQueue() {
    TempQueue queue = TempQueue.getInstance();

    final long now = Instant.now().getEpochSecond();

    Temperature first = new Temperature("1", 1.0, now);
    Temperature second = new Temperature("2", 1.0, now);

    queue.addTemperature(first);
    queue.addTemperature(second);

    Assert.assertEquals(queue.getQueueLen(), 2);
    queue.removeTemperature(first);
    queue.removeTemperature(second);
    Assert.assertEquals(queue.getQueueLen(), 0);
  }

  @Test
  public void testAddRemoveElementFromQueue() {
    TempQueue queue = TempQueue.getInstance();

    final long now = Instant.now().getEpochSecond();

    Temperature first = new Temperature("1", 1.0, now);

    queue.addTemperature(first);

    Assert.assertEquals(queue.getQueueLen(), 1);
    Temperature firstFromQueue = queue.getOne();
    queue.removeTemperature(firstFromQueue);
    Assert.assertEquals(firstFromQueue, first);
    Assert.assertSame(firstFromQueue, first);
    Assert.assertEquals(queue.getQueueLen(), 0);
  }

  @Test
  public void testRemoveElementsFromQueue() {
    TempQueue queue = TempQueue.getInstance();

    final long now = Instant.now().getEpochSecond();

    List<Temperature> temps = new ArrayList<>();
    temps.add(new Temperature("1", 2.0, now));
    temps.add(new Temperature("2", 2.0, now));
    temps.add(new Temperature("3", 2.0, now));
    temps.add(new Temperature("4", 2.0, now));
    temps.add(new Temperature("5", 2.0, now));
    temps.add(new Temperature("6", 2.0, now));
    temps.add(new Temperature("7", 2.0, now));
    temps.add(new Temperature("8", 2.0, now));
    temps.add(new Temperature("9", 2.0, now));
    temps.add(new Temperature("10", 2.0, now));

    Set<Temperature> addToS = new HashSet<>(temps);
    Set<Temperature> removeFromS = new HashSet<>(temps.subList(0, 5));

    queue.addTemperatures(addToS);
    queue.removeTemperatures(removeFromS);

    Assert.assertEquals(queue.getQueueLen(), 5);
    addToS.forEach(queue::removeTemperature);
    Assert.assertEquals(queue.getQueueLen(), 0);
  }

  @Test
  public void testGetN() {
    TempQueue queue = TempQueue.getInstance();

    final long now = Instant.now().getEpochSecond();

    Set<Temperature> temps = new HashSet<>();
    temps.add(new Temperature("1", 2.0, now));
    temps.add(new Temperature("2", 2.0, now));
    temps.add(new Temperature("3", 2.0, now));
    temps.add(new Temperature("4", 2.0, now));
    temps.add(new Temperature("5", 2.0, now));
    temps.add(new Temperature("6", 2.0, now));
    temps.add(new Temperature("7", 2.0, now));
    temps.add(new Temperature("8", 2.0, now));
    temps.add(new Temperature("9", 2.0, now));
    temps.add(new Temperature("10", 2.0, now));

    queue.addTemperatures(temps);

    Assert.assertEquals(1, queue.getN(1).size());
    Assert.assertEquals(2, queue.getN(2).size());
    Assert.assertEquals(3, queue.getN(3).size());
    Assert.assertEquals(4, queue.getN(4).size());
    Assert.assertEquals(5, queue.getN(5).size());
    Assert.assertEquals(6, queue.getN(6).size());
    Assert.assertEquals(7, queue.getN(7).size());
    Assert.assertEquals(8, queue.getN(8).size());
    Assert.assertEquals(9, queue.getN(9).size());
    Assert.assertEquals(10, queue.getN(10).size());
    Assert.assertEquals(0, queue.getN(0).size());
    Assert.assertEquals(10, queue.getN(20).size());

    temps.forEach(t -> queue.removeTemperature(t));
    Assert.assertEquals(0, queue.getQueueLen());
  }
}
