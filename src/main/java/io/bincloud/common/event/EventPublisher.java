package io.bincloud.common.event;

public interface EventPublisher<E> {
	public void publish(E event);
}
