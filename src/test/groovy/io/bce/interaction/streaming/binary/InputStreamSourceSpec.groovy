package io.bce.interaction.streaming.binary

import io.bce.domain.errors.UnexpectedErrorException
import io.bce.interaction.streaming.Source.DestinationConnection
import io.bce.interaction.streaming.binary.BinaryChunk
import io.bce.interaction.streaming.binary.InputStreamSource
import spock.lang.Specification

class InputStreamSourceSpec extends Specification {
  private static final String TRANSFERRING_DATA = "Hello world!!!";

  def "Scenario: read from input stream source"() {
    BinaryChunk chunk
    int submittedSize

    given: "The input stream"
    ByteArrayInputStream inputStream = new ByteArrayInputStream(TRANSFERRING_DATA.getBytes())

    and: "The input stream source"
    InputStreamSource inputStreamSource = new InputStreamSource(inputStream, 1000);

    and: "The destination connection"
    DestinationConnection<BinaryChunk> destinationConnection = Mock(DestinationConnection)

    when: "The data is received from source"
    inputStreamSource.read(destinationConnection)

    then: "The binary chunk should be received"
    1 * destinationConnection.submit(_, _) >> {
      chunk = it[0]
      submittedSize = it[1]
      inputStreamSource.read(destinationConnection)
    }

    and: "The transferring process should be completed"
    1 * destinationConnection.complete() >> {
      inputStreamSource.release()
    }

    and: "The expected data should be transferred"
    chunk.getSize() == submittedSize
    new String(chunk.getBody()) == TRANSFERRING_DATA

    cleanup:
    inputStreamSource.close()
  }

  def "Scenario: limited read from input stream source"() {
    BinaryChunk chunk
    int submittedSize

    given: "The input stream"
    ByteArrayInputStream inputStream = new ByteArrayInputStream(TRANSFERRING_DATA.getBytes())

    and: "The input stream source with limited length"
    InputStreamSource inputStreamSource = new InputStreamSource(inputStream, 5L, 1000);

    and: "The destination connection"
    DestinationConnection<BinaryChunk> destinationConnection = Mock(DestinationConnection)

    when: "The data is received from source"
    inputStreamSource.read(destinationConnection)

    then: "The binary chunk should be received"
    1 * destinationConnection.submit(_, _) >> {
      chunk = it[0]
      submittedSize = it[1]
      inputStreamSource.read(destinationConnection)
    }

    and: "The transferring process should be completed"
    1 * destinationConnection.complete() >> {
      inputStreamSource.release()
    }

    and: "The expected data should be transferred"
    chunk.getSize() == submittedSize
    new String(chunk.getBody()) == TRANSFERRING_DATA.substring(0, 5)

    cleanup:
    inputStreamSource.close()
  }

  def "Scenario: read from input stream source with error"() {
    given: "The wrong input stream"
    InputStream inputStream = new InputStream() {
          @Override
          public int read() throws IOException {
            throw new IOException()
          }
        }

    and: "The input stream source"
    InputStreamSource inputStreamSource = new InputStreamSource(inputStream, 1000);

    and: "The destination connection"
    DestinationConnection<BinaryChunk> destinationConnection = Mock(DestinationConnection)

    when: "The data is received from source"
    inputStreamSource.read(destinationConnection)

    then: "The unexpected error exception should be happened"
    thrown(UnexpectedErrorException)
  }
}
