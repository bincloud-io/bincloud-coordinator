package io.bce.domain.buses

import io.bce.domain.BoundedContextId
import io.bce.domain.EventBus
import io.bce.domain.EventBus.EventSubscribtion
import io.bce.domain.buses.PubSubEventBus.UnacceptableEventException
import io.bce.domain.EventListener
import io.bce.domain.EventType
import io.bce.interaction.pubsub.PubSub
import io.bce.interaction.pubsub.PubSub.Publisher
import io.bce.interaction.pubsub.PubSub.Subscriber
import io.bce.interaction.pubsub.PubSub.Subscribtion
import io.bce.interaction.pubsub.Topic
import spock.lang.Specification

class PubSubEventBusSpec extends Specification {
  private static final BoundedContextId CONTEXT = BoundedContextId.createFor("CONTEXT")
  private static final EventType EVENT_TYPE = EventType.createFor("EVENT", Long)
  private static final Topic TOPIC_NAME = Topic.ofName("CONTEXT__EVENT")
  private static final EventType WRONG_INITIALIZED_EVENT_TYPE = EventType.createFor("EVENT", String)

  def "Scenario: publish events to the event bus"() {
    given: "The pub-sub event channel"
    Subscriber channelSubscriber
    Subscribtion subscribtion = Mock(Subscribtion)
    PubSub channel = Mock(PubSub) {
      getPublisher(TOPIC_NAME) >> {
        return Mock(Publisher) {
          publish(_) >> {
            channelSubscriber.onMessage(it[0])
          }
        }
      }
      subscribeOn(TOPIC_NAME, _, _) >> {
        channelSubscriber = it[2]
        return subscribtion
      }
    }

    and: "The event bus, created over the channel"
    EventBus eventBus = PubSubEventBus.factory(channel).createEventBus()

    and: "The event listener"
    EventListener eventListener = Mock(EventListener)

    when: "The event listener is subscribed on the event"
    EventSubscribtion eventSubscribtion = eventBus.subscribeOn(CONTEXT, EVENT_TYPE, eventListener)

    and: "The event is published"
    eventBus.getPublisher(CONTEXT, EVENT_TYPE).publish(1L)

    and: "The subscriber is unsubscribed from the event"
    eventSubscribtion.unsubscribe()

    then: "The event should be passed to the event subscriber"
    1 * eventListener.onEvent(1L)

    and: "The pub-sub subscriber should be unsubscribed"
    1 * subscribtion.unsubscribe()
  }

  def "Scenario: publish wrong event type"() {
    given: "The pub-sub event channel"
    Subscriber channelSubscriber
    Subscribtion subscribtion = Mock(Subscribtion)
    PubSub channel = Mock(PubSub) {
      getPublisher(TOPIC_NAME) >> {
        return Mock(Publisher) {
          publish(_) >> {
            channelSubscriber.onMessage(it[0])
          }
        }
      }
      subscribeOn(TOPIC_NAME, _, _) >> {
        channelSubscriber = it[2]
        return subscribtion
      }
    }

    and: "The event bus, created over the channel"
    EventBus eventBus = PubSubEventBus.factory(channel).createEventBus()

    when: "The wrong event type is published"
    eventBus.getPublisher(CONTEXT, EVENT_TYPE).publish("HELLO")

    then: "The unacceptable event exception should be happened"
    thrown(UnacceptableEventException)
  }

  def "Scenario: receive wrong type"() {
    given: "The pub-sub event channel"
    Subscriber channelSubscriber
    Subscribtion subscribtion = Mock(Subscribtion)
    PubSub channel = Mock(PubSub) {
      getPublisher(TOPIC_NAME) >> {
        return Mock(Publisher) {
          publish(_) >> {
            channelSubscriber.onMessage(it[0])
          }
        }
      }
      subscribeOn(TOPIC_NAME, _, _) >> {
        channelSubscriber = it[2]
        return subscribtion
      }
    }

    and: "The event bus, created over the channel"
    EventBus eventBus = PubSubEventBus.factory(channel).createEventBus()

    and: "The event listener"
    EventListener eventListener = Mock(EventListener)

    when: "The event listener is subscribed on the event with incorrectly initialized event type(wrong specified event class)"
    EventSubscribtion eventSubscribtion = eventBus.subscribeOn(CONTEXT, WRONG_INITIALIZED_EVENT_TYPE, eventListener)

    and: "The event is published to the correct"
    eventBus.getPublisher(CONTEXT, EVENT_TYPE).publish(1L)

    and: "The subscriber is unsubscribed from the event"
    eventSubscribtion.unsubscribe()

    then: "The event should be passed to the event subscriber"
    0 * eventListener.onEvent(_)

    and: "The pub-sub subscriber should be unsubscribed"
    1 * subscribtion.unsubscribe()
  }
}
