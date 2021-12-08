package io.bce.timer

import io.bce.timer.Timeout
import io.bce.timer.TimeoutException
import spock.lang.Specification

class TimeoutExceptionSpec extends Specification {
  def "Scenario: create the timeout exception"() {
    given: "The timeout of the 1000 milliseconds"
    Timeout timeout = Timeout.ofMilliseconds(1000)

    expect: "The exception should be succesfully created"
    TimeoutException error = new TimeoutException(timeout)
    error.getMessage() == "The actor response waiting time is over. Timeout is Timeout(amount=1000, unit=Millis)."
  }
}
