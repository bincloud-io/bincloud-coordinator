package io.bce.interaction.pubsub

import io.bce.actor.ActorName
import io.bce.actor.ActorSystem
import io.bce.actor.EventLoop.Dispatcher
import io.bce.interaction.pubsub.PubSub.Publisher
import io.bce.interaction.pubsub.PubSub.Subscriber
import io.bce.interaction.pubsub.PubSub.Subscribtion
import spock.lang.Specification

class LocalPubSubSpec extends Specification {
		private static final ActorName COORDINATOR_NAME = ActorName.wrap("PUB_SUB_COORDINATOR")
	private static final Topic TOPIC_1 = Topic.ofName("TOPIC.1")
	private static final Topic TOPIC_2 = Topic.ofName("TOPIC.2")
	private static final String MESSAGE_1 = "TEST_MESSAGE"
	private static final Long MESSAGE_2 = -12345L
	
	private Subscriber<String> firstSubscriber
	private Subscriber<String> secondSubscriber
	private Subscriber<Long> thirdSubscriber
	
	def setup() {
		this.firstSubscriber = Mock(Subscriber)
		this.secondSubscriber = Mock(Subscriber)
		this.thirdSubscriber = Mock(Subscriber)		
	}
	
	def "Scenario: publish events to topics and consume by subscribers"() {
		given: "The pub-sub channel"
		PubSub<Object> pubSubChannel = new LocalPubSub()
		
		when: "The first and second subscribers are subscribed to the ${TOPIC_1} for string message type"
		Subscribtion firstSubscribtion = pubSubChannel.subscribeOn(TOPIC_1, String, firstSubscriber)
		Subscribtion secondSubscribtion = pubSubChannel.subscribeOn(TOPIC_1, String, secondSubscriber)
		
		and: "The third subscriber is subscribed to the ${TOPIC_2} for long message type"
		Subscribtion thirdSubscribtion = pubSubChannel.subscribeOn(TOPIC_2, Long, thirdSubscriber)
		
		and: "The publisher is created for ${TOPIC_1} and ${TOPIC_2} topics"
		Publisher<Object> publisherTopic1 = pubSubChannel.getPublisher(TOPIC_1)
		Publisher<Object> publisherTopic2 = pubSubChannel.getPublisher(TOPIC_2)
		
		and: "The message ${MESSAGE_1} is published to the ${TOPIC_1}"
		publisherTopic1.publish(MESSAGE_1)
		
		and: "The message ${MESSAGE_2} is published to the ${TOPIC_2}"
		publisherTopic2.publish(MESSAGE_2)
		
		and: "The subscribers is unsubscribed from their subscribtions"
		firstSubscribtion.unsubscribe()
		secondSubscribtion.unsubscribe()
		thirdSubscribtion.unsubscribe()
		
		and: "The pub-sub channel is closed"
		pubSubChannel.close()
		
		then: "The message ${MESSAGE_1} should be received by the first and second consumers"
		1 * firstSubscriber.onMessage(MESSAGE_1)
		1 * secondSubscriber.onMessage(MESSAGE_1)
		
		and: "The message ${MESSAGE_2} should be received by the third consumers"
		1 * thirdSubscriber.onMessage(MESSAGE_2) 
	}
}
