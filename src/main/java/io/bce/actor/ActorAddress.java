package io.bce.actor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.bce.FormatChecker;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ActorAddress {
	public static final ActorAddress UNKNOWN_ADDRESS = new ActorAddress("urn:actor:SYSTEM.DEAD_LETTER");
	private static final String ACTOR_URN_PATTERN = "urn:actor:(.+)";
	private static final FormatChecker ACTOR_URN_FORMAT_CHECKER = FormatChecker.createFor(ACTOR_URN_PATTERN,
			WrongActorAddressFormatException::new);

	private final String addressURN;

	public ActorName getActorName() {
		return ActorName.wrap(getURNMatcher().group(1));
	}

	@Override
	public String toString() {
		return addressURN;
	}

	private Matcher getURNMatcher() {
		Pattern urnPattern = Pattern.compile(ACTOR_URN_PATTERN);
		Matcher urnMatcher = urnPattern.matcher(addressURN);
		urnMatcher.find();
		return urnMatcher;
	}

	public static final ActorAddress ofURN(@NonNull String urn) {
		ACTOR_URN_FORMAT_CHECKER.checkThatValueIsWellFormatted(urn);
		return new ActorAddress(urn);
	}

	static final ActorAddress ofName(@NonNull ActorName actorName) {
		return ofURN(String.format("urn:actor:%s", actorName));
	}

	/**
	 * This exception is happened if an actor URN address is badly formatted
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 */
	public static class WrongActorAddressFormatException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public WrongActorAddressFormatException(String urn) {
			super(String.format("The actor URN \"%s\" isn't matched to the \"%s\" pattern", urn, ACTOR_URN_PATTERN));
		}
	}
}
