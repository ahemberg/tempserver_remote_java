package eu.alehem.tempserver.remote;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TempQueue {

    private List<Temperature> measurements = new ArrayList<>();
    private Set<Temperature> measurementSet = new HashSet<>(); //More memory efficient? Faster. No order. But timestamps make it unnecessary? Temperatures will return in random order to server though

    private TempQueue() {
    }

    private static class InstanceHolder {
        private static final TempQueue instance  = new TempQueue();
    }

    public static TempQueue getInstance() {
        return InstanceHolder.instance;
    }

    public void addTemperature(Temperature temperature) {
        measurements.add(temperature);
        measurementSet.add(temperature);
    }

    public void removeTemperature(Temperature temperature) {
        measurements.remove(temperature);
        measurementSet.remove(temperature);
    }

    public List<Temperature> getMeasurements() {
        return measurements;
    }

    public Set<Temperature> getMeasurementSet() {
        return measurementSet;
    }


}
