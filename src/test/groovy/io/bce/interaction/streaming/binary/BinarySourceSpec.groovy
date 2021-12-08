package io.bce.interaction.streaming.binary

import io.bce.interaction.streaming.Source.DestinationConnection
import io.bce.interaction.streaming.binary.BinaryChunk.BinaryChunkReader
import spock.lang.Specification

class BinarySourceSpec extends Specification {
  private static final BinaryChunk FIRST_CHUNK = new BinaryChunk((byte[]) [1, 2, 3])
  private static final BinaryChunk SECOND_CHUNK = new BinaryChunk((byte[]) [4, 5, 6])

  def "Scenario: read data"() {
    given: "The chunks source having 2 chunks"
    SimpleSource source = new SimpleSource([FIRST_CHUNK, SECOND_CHUNK])

    and: "The destination connection"
    DestinationConnection<BinaryChunk> connection = Mock(DestinationConnection) {
      complete() >> {
        source.release()
      }
    }

    when: "The read request is performed 3 times"
    source.read(connection)
    source.read(connection)
    source.read(connection)

    then: "The chunks should be submitted to the destination using connection"
    1 * connection.submit(FIRST_CHUNK, 3)
    1 * connection.submit(SECOND_CHUNK, 3)

    and: "The source should be released"
    source.isReleased() == true
  }

  static class SimpleSource extends BinarySource {
    private boolean released = false;

    public SimpleSource(Collection<BinaryChunk> chunks) {
      super(new ChunksReader(chunks));
    }

    public boolean isReleased() {
      return released;
    }

    @Override
    public void release() {
      this.released = true
    }

    private static class ChunksReader implements BinaryChunkReader {
      private Iterator<BinaryChunk> chunksIterator;

      public ChunksReader(Collection<BinaryChunk> chunks) {
        super();
        this.chunksIterator = chunks.iterator()
      }

      @Override
      public BinaryChunk readChunk() {
        if (chunksIterator.hasNext()) {
          return chunksIterator.next();
        }
        return BinaryChunk.EMPTY
      }
    }
  }
}
