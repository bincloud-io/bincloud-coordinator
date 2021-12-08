package io.bce.interaction.streaming.binary;

import io.bce.domain.errors.UnexpectedErrorException;
import io.bce.interaction.streaming.binary.BinaryChunk.BinaryChunkReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * This class implements the binary source, reading chunks from input stream.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class InputStreamSource extends BinarySource implements Closeable {
  private final Closeable streamCloser;

  public InputStreamSource(@NonNull InputStream inputStream, int bufferSize) {
    this(inputStream, Long.MAX_VALUE, bufferSize);
  }

  public InputStreamSource(@NonNull InputStream inputStream, long limit, int bufferSize) {
    super(new ChunkReader(inputStream, bufferSize, limit));
    this.streamCloser = inputStream;
  }

  @Override
  public void release() {
  }

  @Override
  public void close() throws IOException {
    streamCloser.close();
  }

  @AllArgsConstructor
  private static class ChunkReader implements BinaryChunkReader {
    private final InputStream inputStream;
    private final int bufferSize;
    private long limit;

    @Override
    public BinaryChunk readChunk() {
      try {
        int readCount;
        byte[] buffer = new byte[getEffectiveBufferSize()];
        if (buffer.length == 0 || (readCount = inputStream.read(buffer)) == -1) {
          return BinaryChunk.EMPTY;
        } else {
          BinaryChunk chunk = new BinaryChunk(Arrays.copyOfRange(buffer, 0, readCount));
          decreaseLimit(readCount);
          return chunk;
        }
      } catch (IOException error) {
        throw new UnexpectedErrorException(error);
      }
    }

    private int getEffectiveBufferSize() {
      if (limit < bufferSize) {
        return (int) limit;
      }
      return bufferSize;
    }

    private void decreaseLimit(long bytesCount) {
      this.limit -= bytesCount;
    }
  }
}
