package io.bce.interaction.streaming

import io.bce.interaction.streaming.Source.DestinationConnection
import spock.lang.Specification

class RechargeableSourceSpec extends Specification {
  def "Scenario: read data from rechargeable source"() {
    given: "The rechargeable source"
    Source<Integer> firstSource = Mock(Source)
    firstSource.read(_) >> {DestinationConnection conn ->
      conn.submit(1, 1)
    } >> { DestinationConnection conn ->
      conn.complete()
    }

    Source<Integer> secondSource = Mock(Source)
    secondSource.read(_) >> {DestinationConnection conn ->
      conn.submit(2, 1)
    } >> { DestinationConnection conn ->
      conn.complete()
    }

    Queue<Source<Integer>> sourceQueue = new LinkedList([firstSource, secondSource])
    RechargeableSource source = new RechargeableSource(sourceQueue)

    and: "The destination connection"
    List<Integer> receiver = new ArrayList<>();
    DestinationConnection<Integer> connection = Mock(DestinationConnection)
    connection.submit(_, _) >> {data, size ->
      receiver.add(data);
      source.read(connection)
    }

    when: "The data are received from the rechargeable connection"
    source.read(connection)

    then: "The rechargeable connection should submit data from all data sources"
    receiver == [1, 2]

    and: "The rechargeable source should complete connection only once"
    1 * connection.complete() >> {source.release()}
  }
}
