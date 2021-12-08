package io.bce.interaction.streaming.binary

import io.bce.domain.errors.UnexpectedErrorException
import io.bce.interaction.streaming.Destination.SourceConnection
import io.bce.interaction.streaming.binary.BinaryChunk
import io.bce.interaction.streaming.binary.OutputStreamDestination
import spock.lang.Specification

class OutputStreamDestinationSpec extends Specification {
  private static final String TRANSFERRING_DATA = "Hello world!!!";

  def "Scenario: write to output stream source"() {
    byte[] buffer = TRANSFERRING_DATA.getBytes()
    given: "The output stream"
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

    and: "The output stream destination"
    OutputStreamDestination destination = new OutputStreamDestination(outputStream)

    and: "The source connection"
    SourceConnection sourceConnection = Mock(SourceConnection)

    when: "The binary chunk is written into the destination"
    destination.write(sourceConnection, new BinaryChunk(buffer), buffer.length)

    then: "The data should be written into output stream"
    new String(outputStream.toByteArray()) == TRANSFERRING_DATA

    and: "The next data portion should be requested"
    1 * sourceConnection.receive() >> {
      destination.release()
    }

    cleanup:
    destination.close()
  }

  def "Scenario: write to output stream source with error"() {
    byte[] buffer = TRANSFERRING_DATA.getBytes()
    given: "The wrong output stream"
    OutputStream outputStream = new OutputStream() {
          @Override
          public void write(int b) throws IOException {
            throw new IOException()
          }
        }

    and: "The output stream destination"
    OutputStreamDestination destination = new OutputStreamDestination(outputStream)

    and: "The source connection"
    SourceConnection sourceConnection = Mock(SourceConnection)

    when: "The binary chunk is written into the destination"
    destination.write(sourceConnection, new BinaryChunk(buffer), buffer.length)

    then: "The unexpected error exception should be happened"
    thrown(UnexpectedErrorException)
  }
}
