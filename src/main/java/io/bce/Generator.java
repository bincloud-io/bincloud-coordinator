package io.bce;

public interface Generator<V> {
	/**
	 * Generate next value
	 * 
	 * @return The generated value
	 */
	public V generateNext();
}
