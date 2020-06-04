package eu.alehem.tempserver.remote.core.measurementsuppliers;

import java.util.function.Supplier;

/**
 * A supplier that gets measurements to save
 * @param <T> The type of measurement to get
 */
public interface MeasurementSupplier<T> extends Supplier<T> {}
