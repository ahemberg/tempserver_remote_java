package eu.alehem.tempserver.remote.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

  public synchronized void removeTemperaturesById(final Set<UUID> ids) {
    if (removeLock) return;
    final Set<Temperature> temperaturesToRemove =
        measurementSet.stream()
            .filter(m -> ids.stream().anyMatch(id -> id.equals(m.getId())))
            .collect(Collectors.toSet());
    removeTemperatures(temperaturesToRemove);
  }

  public synchronized int getQueueLen() {
    return measurementSet.size();
  }

  public synchronized Set<Temperature> getN(int n) {
    final int queueLength = getQueueLen();
    n = Math.min(n, queueLength);
    return new HashSet<>(new ArrayList<>(measurementSet).subList(0, n));
  }

  public synchronized void setRemoveLock(final boolean locked) {
    this.removeLock = locked;
  }

  private static class InstanceHolder {
    private static final TempQueue instance = new TempQueue();
  }
}
