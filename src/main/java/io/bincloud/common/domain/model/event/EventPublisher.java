package io.bincloud.common.domain.model.event;

public interface EventPublisher<E> {
	public void publish(E event);
}
