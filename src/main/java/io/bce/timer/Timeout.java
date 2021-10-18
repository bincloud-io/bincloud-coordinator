package io.bce.timer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Timeout {
	private final Long amount;
	private final TemporalUnit unit;
	
	public final Long getMilliseconds() {
		return getDuration().toMillis();
	}
	
	private Duration getDuration() {
		return Duration.of(amount, unit);
	}

	public static final Timeout ofMilliseconds(@NonNull Long amount) {
		return new Timeout(amount, ChronoUnit.MILLIS);
	}

	public static final Timeout ofSeconds(@NonNull Long amount) {
		return new Timeout(amount, ChronoUnit.SECONDS);
	}

	public static final Timeout ofMinutes(@NonNull Long amount) {
		return new Timeout(amount, ChronoUnit.MINUTES);
	}

	public static final Timeout ofHours(@NonNull Long amount) {
		return new Timeout(amount, ChronoUnit.HOURS);
	}

	public static final Timeout ofDays(@NonNull Long amount) {
		return new Timeout(amount, ChronoUnit.DAYS);
	}
}
