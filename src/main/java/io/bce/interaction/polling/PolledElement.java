package io.bce.interaction.polling;

/**
 * This interface declares the contract for access to polled element information.
 *
 * @author Dmitry Mikhaylenko
 *
 * @param <D> The polling data type name
 */
public interface PolledElement<D> {
  /**
   * Get element index.
   *
   * @return The index
   */
  public Long getIndex();

  /**
   * Get polled element data.
   *
   * @return The polled data
   */
  public D getData();

  /**
   * Calculate hash code for whole internal state.
   */
  @Override
  int hashCode();

  /**
   * Check the structural equivalence.
   */
  @Override
  boolean equals(Object obj);

  /**
   * Stringify index and data.
   */
  @Override
  String toString();
}
