package io.bce;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class URN {
	private static final String URN_PATTERN = "\\burn:[a-zA-Z0-9][a-zA-Z0-9-]{0,31}:[a-zA-Z0-9()+,\\-.:=@;$_!*'%/?#]+";
	private static final FormatChecker URN_FORMAT_CHECKER = FormatChecker.createFor(URN_PATTERN, WrongUrnAddressFormatException::new);
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
		URN_FORMAT_CHECKER.checkThatValueIsWellFormatted(urnAddress);
		return new URN(urnAddress);
	}

	/**
	 * This exception is happened if an URN address is badly formatted
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 */
	public static class WrongUrnAddressFormatException extends RuntimeException {
		private static final long serialVersionUID = -2834624762838052971L;

		public WrongUrnAddressFormatException(String urnAddress) {
			super(String.format("The URN address \"%s\" has wrong format", urnAddress));
		}
	}
}
