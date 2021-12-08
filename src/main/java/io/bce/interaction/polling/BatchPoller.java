package io.bce.interaction.polling;

import java.util.Collection;

/**
 * This interface describes the contract for piece of data polling mechanism.
 *
 * @author Dmitry Mikhaylenko
 *
 * @param <D> The polling data type name
 */
public interface BatchPoller<D> {
  /**
   * Poll piece of data.
   *
   * @return The polled piece of data
   */
  public Collection<D> poll();
}
