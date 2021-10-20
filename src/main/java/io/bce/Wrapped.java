package io.bce;

/**
 * This interface declares the approach to get access to the original value
 * wrapped by the wrapper object
 * 
 * @author Dmitry Mikhaylenko
 *
 * @param <T> The wrapped object type
 */
public interface Wrapped<T> {
	/**
	 * Extract the wrapped value
	 * 
	 * @return The wrapped value
	 */
	public T extract();
}
