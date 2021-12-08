package io.bce.domain;

/**
 * This interface describes the event bus abstraction. The event bus is the component which
 * distributes domain events between different subdomains or publishes them outside (for example if
 * we are going notify another system about event inside our system).
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface EventBus {
  /**
   * Get event publisher for the specified type.
   *
   * @param <E>       The event type name
   * @param contextId The bounded context, which published the event
   * @param eventType The event type
   * @return The publisher
   */
  public <E> EventPublisher<E> getPublisher(BoundedContextId contextId, EventType<E> eventType);

  /**
   * Subscribe to the event.
   *
   * @param <E>           The event type name
   * @param contextId     The bounded context in which the event is published
   * @param eventType     The type of the event
   * @param eventListener The event listener
   * @return The event subscribtion object
   */
  public <E> EventSubscribtion subscribeOn(BoundedContextId contextId, EventType<E> eventType,
      EventListener<E> eventListener);

  /**
   * The event subscribtion.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface EventSubscribtion {
    /**
     * Unsubscribe.
     */
    public void unsubscribe();
  }

  /**
   * The event bus factory.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface Factory {
    /**
     * Create an event bus.
     *
     * @return The event bus
     */
    public EventBus createEventBus();
  }
}
