package io.bincloud.common.time;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode
public class DateTime implements Comparable<DateTime> {
	@NonNull
	private Date unit;
	@NonNull
	private Long fraction;
	
	public DateTime() {
		this(Instant.now().truncatedTo(ChronoUnit.SECONDS)); 
	}
	
	public DateTime(@NonNull Instant instant) {
		super();
		this.unit = new Date(instant.getEpochSecond() * 1000);
		this.fraction = (long) instant.getNano();
	}
	
	public DateTime(@NonNull State internalState) {
		super();
		this.unit = internalState.getUnit();
		this.fraction = internalState.getFraction();
	}
	
	public State internalState() {
		return new State() {
			@Override
			public Date getUnit() {
				return unit;
			}
			
			@Override
			public Long getFraction() {
				return fraction;
			}
		};
	}
	
	public DateTime plus(Long amount, TemporalUnit unit) {
		return new DateTime(toInstant().plus(amount, unit));
	}
	
	public DateTime minus(Long amount, TemporalUnit unit) {
		return new DateTime(toInstant().minus(amount, unit));
	}
	
	public Instant toInstant() {
		return unit.toInstant().plusNanos(fraction);
	}

	@Override
	public int compareTo(DateTime opposite) {
		return Optional.ofNullable(opposite)
				.map(value -> toInstant().compareTo(value.toInstant()))
				.orElse(1);
	}
	
	public static DateTime now() {
		return new DateTime(Instant.now());
	}
	
	
	
	@Override
	public String toString() {
		return toInstant().toString();
	}



	public interface State {
		public Date getUnit();
		public Long getFraction();
	}
}
