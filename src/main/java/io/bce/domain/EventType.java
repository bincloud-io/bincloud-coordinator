package io.bce.domain;

import io.bce.FormatChecker;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * This class is a value, identifying the event type.
 *
 * @author Dmitry Mikhaylenko
 *
 * @param <E> The event type name
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EventType<E> {
  private static final String PATTERN = "^[a-zA-Z0-9\\-\\_\\.]+$";
  private static final FormatChecker EVENT_TYPE_FORMAT_CHECKER =
      FormatChecker.createFor(PATTERN, WrongEventTypeFormatException::new);

  @EqualsAndHashCode.Include
  private final String typeName;
  private final Class<E> eventType;

  public static final EventType<Object> createFor(@NotNull String typeName) {
    return createFor(typeName, Object.class);
  }

  public static final <E> EventType<E> createFor(@NonNull String typeName,
      @NonNull Class<E> eventType) {
    EVENT_TYPE_FORMAT_CHECKER.checkThatValueIsWellFormatted(typeName);
    return new EventType<>(typeName, eventType);
  }

  public final String extract() {
    return typeName;
  }

  public final Class<E> getEventClass() {
    return eventType;
  }

  public final boolean isAccepts(Object eventInstance) {
    return eventType.isInstance(eventInstance);
  }

  @Override
  public final String toString() {
    return String.format("%s[%s]", typeName, eventType.getName());
  }

  /**
   * This exception is happened if an event type name is badly formatted.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public static final class WrongEventTypeFormatException extends RuntimeException {
    private static final long serialVersionUID = -1769763038225128075L;

    /**
     * Create the exception object from the event type name.
     *
     * @param typeName The event type name
     */
    public WrongEventTypeFormatException(String typeName) {
      super(String.format(
          "The event type \"%s\" has wrong gormat. It should be matched to the \"%s\" pattern.",
          typeName, PATTERN));
    }
  }
}
