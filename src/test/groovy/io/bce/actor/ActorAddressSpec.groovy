package io.bce.actor

import io.bce.actor.ActorAddress.WrongActorAddressFormatException
import spock.lang.Specification

class ActorAddressSpec extends Specification {
  def "Scenario: create the actor address from the actor name"() {
    given: "The actor name \"SOME.ACTOR\""
    ActorName actorName = ActorName.wrap("SOME.ACTOR")

    expect: "The actor address should be successfully created from actor name as the URN address \"urn:actor:SOME.ACTOR\""
    ActorAddress actorAddress = ActorAddress.ofName(actorName)
    actorAddress.toString() == "urn:actor:SOME.ACTOR"
  }

  def "Scenario: create the actor addres from the URN address"() {
    given: "The actor created from URN address\"urn:address:SOME_ACTOR\""
    ActorAddress actorAddress = ActorAddress.ofUrn("urn:actor:SOME_ACTOR")

    expect: "The actor should be correctly initialized without error and URN transformations"
    actorAddress.toString() == "urn:actor:SOME_ACTOR"
  }

  def "Scenario: create the actor address from incorrect URN format"() {
    when: "The actor address is initialized by the wrong URN address syntax"
    ActorAddress.ofUrn("WRONG ADDRESS")

    then: "The wrong address format exception should be happened"
    WrongActorAddressFormatException error = thrown(WrongActorAddressFormatException)
    error.getMessage() == "The actor URN \"WRONG ADDRESS\" isn't matched to the \"urn:actor:(.+)\" pattern"
  }

  def "Scenario: extract the actor name"() {
    given: "The actor with URN address \"urn:actor:SOME_ACTOR\""
    ActorAddress actorAddress = ActorAddress.ofUrn("urn:actor:SOME_ACTOR")

    expect: "The actor name should be \"SOME_ACTOR\""
    actorAddress.getActorName() == ActorName.wrap("SOME_ACTOR")
  }

  def "Scenario: unknown actor address"() {
    expect: "The unknown actor address URN should be \"urn:actor:SYSTEM.DEAD_LETTER\""
    ActorAddress.UNKNOWN_ADDRESS.toString() == "urn:actor:SYSTEM.DEAD_LETTER"
  }
}
