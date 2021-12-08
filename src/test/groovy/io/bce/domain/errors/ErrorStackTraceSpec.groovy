package io.bce.domain.errors

import io.bce.domain.errors.ErrorStackTrace
import spock.lang.Specification

class ErrorStackTraceSpec extends Specification {
  def "Scenario: stringify stack trace"() {
    given: "The random throwable object"
    Throwable throwableObject = new Throwable("RANDOM THROWABLE")

    and: "The error stack trace initialized by the throwable object"
    ErrorStackTrace errorStace = new ErrorStackTrace(throwableObject)

    expect: "The error stack trace will successfully stringified"
    errorStace.toString() == stringifyStackTrace(throwableObject)
  }

  private String stringifyStackTrace(Throwable error) {
    StringWriter writer = new StringWriter()
    error.printStackTrace(new PrintWriter(writer))
    return writer.toString()
  }
}
