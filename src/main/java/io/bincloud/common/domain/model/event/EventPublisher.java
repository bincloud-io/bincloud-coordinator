package io.bincloud.common.domain.model.event;

/**
 * This functional interface declares the contract of domain event publishing
 * responsibility
 * 
 * @author Dmitry Mikhaylenko
 *
 * @param <E> The domain event type
 */
@FunctionalInterface
public interface EventPublisher<E> {
	public void publish(E event);
}
