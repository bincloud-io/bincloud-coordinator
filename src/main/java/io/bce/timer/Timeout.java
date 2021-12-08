package io.bce.timer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * This class represents the timeout value and allows to work with them.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Timeout {
  private final Long amount;
  private final TemporalUnit unit;

  /**
   * Get timeout milliseconds amount.
   *
   * @return The timeout milliseconds amount
   */
  public final Long getMilliseconds() {
    return getDuration().toMillis();
  }

  private Duration getDuration() {
    return Duration.of(amount, unit);
  }

  /**
   * Create timeout in milliseconds.
   *
   * @param amount The amount of milliseconds
   * @return The timeout value
   */
  public static final Timeout ofMilliseconds(@NonNull Long amount) {
    return new Timeout(amount, ChronoUnit.MILLIS);
  }

  /**
   * Create timeout in seconds.
   *
   * @param amount The amount of seconds
   * @return The timeout value
   */
  public static final Timeout ofSeconds(@NonNull Long amount) {
    return new Timeout(amount, ChronoUnit.SECONDS);
  }

  /**
   * Create timeout in minutes.
   *
   * @param amount The amount of minutes
   * @return The timeout value
   */
  public static final Timeout ofMinutes(@NonNull Long amount) {
    return new Timeout(amount, ChronoUnit.MINUTES);
  }

  /**
   * Create timeout in hours.
   *
   * @param amount The amount of hours
   * @return The timeout value
   */
  public static final Timeout ofHours(@NonNull Long amount) {
    return new Timeout(amount, ChronoUnit.HOURS);
  }

  /**
   * Create timeout in days.
   *
   * @param amount The amount of days
   * @return The timeout value
   */
  public static final Timeout ofDays(@NonNull Long amount) {
    return new Timeout(amount, ChronoUnit.DAYS);
  }
}
