package io.bce.timer

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bce.timer.Timeout
import spock.lang.Specification

class TimeoutSpec extends Specification {
  private static final Long AMOUNT = 10L

  def "Scenario: create timeout of milliseconds"() {
    given: "The amount value is ${AMOUNT}"
    expect: "The timeout value should be initialized for ${AMOUNT} milliseconds"
    Timeout timeout = Timeout.ofMilliseconds(AMOUNT)
    timeout.getMilliseconds() ==  10L
  }

  def "Scenario: create timeout of seconds"() {
    given: "The amount value is ${AMOUNT}"
    expect: "The timeout value should be initialized for ${AMOUNT} seconds"
    Timeout timeout = Timeout.ofSeconds(AMOUNT)
    timeout.getMilliseconds() == 10L * 1000L
  }

  def "Scenario: create timeout of minutes"() {
    given: "The amount value is ${AMOUNT}"
    expect: "The timeout value should be initialized for ${AMOUNT} minutes"
    Timeout timeout = Timeout.ofMinutes(AMOUNT)
    timeout.getMilliseconds() ==  10L * 60L * 1000L
  }

  def "Scenario: create timeout of hours"() {
    given: "The amount value is ${AMOUNT}"
    expect: "The timeout value should be initialized for ${AMOUNT} hours"
    Timeout timeout = Timeout.ofHours(AMOUNT)
    timeout.getMilliseconds() == 10L * 60L * 60L * 1000L
  }

  def "Scenario: create timeout of days"() {
    given: "The amount value is ${AMOUNT}"
    expect: "The timeout value should be initialized for ${AMOUNT} days"
    Timeout timeout = Timeout.ofDays(AMOUNT)
    timeout.getMilliseconds() ==  10L * 24L * 60L * 60L * 1000L
  }
}
