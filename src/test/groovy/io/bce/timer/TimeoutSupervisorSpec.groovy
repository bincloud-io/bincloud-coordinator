package io.bce.timer

import io.bce.timer.Timeout
import io.bce.timer.TimeoutSupervisor
import io.bce.timer.TimeoutSupervisor.TimeoutCallback
import spock.lang.Specification

class TimeoutSupervisorSpec extends Specification {
  private TimeoutCallback timeoutListener;

  def setup() {
    this.timeoutListener = Mock(TimeoutCallback)
  }

  def "Scenario: complete before timeout exceeded"() {
    given: "The timeout of 100 milliseconds"
    Timeout timeout = Timeout.ofMilliseconds(100)

    and: "The timeout supervisor is initialized by the timeout and callback"
    TimeoutSupervisor supervisor = new TimeoutSupervisor(timeout, timeoutListener)

    when: "The timeout supervision is started"
    supervisor.startSupervision()

    and: "The timeout supervision is completed after 30 milliseconds"
    System.sleep(30)
    supervisor.stopSupervision()
    System.sleep(100)

    then: "The timeout listener shouldn't be notified"
    0 * timeoutListener.onTimeout()
  }

  def "Scenario: complete after timeout exceeded"() {
    given: "The timeout of 30 milliseconds"
    Timeout timeout = Timeout.ofMilliseconds(30)

    and: "The timeout supervisor is initialized by the timeout and callback"
    TimeoutSupervisor supervisor = new TimeoutSupervisor(timeout, timeoutListener)

    when: "The timeout supervision is started"
    supervisor.startSupervision()

    and: "The timeout supervision is completed after 100 milliseconds"
    System.sleep(100)
    supervisor.stopSupervision()

    then: "The timeout listener should be notified only once"
    1 * timeoutListener.onTimeout()
  }

  def "Scenario: timeout is exceeded without completion"() {
    given: "The timeout of 30 milliseconds"
    Timeout timeout = Timeout.ofMilliseconds(30)

    and: "The timeout supervisor is initialized by the timeout and callback"
    TimeoutSupervisor supervisor = new TimeoutSupervisor(timeout, timeoutListener)

    when: "The timeout supervision is started"
    supervisor.startSupervision()
    System.sleep(100)

    and: "The timeout supervision isn't completed"

    then: "The timeout listener should be notified only once"
    1 * timeoutListener.onTimeout()
  }
}
