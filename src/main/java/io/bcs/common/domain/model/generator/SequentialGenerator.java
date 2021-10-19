package io.bcs.common.domain.model.generator;

/**
 * This interface declares the contract for value generation based on sequence.
 * For example it might be auto-increment or UUID generation or sequence based on
 * external source. There is strict constraint for generated value - it must be
 * unique value.
 * 
 * @author Dmitry Mikhaylenko
 *
 * @param <V> The generated sequential value type
 */
public interface SequentialGenerator<V> {
	/**
	 * Get next sequential value
	 * 
	 * @return The generated value
	 */
	public V nextValue();
}
