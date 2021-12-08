package io.bce.interaction.polling

import java.util.stream.Collectors
import java.util.stream.Stream

import io.bce.interaction.polling.BatchPoller
import io.bce.interaction.polling.PolledElement
import io.bce.interaction.polling.Polling
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	To make batch polling more comfortable, as a developer I'm needed in a component which will 
	translate polling data parts, returned by special function, to the data stream.  
""")
class PollingSpec extends Specification {
  def "Scenario: poll numerated data sequentially"() {
    given: "The data poller"
    BatchPoller<Long> poller = Stub(BatchPoller)
    poller.poll() >> [1, 2, 3, 4, 5] >> [6, 7, 8] >> []

    when: "The sequential data polling has been started"
    Stream<PolledElement<Long>> pollingStream = Polling.sequentialNumeratedPolling(poller);

    and: "The stream data has been collected to a collection"
    Collection<PolledElement<Long>> pollingResult = pollingStream.collect(Collectors.toList())

    then: "The result data collection sould be ordered the same as the input data"
    pollingResult == [
      new PollingDataHolder<Long>(0, 1),
      new PollingDataHolder<Long>(1, 2),
      new PollingDataHolder<Long>(2, 3),
      new PollingDataHolder<Long>(3, 4),
      new PollingDataHolder<Long>(4, 5),
      new PollingDataHolder<Long>(5, 6),
      new PollingDataHolder<Long>(6, 7),
      new PollingDataHolder<Long>(7, 8)
    ]
  }

  def "Scenario: poll numerated data parallelly"() {
    given: "The data poller"
    BatchPoller<Long> poller = Stub(BatchPoller)
    poller.poll() >> [1, 2, 3, 4, 5] >> [6, 7, 8] >> []

    when: "The parallel data polling has been started"
    Stream<Long> pollingStream = Polling.parallelNumeratedPolling(poller);

    and: "The stream data has been collected to a collection"
    Collection<Long> pollingResult = pollingStream.collect(Collectors.toList())

    then: "The result data collection sould be ordered the same as the input data"
    pollingResult == [
      new PollingDataHolder<Long>(0, 1),
      new PollingDataHolder<Long>(1, 2),
      new PollingDataHolder<Long>(2, 3),
      new PollingDataHolder<Long>(3, 4),
      new PollingDataHolder<Long>(4, 5),
      new PollingDataHolder<Long>(5, 6),
      new PollingDataHolder<Long>(6, 7),
      new PollingDataHolder<Long>(7, 8)
    ]
  }

  def "Scenario: poll data sequentially"() {
    given: "The data poller"
    BatchPoller<Long> poller = Stub(BatchPoller)
    poller.poll() >> [1, 2, 3, 4, 5] >> [6, 7, 8] >> []

    when: "The sequential data polling has been started"
    Stream<PolledElement<Long>> pollingStream = Polling.sequentialPolling(poller);

    and: "The stream data has been collected to a collection"
    Collection<PolledElement<Long>> pollingResult = pollingStream.collect(Collectors.toList())

    then: "The result data collection sould be ordered the same as the input data"
    pollingResult == [1, 2, 3, 4, 5, 6, 7, 8]
  }

  def "Scenario: poll data parallelly"() {
    given: "The data poller"
    BatchPoller<Long> poller = Stub(BatchPoller)
    poller.poll() >> [1, 2, 3, 4, 5] >> [6, 7, 8] >> []

    when: "The parallel data polling has been started"
    Stream<Long> pollingStream = Polling.parallelPolling(poller);

    and: "The stream data has been collected to a collection"
    Collection<Long> pollingResult = pollingStream.collect(Collectors.toList())

    then: "The result data collection sould be ordered the same as the input data"
    pollingResult == [1, 2, 3, 4, 5, 6, 7, 8]
  }
}
