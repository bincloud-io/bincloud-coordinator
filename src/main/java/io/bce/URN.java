package io.bce;

import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class URN {
	private static final Pattern URN_PATTERN = Pattern
			.compile("\\burn:[a-zA-Z0-9][a-zA-Z0-9-]{0,31}:[a-zA-Z0-9()+,\\-.:=@;$_!*'%/?#]+");
	private final String addressString;
	
	@Override
	public String toString() {
		return addressString;
	}
	
	/**
	 * Create the target address of an URN-address string value
	 * 
	 * @param urnAddress The URN-address string value
	 * @return The target address
	 */
	public static final URN ofURN(@NonNull String urnAddress) {
		checkThatAddressIsMatchedToPattern(urnAddress);
		return new URN(urnAddress);
	}

	private static void checkThatAddressIsMatchedToPattern(String urnAddress) {
		if (!URN_PATTERN.matcher(urnAddress).matches()) {
			throw new WrongUrnAddressFormatException(urnAddress);
		}
	}

	public static class WrongUrnAddressFormatException extends RuntimeException {
		private static final long serialVersionUID = -2834624762838052971L;

		public WrongUrnAddressFormatException(String urnAddress) {
			super(String.format("The URN address \"%s\" has wrong format", urnAddress));
		}
	}
}
