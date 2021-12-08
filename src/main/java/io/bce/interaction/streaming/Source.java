package io.bce.interaction.streaming;

/**
 * This interface declares the contract for the streaming data sourcing.
 *
 * @author Dmitry Mikhaylenko
 *
 * @param <T> The transferring data type
 */
public interface Source<T> {
  /**
   * Read the data set.
   *
   * @param connection The destination connection
   */
  public void read(DestinationConnection<T> connection);

  /**
   * Complete streaming.
   */
  public void release();

  /**
   * This interface declares the contract for interaction between source and destination.
   *
   * @author Dmitry Mikhaylenko
   *
   * @param <T> The transferring data type
   */
  public interface DestinationConnection<T> {
    /**
     * Submit data to a destination.
     *
     * @param data The submitted data
     * @param size The data size
     */
    public void submit(T data, Integer size);

    /**
     * Complete the transferring process.
     */
    public void complete();
  }
}
