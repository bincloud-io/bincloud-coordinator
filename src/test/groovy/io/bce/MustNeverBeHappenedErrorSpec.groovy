package io.bce

import io.bce.MustNeverBeHappenedError
import spock.lang.Specification

class MustNeverBeHappenedErrorSpec extends Specification {
  def "Scenario: initialize error by message text"() {
    expect: "The error object initialized by message text should have message text equivalent to the initial text"
    MustNeverBeHappenedError error = new MustNeverBeHappenedError("MESSAGE_TEXT")
    error.getMessage() == "MESSAGE_TEXT"

    and: "The error object shouldn't have cause error"
    error.getCause() == null
  }

  def "Scenario: initize error by throwable object"() {
    given: "The throwable object"
    Throwable initialCause = new Throwable("ERROR_TEXT")
    expect: "The error object initialized by error text should have description containing init cause type name"
    MustNeverBeHappenedError error = new MustNeverBeHappenedError(initialCause)
    error.getMessage() == "Error class java.lang.Throwable must never be happened for this case."

    and: "The throwable initial object should be initialized as the error cause"
    error.getCause().is(initialCause)
  }
}
