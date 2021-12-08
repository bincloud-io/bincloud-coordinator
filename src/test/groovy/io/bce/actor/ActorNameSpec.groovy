package io.bce.actor

import spock.lang.Specification

class ActorNameSpec extends Specification {
  def "Scenario: initialize the actor name"() {
    expect: "The actor name should be successfully wrapped"
    ActorName.wrap("SOME_ACTOR").toString() == "SOME_ACTOR"
  }

  def "Scenario: derive the actor name"() {
    given: "The base actor name with name \"BASE\""
    ActorName baseActorName = ActorName.wrap("BASE")

    and: "The derived actor local name with name \"DERIVED\""
    ActorName targetActorName = ActorName.wrap("DERIVED")

    when: "The base actor derives the actor name with the received local derived actor name"
    ActorName derivedActorFullName = baseActorName.deriveWith(targetActorName)

    then: "The total derived actor full name should be \"BASE.DERIVED\""
    derivedActorFullName.toString() == "BASE.DERIVED"
  }
}
