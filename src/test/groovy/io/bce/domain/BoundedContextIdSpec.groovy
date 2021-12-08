package io.bce.domain

import io.bce.domain.BoundedContextId
import io.bce.domain.BoundedContextId.WrongBoundedContextIdFormatException
import spock.lang.Specification

class BoundedContextIdSpec extends Specification {
  private static final String CONTEXT_ID_STRING = "CONTEXT"
  private static final String PLATFORM_CONTEXT_ID = "PLATFORM"
  private static final String BAD_FORMATTED_NAME = "^&^&%^%:::&*&*)*(_*()"

  def "Scenario: create from well-formatted context name"() {
    expect: "The bounded context identifier name should be successfully wrapped"
    BoundedContextId contextId = BoundedContextId.createFor(CONTEXT_ID_STRING)
    contextId.toString() == CONTEXT_ID_STRING
  }

  def "Scenario: platform bounded context id"() {
    expect: "The global bounded context should be named as the \"${PLATFORM_CONTEXT_ID}\""
    BoundedContextId.PLATFORM_CONTEXT.toString() == PLATFORM_CONTEXT_ID
  }

  def "Scenario: create from bad-formatted or reserved context name"() {
    when: "The bounded context id is created from reserved name"
    BoundedContextId.createFor(wrongName)

    then: "The wrong bounded context id format exception should be happened"
    thrown(WrongBoundedContextIdFormatException)

    where:
    wrongName << [
      PLATFORM_CONTEXT_ID,
      BAD_FORMATTED_NAME
    ]
  }
}
