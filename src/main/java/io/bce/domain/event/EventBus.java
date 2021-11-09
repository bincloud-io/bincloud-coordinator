package io.bce.domain.event;

public interface EventBus {
	/**
	 * Get event publisher for the specified type
	 * 
	 * @param <E>       The event class name name
	 * @param eventType The event type
	 * @return The publisher
	 */
	public <E> EventPublisher<E> getPublisher(EventType eventType);

	
	
	public interface EventSubscribtion {
		/**
		 * Unsubscribe 
		 */
		public void unsubscribe();
	}
	
	/**
	 * This interface describes the contract of domain events listening
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 * @param <E> The domain event type
	 */
	public interface EventListener<E> {
		public void onEvent(E event);
	}

	
	public interface EventPublisher<E> {
		/**
		 * Publish the domain event
		 * 
		 * @param event The domain event
		 */
		public void publish(E event);
	}
}
