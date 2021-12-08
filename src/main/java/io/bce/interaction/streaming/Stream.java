package io.bce.interaction.streaming;

import io.bce.promises.Promise;

/**
 * This interface describes the data stream abstraction. Data stream is the functional element,
 * transmits data from source to destination and notifies about successfully transferring completion
 * or transferring error.
 *
 * @author Dmitry Mikhaylenko
 *
 * @param <T> The data type stream
 */
public interface Stream<T> {
  /**
   * Include status observer.
   *
   * @param statusObserver The status observer
   * @return The stream instance
   */
  public Stream<T> observeStatus(StatusObserver statusObserver);

  /**
   * Start data streaming between source and destination.
   *
   * @return The promise resolving by transferred data count
   */
  public Promise<Stat> start();

  /**
   * This interface declares the contract for getting access to the transferring statistic.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface Stat {
    /**
     * Get transferred size.
     *
     * @return The transferred size
     */
    public Long getSize();
  }

  /**
   * This interface declares the signature of the observer function, allowing to get intermediate
   * streaming statistic.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface StatusObserver {
    /**
     * Listen streaming status change.
     *
     * @param statistic The current statistic
     */
    public void onStatusChange(Stat statistic);
  }
}
