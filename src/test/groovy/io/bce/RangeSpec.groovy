package io.bce

import io.bce.Range.ThresholdsAmountsException
import spock.lang.Specification

class RangeSpec extends Specification {
  private static final Long MIN = 0L;
  private static final Long MAX = 10L;

  def "Scenario: create range when max value less then min"() {
    when: "The range is created for case when min > max"
    Range.createFor(MAX, MIN)

    then: "The thresholds amounts exception should be thrown"
    thrown(ThresholdsAmountsException)
  }

  def "Scenario: check that the random poin contains into the range"() {
    given: "The range Min: ${MIN}, Max: ${MAX}"
    Range<Long> range = Range.createFor(MIN, MAX)

    expect: "The value ${value} is contained into the range: ${isContained}"
    range.contains(value) == isContained

    where:
    value    | isContained
    -10L     | false
    11L     | false
    0L      | true
    5L      | true
    10L     | true
  }
}
