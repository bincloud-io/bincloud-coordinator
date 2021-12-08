package io.bce.actor

import spock.lang.Specification

class CorrelationKeySpec extends Specification {
  def "Scenario: wrap value"() {
    expect: "The correlation key value should be successfully wrapped"
    CorrelationKey correlationKey = CorrelationKey.wrap("INSTANCE_1.12556537123.11")
    correlationKey.toString() == "INSTANCE_1.12556537123.11"
  }

  def "Scenario: check that the key represent correlated value"() {
    expect: "The correlation key represents: ${valueType}"
    value.isRepresentCorrelated() == expectedResult

    where:
    value                           | expectedResult | valueType
    CorrelationKey.UNCORRELATED     | false          | "Uncorellated value"
    CorrelationKey.wrap("")         | false          | "Uncorrelated value"
    CorrelationKey.wrap("12345")    | true           | "Correlated value"
  }
}
