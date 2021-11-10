package io.bce.domain;

/**
 * This interface describes the contract of element, which receives asynchronous
 * domain events.
 * 
 * @author Dmitry Mikhaylenko
 *
 * @param <E> The domain event type
 */
public interface EventListener<E> {
	/**
	 * Receive the published event
	 * 
	 * @param event The 
	 */
	public void onEvent(E event);
}