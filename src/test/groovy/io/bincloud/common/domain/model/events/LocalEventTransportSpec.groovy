package io.bincloud.common.domain.model.events

import io.bincloud.common.domain.model.event.EventListener
import io.bincloud.common.domain.model.event.EventPublisher
import io.bincloud.common.domain.model.event.LocalEventTransport
import io.bincloud.common.domain.model.events.LocalEventTransportSpec.SecondEvent
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	To be possible use domain events locally (inside java application process), as a developer
	I am needed in a component which would provide local domain event transport 
	mechanism. 
""")
class LocalEventTransportSpec extends Specification {
	def "Scenario: transport domain events"() {
		given: "The two created global publishers for two different for local transport "
		EventPublisher<FirstEvent> firstEventPublisher = LocalEventTransport.createGlobalEventPublisher();
		EventPublisher<SecondEvent> secondEventPublisher = LocalEventTransport.createGlobalEventPublisher();
		
		and: "The registered event listeners for corresponding events into local context"
		EventListener<FirstEvent> firstEventListener = Mock(EventListener)
		EventListener<SecondEvent> secondEventListener = Mock(EventListener)
		EventListener<SecondEvent> thirdEventListener = Mock(EventListener)
		LocalEventTransport.registerLocalEventListener(FirstEvent.class, firstEventListener)
		LocalEventTransport.registerLocalEventListener(SecondEvent.class, secondEventListener)
		LocalEventTransport.registerLocalEventListener(SecondEvent.class, thirdEventListener)
		
		when: "The events has been published"
		firstEventPublisher.publish(new FirstEvent())
		secondEventPublisher.publish(new SecondEvent())
		
		then: "Each of these events should be transported to the corresponding listener"
		1 * firstEventListener.onEvent(_) >> {
			it[0] instanceof FirstEvent == true
		}
		
		1 * secondEventListener.onEvent(_) >> {
			it[0] instanceof SecondEvent == true
		}
		
		1 * thirdEventListener.onEvent(_) >> {
			it[0] instanceof SecondEvent == true
		}
	}
	
	private static class FirstEvent {
	}

	private static class SecondEvent {
	}
}
