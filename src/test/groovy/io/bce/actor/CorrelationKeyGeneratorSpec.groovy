package io.bce.actor

import io.bce.Generator
import spock.lang.Specification

class CorrelationKeyGeneratorSpec extends Specification {
  private static final String GLOBAL_INSTANCE_ID = "GLOBAL"
  private static final String KEY_PATTERN = "GLOBAL:[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}"

  def "Scenario: generate key"() {
    given: "The key generator for instance with name ${GLOBAL_INSTANCE_ID}"
    Generator<CorrelationKey> generator = new CorrelationKeyGenerator(GLOBAL_INSTANCE_ID)

    expect: "The generated key should be matched to the pattern: ${KEY_PATTERN}"
    generator.generateNext().toString().matches(KEY_PATTERN) == true
  }
}
