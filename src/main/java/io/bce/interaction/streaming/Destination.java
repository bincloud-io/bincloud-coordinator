package io.bce.interaction.streaming;

/**
 * This interface declares the contract for the streaming data receiving.
 *
 * @author Dmitry Mikhaylenko
 *
 * @param <T> The streaming data type
 */
public interface Destination<T> {
  /**
   * Write the data set.
   *
   * @param connection The source connection
   * @param data       The data set
   * @param size       The size of written data
   */
  public void write(SourceConnection connection, T data, Integer size);

  /**
   * Complete streaming.
   */
  public void release();

  /**
   * This interface declares the contract for connecting to the source.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface SourceConnection {
    /**
     * Receive the next portion of data.
     */
    public void receive();
  }
}
