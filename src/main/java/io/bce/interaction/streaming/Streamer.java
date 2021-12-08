package io.bce.interaction.streaming;

/**
 * This interface declares the contract for creating streams, transferring data from source
 * {@link Source} to destination {@link Destination}.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface Streamer {
  /**
   * Create stream, transferring data between source and destination.
   *
   * @param <T>         The transferred data type
   * @param source      The data streaming source
   * @param destination The data streaming destination
   * @return The stream transferred data between source and destination
   */
  public <T> Stream<T> createStream(Source<T> source, Destination<T> destination);
}
