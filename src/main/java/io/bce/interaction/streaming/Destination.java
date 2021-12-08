package io.bce.interaction.streaming;

public interface Destination<T> {
  /**
   * Write the data set
   * 
   * @param connection The source connection
   * @param data The data set
   * @param size The size of written data
   */
  public void write(SourceConnection connection, T data, Integer size);

  /**
   * Complete streaming
   */
  public void release();

  public interface SourceConnection {
    public void receive();
  }
}
