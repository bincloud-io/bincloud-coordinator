package io.bce.interaction.streaming;

public interface Streamer {
  /**
   * Create stream, transferring data between source and destination
   * 
   * @param <T> The transferred data type
   * @param source The data streaming source
   * @param destination The data streaming destination
   * @return The stream transferred data between source and destination
   */
  public <T> Stream<T> createStream(Source<T> source, Destination<T> destination);
}
