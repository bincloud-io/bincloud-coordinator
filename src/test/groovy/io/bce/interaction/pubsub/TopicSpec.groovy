package io.bce.interaction.pubsub

import io.bce.interaction.pubsub.Topic.WrongTopicNameFormatException
import spock.lang.Specification

class TopicSpec extends Specification {
  private static final String CORRECT_TOPIC_NAME_STRING = "TOPIC-NAME.T_12345";
  private static final String WRONG_TOPIC_NAME_STRING = "TOPIC-NAME.T_1 2 3 4 5";

  def "Scenario: create topic from correct topic name format"() {
    when: "The topic is created from well formatted topic name string value"
    Topic urnAddress = Topic.ofName(CORRECT_TOPIC_NAME_STRING)

    then: "The topic should be correctly created"
    urnAddress.toString() == CORRECT_TOPIC_NAME_STRING
  }

  def "Scenario: create topic from wrong topic name format"() {
    when: "The topic is created from wrong formatted topic name string value"
    Topic.ofName(WRONG_TOPIC_NAME_STRING)

    then: "The wrong topic name format exception should be happened"
    thrown(WrongTopicNameFormatException)
  }
}
