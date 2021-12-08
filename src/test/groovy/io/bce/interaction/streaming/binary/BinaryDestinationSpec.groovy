package io.bce.interaction.streaming.binary

import io.bce.interaction.streaming.Destination.SourceConnection
import io.bce.interaction.streaming.binary.BinaryChunk.BinaryChunkWriter
import spock.lang.Specification

class BinaryDestinationSpec extends Specification {
  private static final BinaryChunk FIRST_CHUNK = new BinaryChunk((byte[]) [1, 2, 3])

  def "Scenario: write data"() {
    given: "The binary chunks writer"
    BinaryChunkWriter writer = Mock(BinaryChunkWriter)

    and: "The binary destination"
    SimpleDestination destination = new SimpleDestination(writer)

    and: "The source connection"
    SourceConnection connection = Mock(SourceConnection) {
      receive() >> {destination.release()}
    }

    when: "The data chunk is written"
    destination.write(connection, FIRST_CHUNK, 3)

    then: "The data should be written"
    1 * writer.writeChunk(FIRST_CHUNK)

    and: "The destination should be released"
    destination.isReleased() == true
  }

  static class SimpleDestination extends BinaryDestination {
    private boolean released = false;

    public SimpleDestination(BinaryChunkWriter writer) {
      super(writer);
    }

    @Override
    public void release() {
      this.released = true
    }

    public boolean isReleased() {
      return released
    }
  }
}
