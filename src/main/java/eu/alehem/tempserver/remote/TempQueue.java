package eu.alehem.tempserver.remote;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class TempQueue {

  private Set<Temperature> measurementSet = new HashSet<>();
  private boolean removeLock = false;

  private TempQueue() {}

  static TempQueue getInstance() {
    return InstanceHolder.instance;
  }

  synchronized void addTemperature(Temperature temperature) {
    measurementSet.add(temperature);
  }

  synchronized void addTemperatures(Set<Temperature> temperatures) {
    measurementSet.addAll(temperatures);
  }

  synchronized void removeTemperature(Temperature temperature) {
    if (removeLock) return;
    measurementSet.remove(temperature);
  }

  synchronized void removeTemperatures(Set<Temperature> temperatures) {
    if (removeLock) return;
    temperatures.forEach(measurementSet::remove);
  }

  synchronized int getQueueLen() {
    return measurementSet.size();
  }

  synchronized Temperature getOne() {
    return measurementSet.stream().findFirst().orElse(null);
  }

  synchronized Set<Temperature> getN(int n) {
    if (n > getQueueLen()) {
      n = getQueueLen();
    }
    return new HashSet<>(new ArrayList<>(measurementSet).subList(0, n));
  }

  synchronized void setRemoveLock(boolean locked) {
    this.removeLock = locked;
  }

  private static class InstanceHolder {
    private static final TempQueue instance = new TempQueue();
  }
}
