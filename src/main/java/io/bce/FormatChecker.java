package io.bce;

import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FormatChecker {
	private final Pattern expressionPattern;
	private final ErrorFactory errorFactory;
	
	public void checkThatValueIsWellFormatted(String value) {
		if (!expressionPattern.matcher(value).matches()) {
			throw errorFactory.createError(value);
		}
	}
	
	public interface ErrorFactory {
		public RuntimeException createError(String value);
	}

	public static final FormatChecker createFor(String expressionPattern) {
		return createFor(expressionPattern, value -> new WrongValueFormatException(value, expressionPattern));
	}
	
	public static final FormatChecker createFor(String expressionPattern, ErrorFactory errorFactory) {
		return new FormatChecker(Pattern.compile(expressionPattern), errorFactory);
	}

	public static class WrongValueFormatException extends RuntimeException {
		private static final long serialVersionUID = -1953906038603832486L;

		public WrongValueFormatException(String value, String pattern) {
			super(String.format("The \"%s\" value has wrong format. It must be matched to the \"%s\" pattern.", value,
					pattern));
		}
	}
}
