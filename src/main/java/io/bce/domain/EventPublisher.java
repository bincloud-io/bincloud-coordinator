package io.bce.domain;

/**
 * This interface declares the contract for event publishing.
 * 
 * @author Dmitry Mikhaylenko
 *
 * @param <E>
 */
public interface EventPublisher<E> {
	/**
	 * Publish the domain event
	 * 
	 * @param event The domain event
	 */
	public void publish(E event);
}