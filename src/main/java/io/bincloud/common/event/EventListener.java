package io.bincloud.common.event;

public interface EventListener<E> {
	public void onEvent(E event);
}
