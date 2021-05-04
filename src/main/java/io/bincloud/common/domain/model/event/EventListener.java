package io.bincloud.common.domain.model.event;

public interface EventListener<E> {
	public void onEvent(E event);
}
