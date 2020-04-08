package eu.alehem.tempserver.remote.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TempQueue {

  private Set<Temperature> measurementSet = new HashSet<>();
  private boolean removeLock = false;

  private TempQueue() {}

  public static TempQueue getInstance() {
    return InstanceHolder.instance;
  }

  public synchronized void addTemperature(Temperature temperature) {
    measurementSet.add(temperature);
  }

  public synchronized void addTemperatures(Set<Temperature> temperatures) {
    measurementSet.addAll(temperatures);
  }

  public synchronized void removeTemperature(Temperature temperature) {
    if (removeLock) return;
    measurementSet.remove(temperature);
  }

  public synchronized void removeTemperatures(Set<Temperature> temperatures) {
    if (removeLock) return;
    temperatures.forEach(measurementSet::remove);
  }

  public synchronized int getQueueLen() {
    return measurementSet.size();
  }

  public synchronized Set<Temperature> getN(int n) {
    final int queueLength = getQueueLen();
    n = Math.min(n, queueLength);
    return new HashSet<>(new ArrayList<>(measurementSet).subList(0, n));
  }

  public synchronized void setRemoveLock(boolean locked) {
    this.removeLock = locked;
  }

  private static class InstanceHolder {
    private static final TempQueue instance = new TempQueue();
  }
}
