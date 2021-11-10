package io.bce.domain

import io.bce.domain.EventType.WrongEventTypeFormatException
import spock.lang.Specification

class EventTypeSpec extends Specification {
	private static final String EVENT_TYPE_NAME = "EVENT_TYPE";
	
	def "Scenario: create the event type"() {
		given: "The event type"
		EventType eventType = EventType.createFor(EVENT_TYPE_NAME)
		
		expect: "The event type name should be allowable for extracting"
		eventType.extract() == EVENT_TYPE_NAME
		
		and: "The stringified event type should contain type name and class name"
		eventType.toString() == "${EVENT_TYPE_NAME}[java.lang.Object]"
	}
	
	def "Scenario: create event type for wrong event type name"() {
		when: "The event type is created for bad-formatted event type"
		EventType.createFor("Bad formatted event type!!!!!!!!")
		
		then: "The wrong event type format exception should be happened"
		thrown(WrongEventTypeFormatException)
	}
	
	def "Scenario: check that the event instance is acceptable for the type"() {
		given: "The event type"
		EventType eventType = EventType.createFor(EVENT_TYPE_NAME, eventClass)
		
		expect: "The event instance is allowable for the event type: ${isAcceptable}"
		eventType.isAccepts(eventInstance) == isAcceptable
				
		where: 
		eventClass        | eventInstance   | isAcceptable
		Long.class       | new Object()    | false     
	}
}
