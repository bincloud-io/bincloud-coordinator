package io.bce.interaction.streaming.binary;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * This class is responsible for keeping chunk of binary data.
 *
 * @author The binary data chunk.
 *
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class BinaryChunk {
  public static final BinaryChunk EMPTY = new BinaryChunk(new byte[0]);

  private final byte[] body;

  /**
   * Get the binary chunk body.
   *
   * @return The binary chunk body
   */
  public byte[] getBody() {
    return body;
  }

  /**
   * Get the chunk size.
   *
   * @return The chunk size
   */
  public int getSize() {
    return body.length;
  }

  /**
   * Check that the binary chunk is empty.
   *
   * @return True is empty and false otherwise
   */
  public boolean isEmpty() {
    return getSize() == 0;
  }

  /**
   * This interface declares the contract for chunks receiving from somewhere.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface BinaryChunkReader {
    /**
     * Read data chunk.
     *
     * @return The data chunk
     */
    public BinaryChunk readChunk();
  }

  /**
   * This interface declares the contract for chunks writing to somewhere.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface BinaryChunkWriter {
    /**
     * Write data chunk.
     *
     * @param chunk The data chunk
     */
    public void writeChunk(BinaryChunk chunk);
  }
}
