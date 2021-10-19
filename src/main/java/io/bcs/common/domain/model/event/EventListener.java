package io.bcs.common.domain.model.event;

/**
 * This functional interface declares signature of 
 * domain event listener
 * 
 * @author Dmitry Mikhaylenko
 *
 * @param <E> The domain event type
 */
@FunctionalInterface
public interface EventListener<E> {
	public void onEvent(E event);
}
