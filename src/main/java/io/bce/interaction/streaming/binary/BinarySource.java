package io.bce.interaction.streaming.binary;

import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk.BinaryChunkReader;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * This class is the binary chunks destination.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BinarySource implements Source<BinaryChunk> {
  @NonNull
  private final BinaryChunkReader reader;

  @Override
  public void read(DestinationConnection<BinaryChunk> connection) {
    BinaryChunk chunk = reader.readChunk();
    if (!chunk.isEmpty()) {
      submitChunk(connection, chunk);
    } else {
      connection.complete();
    }
  }

  private void submitChunk(DestinationConnection<BinaryChunk> connection, BinaryChunk chunk) {
    connection.submit(chunk, chunk.getSize());
  }
}
