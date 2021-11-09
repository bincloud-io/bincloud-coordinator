package io.bce.domain.event;

import io.bce.FormatChecker;
import io.bce.Wrapped;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EventType implements Wrapped<String> {
	private static final String PATTERN = "^[a-zA-Z0-9\\-\\_\\.]+$";
	private static final FormatChecker EVENT_TYPE_FORMAT_CHECKER = FormatChecker.createFor(PATTERN,
			WrongEventTypeFormatException::new);

	private final String typeName;
	private final Class<?> eventType;

	public static final EventType createFor(@NonNull String typeName, @NonNull Class<?> eventType) {
		EVENT_TYPE_FORMAT_CHECKER.checkThatValueIsWellFormatted(typeName);
		return new EventType(typeName, eventType);
	}

	@Override
	public String extract() {
		return typeName;
	}

	@Override
	public String toString() {
		return String.format("%s[%s]", eventType, typeName);
	}

	
	public static final class WrongEventTypeFormatException extends RuntimeException {
		private static final long serialVersionUID = -1769763038225128075L;

		public WrongEventTypeFormatException(String value) {
			super(String.format("The event type \"%s\" has wrong gormat. It should be matched to the \"%s\" pattern.",
					value, PATTERN));
		}
	}
}
