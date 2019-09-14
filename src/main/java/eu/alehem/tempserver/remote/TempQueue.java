package eu.alehem.tempserver.remote;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TempQueue {

  private Set<Temperature> measurementSet = new HashSet<>();
  private boolean removeLock = false;

  private TempQueue() {}

  private static class InstanceHolder {
    private static final TempQueue instance = new TempQueue();
  }

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

  public synchronized Set<Temperature> getMeasurementSet() {
    return measurementSet;
  }

  public synchronized int getQueueLen() {
    return measurementSet.size();
  }

  public synchronized Temperature getOne() {
    return measurementSet.stream().findFirst().orElse(null);
  }

  public synchronized Set<Temperature> getN(int n) {
    if (n > getQueueLen()) {
      n = getQueueLen();
    }
    return new HashSet<>(new ArrayList<>(measurementSet).subList(0, n));
  }

  public synchronized void setRemoveLock(boolean locked) {
    this.removeLock = locked;
  }
}
