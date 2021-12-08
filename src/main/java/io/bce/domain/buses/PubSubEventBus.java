package io.bce.domain.buses;

import io.bce.domain.BoundedContextId;
import io.bce.domain.EventBus;
import io.bce.domain.EventListener;
import io.bce.domain.EventPublisher;
import io.bce.domain.EventType;
import io.bce.interaction.pubsub.PubSub;
import io.bce.interaction.pubsub.PubSub.Subscriber;
import io.bce.interaction.pubsub.PubSub.Subscribtion;
import io.bce.interaction.pubsub.Topic;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * This class implements event bus over pub-sub interaction.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PubSubEventBus implements EventBus {
  private final PubSub<Object> pubSubChannel;

  public static final Factory factory(PubSub<Object> channel) {
    return () -> new PubSubEventBus(channel);
  }

  @Override
  public <E> EventPublisher<E> getPublisher(BoundedContextId contextId, EventType<E> eventType) {
    return new PubSubEventBusPublisher<>(contextId, eventType);
  }

  @Override
  public <E> EventSubscribtion subscribeOn(BoundedContextId contextId, EventType<E> eventType,
      EventListener<E> eventListener) {
    Topic topic = createTopic(contextId, eventType);
    Subscriber<E> eventSubscriber = new PubSubEventSubscriber<>(eventType, eventListener);
    Subscribtion subscribtion =
        pubSubChannel.subscribeOn(topic, eventType.getEventClass(), eventSubscriber);
    return new PubSubEventBusSubscribtion(subscribtion);
  }

  private <E> Topic createTopic(BoundedContextId contextId, EventType<E> eventType) {
    return Topic.ofName(String.format("%s__%s", contextId, eventType.extract()));
  }

  private final <E> void checkThatTheEventIsAcceptable(EventType<E> eventType, Object event) {
    if (!eventType.isAccepts(event)) {
      throw new UnacceptableEventException(event, eventType);
    }
  }

  @RequiredArgsConstructor
  private class PubSubEventBusPublisher<E> implements EventPublisher<E> {
    private final BoundedContextId contextId;
    private final EventType<E> eventType;

    @Override
    public void publish(E event) {
      checkThatTheEventIsAcceptable(eventType, event);
      pubSubChannel.getPublisher(createTopic(contextId, eventType)).publish(event);
    }
  }

  /**
   * This exception is happened if the event instance is not accepted to the event type.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public static final class UnacceptableEventException extends RuntimeException {
    private static final long serialVersionUID = -7206353691833222090L;

    public UnacceptableEventException(Object instance, EventType<?> eventType) {
      super(String.format("The event instance [%s] is not accepted for the [%s] acceptable type",
          instance.getClass(), eventType));
    }
  }

  @RequiredArgsConstructor
  private final class PubSubEventSubscriber<E> implements Subscriber<E> {
    private final EventType<E> eventType;
    private final EventListener<E> eventListener;

    @Override
    public void onMessage(E message) {
      if (eventType.isAccepts(message)) {
        eventListener.onEvent(message);
      }
    }
  }

  @RequiredArgsConstructor
  private final class PubSubEventBusSubscribtion implements EventSubscribtion {
    private final Subscribtion pubSubSubscribtion;

    @Override
    public void unsubscribe() {
      pubSubSubscribtion.unsubscribe();
    }
  }
}
