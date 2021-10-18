package io.bce.actor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ActorAddress {
	public static final ActorAddress UNKNOWN_ADDRESS = new ActorAddress("urn:actor:SYSTEM.DEAD_LETTER");
	private static final Pattern URN_PATTERN = Pattern.compile("urn:actor:(.+)");

	private final String addressURN;

	public ActorName getActorName() {
		return ActorName.wrap(getURNMatcher().group(1));
	}
	
	@Override
	public String toString() {
		return addressURN;
	}

	private Matcher getURNMatcher() {
		Matcher urnMatcher = URN_PATTERN.matcher(addressURN);
		urnMatcher.find();
		return urnMatcher;
	}
	
	public static final ActorAddress ofURN(@NonNull String urn) {
		checkThatURNFormatIsCorrect(urn);
		return new ActorAddress(urn);
	}

	static final ActorAddress ofName(@NonNull ActorName actorName) {
		return ofURN(String.format("urn:actor:%s", actorName));
	}

	private static final void checkThatURNFormatIsCorrect(String urn) {
		if (!URN_PATTERN.matcher(urn).matches()) {
			throw new WrongActorAddressFormatException(urn);
		}
	}

	public static class WrongActorAddressFormatException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public WrongActorAddressFormatException(String urn) {
			super(String.format("The actor URN \"%s\" isn't matched to the \"%s\" pattern", urn, URN_PATTERN));
		}
	}
}
