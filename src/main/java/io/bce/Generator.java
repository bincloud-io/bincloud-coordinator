package io.bce;

/**
 * This interface declares the contract for components, having the value generation responsibility.
 *
 * @author Dmitry Mikhaylenko
 *
 * @param <V> The generated value type
 */
public interface Generator<V> {
  /**
   * Generate next value.
   *
   * @return The generated value
   */
  public V generateNext();
}
