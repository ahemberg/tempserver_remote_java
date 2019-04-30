package eu.alehem.tempserver.remote;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TempQueue {

    private Set<Temperature> measurementSet = new HashSet<>();

    private TempQueue() {
    }

    private static class InstanceHolder {
        private static final TempQueue instance  = new TempQueue();
    }

    public static TempQueue getInstance() {
        return InstanceHolder.instance;
    }

    synchronized public void addTemperature(Temperature temperature) {
        measurementSet.add(temperature);
    }

    synchronized public void addTemperatures(Set<Temperature> temperatures) {
        measurementSet.addAll(temperatures);
    }

    synchronized public void removeTemperature(Temperature temperature) {
        measurementSet.remove(temperature);
    }

    synchronized public void removeTemperatures(Set<Temperature> temperatures) {
        measurementSet.remove(temperatures);
    }

    synchronized public Set<Temperature> getMeasurementSet() {
        return measurementSet;
    }

    synchronized public int getQueueLen() {
        return measurementSet.size();
    }

    synchronized public Temperature getOne() {
        return measurementSet.stream().findFirst().orElse(null);
    }

    synchronized public Set<Temperature> getN(int n) {
        if (n > getQueueLen()) {
            n = getQueueLen();
        }
        return new HashSet<>(new ArrayList<>(measurementSet).subList(0,n));
    }
}
