package eu.alehem.tempserver.remote.core.measurementsuppliers;

import eu.alehem.tempserver.schema.proto.Tempserver;

import java.util.Set;
import java.util.function.Supplier;

/**
 * A supplier that gets measurements to save
 * @param <T> The type of measurement to get
 */
public interface MeasurementSupplier extends Supplier<Set<Tempserver.Measurement>> {}
