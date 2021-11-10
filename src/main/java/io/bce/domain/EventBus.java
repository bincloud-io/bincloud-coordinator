package io.bce.domain;

public interface EventBus {
	/**
	 * Get event publisher for the specified type
	 * 
	 * @param <E>       The event type name
	 * @param contextId The bounded context, which published the event
	 * @param eventType The event type
	 * @return The publisher
	 */
	public <E> EventPublisher<E> getPublisher(BoundedContextId contextId, EventType<E> eventType);

	/**
	 * Subscribe to the event
	 * 
	 * @param <E>           The event type name
	 * @param contextId     The bounded context in which the event is published
	 * @param eventType     The type of the event
	 * @param eventListener The event listener
	 * @return
	 */
	public <E> EventSubscribtion subscribeOn(BoundedContextId contextId, EventType<E> eventType,
			EventListener<E> eventListener);
	
	
	public interface EventSubscribtion {
		/**
		 * Unsubscribe
		 */
		public void unsubscribe();
	}

	public interface Factory {
		public EventBus createEventBus();
	}
}
