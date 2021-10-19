package io.bcs.common.domain.model.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.experimental.UtilityClass;

/**
 * This class provides the functional, allowing transfer domain events
 * during local module. These domain events mustn't violate local module
 * bindings. 
 * 
 * @author Dmitry Mikhaylenko
 *
 */
@UtilityClass
public class LocalEventTransport {
	private final Map<Class<?>, List<EventListener<Object>>> LOCAL_EVENT_LISTENERS = new ConcurrentHashMap<Class<?>, List<EventListener<Object>>>();

	public final <E> EventPublisher<E> createGlobalEventPublisher() {
		return (E event) -> {
			Class<?> eventType = event.getClass();
			List<EventListener<Object>> listeners = LOCAL_EVENT_LISTENERS.getOrDefault(eventType,
					Collections.emptyList());
			listeners.forEach(listener -> listener.onEvent(event));
		};
	}

	@SuppressWarnings("unchecked")
	public final <E> void registerLocalEventListener(Class<E> eventType, EventListener<E> listener) {
		registerEventListenersBucketIfMissing(eventType);
		LOCAL_EVENT_LISTENERS.get(eventType).add((EventListener<Object>) listener);
	}

	private final void registerEventListenersBucketIfMissing(Class<?> eventType) {
		if (!LOCAL_EVENT_LISTENERS.containsKey(eventType)) {
			LOCAL_EVENT_LISTENERS.put(eventType, new ArrayList<EventListener<Object>>());
		}
	}
}
