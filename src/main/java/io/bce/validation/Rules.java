package io.bce.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import io.bce.Range;
import io.bce.text.TextTemplate;
import io.bce.text.TextTemplates;
import io.bce.text.TextTemplates.DefaultTextTemplate;
import io.bce.validation.ValidationContext.Rule;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Rules {
	public static final String VALUE_PARAMETER_NAME = "$$value";
	public static final String EXPECTED_VALUE_PARAMETER = "$$expectedValue";

	public static final String MIN_PARAMETER_VALUE = "$$min";
	public static final String MAX_PARAMETER_VALUE = "$$max";

	/**
	 * Create rule checking that the value under validation is equal to the passed
	 * expected value
	 * 
	 * @param <T>           The under validation value type
	 * @param expectedValue The expected value
	 * @param errorMessage  The error message if the rule isn't passed
	 * @return The rule
	 */
	@SuppressWarnings({ "unchecked" })
	public final <T> Rule<T> equalTo(@NonNull T expectedValue, @NonNull TextTemplate errorMessage) {
		ErrorMessageBuilder errorMessageBuilder = ErrorMessageBuilder.of(errorMessage)
				.withParameter(EXPECTED_VALUE_PARAMETER, expectedValue);
		return (Rule<T>) new AssertRule<>(Object.class, errorMessageBuilder, value -> value.equals(expectedValue));
	}

	/**
	 * Create rule checking that the value under validation isn't equal to the
	 * passed expected value
	 * 
	 * @param <T>           The under validation value type
	 * @param expectedValue The expected value
	 * @param errorMessage  The error message if the rule isn't passed
	 * @return The rule
	 */
	public final <T> Rule<T> notEqualTo(@NonNull T expectedValue, @NonNull TextTemplate errorMessage) {
		return equalTo(expectedValue, errorMessage).invert();
	}

	/**
	 * Create rule checking that the boolean value under validation has true value
	 * 
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public final Rule<Boolean> assertTrue(@NonNull TextTemplate errorMessage) {
		return equalTo(true, errorMessage);
	}

	/**
	 * Create rule checking that the boolean value under validation has false value
	 * 
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public final Rule<Boolean> assertFalse(@NonNull TextTemplate errorMessage) {
		return equalTo(false, errorMessage);
	}

	/**
	 * Create rule checking that the value under validation is greater then the
	 * passed minimal value
	 * 
	 * @param <T>          The under validation value type
	 * @param valueType    The under validation value class
	 * @param minValue     The minimal value
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public final <T extends Comparable<T>> Rule<T> greaterThan(@NonNull Class<T> valueType, @NonNull T minValue,
			@NonNull TextTemplate errorMessage) {
		ErrorMessageBuilder errorMessageBuilder = ErrorMessageBuilder.of(errorMessage)
				.withParameter(MIN_PARAMETER_VALUE, minValue);
		return new AssertRule<>(valueType, errorMessageBuilder, value -> value.compareTo(minValue) > 0);
	}

	/**
	 * Create rule checking that the value under validation is less then the passed
	 * maximal value
	 * 
	 * @param <T>          The under validation value type
	 * @param valueType    The under validation value class
	 * @param maxValue     The maximal value
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public final <T extends Comparable<T>> Rule<T> lessThan(@NonNull Class<T> valueType, @NonNull T maxValue,
			@NonNull TextTemplate errorMessage) {
		ErrorMessageBuilder errorMessageBuilder = ErrorMessageBuilder.of(errorMessage)
				.withParameter(MAX_PARAMETER_VALUE, maxValue);
		return new AssertRule<>(valueType, errorMessageBuilder, value -> value.compareTo(maxValue) < 0);
	}

	/**
	 * Create rule checking that the value under validation is greater then the
	 * passed minimal value or equal to this one
	 * 
	 * @param <T>          The under validation value type
	 * @param valueType    The under validation value class
	 * @param minValue     The minimal value
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public final <T extends Comparable<T>> Rule<T> greaterThanOrEqual(@NonNull Class<T> valueType, @NonNull T minValue,
			@NonNull TextTemplate errorMessage) {
		return lessThan(valueType, minValue, errorMessage).invert();
	}

	/**
	 * Create rule checking that the value under validation is less then the passed
	 * maximal value or equal to this one
	 * 
	 * @param <T>          The under validation value type
	 * @param valueType    The under validation value class
	 * @param maxValue     The maximal value
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public final <T extends Comparable<T>> Rule<T> lessThanOrEqual(@NonNull Class<T> valueType, @NonNull T maxValue,
			@NonNull TextTemplate errorMessage) {
		return greaterThan(valueType, maxValue, errorMessage).invert();
	}

	/**
	 * Create rule checking that the value under validation is inside the specified
	 * range
	 * 
	 * @param <T>          The under validation value type
	 * @param valueType    The under validation value class
	 * @param min          The minimal range value (lower threshold)
	 * @param max          The maximal range value (upper threshold)
	 * @param errorMessage The error message
	 * @return The rule
	 */
	public final <T extends Comparable<T>> Rule<T> between(@NonNull Class<T> valueType, @NonNull T min, @NonNull T max,
			@NonNull TextTemplate errorMessage) {
		Range<T> range = Range.createFor(min, max);
		ErrorMessageBuilder errorMessageBuilder = ErrorMessageBuilder.of(errorMessage)
				.withParameter(MIN_PARAMETER_VALUE, range.getMin()).withParameter(MAX_PARAMETER_VALUE, range.getMax());
		return new AssertRule<>(valueType, errorMessageBuilder, value -> range.contains(value));
	}

	/**
	 * Create rule checking that the value under validation is outside the specified
	 * range
	 * 
	 * @param <T>          The under validation value type
	 * @param valueType    The under validation value class
	 * @param min          The minimal range value (lower threshold)
	 * @param max          The maximal range value (upper threshold)
	 * @param errorMessage The error message
	 * @return The rule
	 */
	public final <T extends Comparable<T>> Rule<T> outside(@NonNull Class<T> comparableType, @NonNull T min,
			@NonNull T max, @NonNull TextTemplate errorMessage) {
		return between(comparableType, min, max, errorMessage).invert();
	}

	/**
	 * Create rule checking that the character sequence value under validation is
	 * empty sequence
	 * 
	 * @param <T>          The under validation value type
	 * @param valueType    The character sequence concrete type name
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public final <T extends CharSequence> Rule<T> empty(@NonNull Class<T> valueType,
			@NonNull TextTemplate errorMessage) {
		ErrorMessageBuilder errorMessageBuilder = new ErrorMessageBuilder(errorMessage);
		return new AssertRule<>(valueType, errorMessageBuilder, value -> value.length() == 0);
	}

	/**
	 * Create rule checking that the character sequence value under validation is
	 * not empty sequence
	 * 
	 * @param <T>          The under validation value type
	 * @param valueType    The character sequence concrete type name
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public final <T extends CharSequence> Rule<T> notEmpty(@NonNull Class<T> valueType,
			@NonNull TextTemplate errorMessage) {
		return empty(valueType, errorMessage).invert();
	}

	public final <T extends CharSequence> Rule<T> minLength(@NonNull Class<T> valueType, @NonNull Long minLength,
			@NonNull TextTemplate errorMessage) {
		ErrorMessageBuilder errorMessageBuilder = new ErrorMessageBuilder(errorMessage)
				.withParameter(MIN_PARAMETER_VALUE, minLength);
		return new AssertRule<>(valueType, errorMessageBuilder,
				value -> value.length() <= minLength.longValue());
	}

	public final <T extends CharSequence> Rule<T> maxLength(@NonNull Class<T> valueType, @NonNull Long minLength,
			@NonNull TextTemplate errorMessage) {
		throw new UnsupportedOperationException();
	}

	public static final class SizeMustNotBeNegativeValue extends RuntimeException {
		private static final long serialVersionUID = -7584943704277538153L;

	}

	/**
	 * Create rule checking that the collection under validation is empty collection
	 * 
	 * @param <T>            The under validation value type
	 * @param collectionType The under validation collection type
	 * @param errorMessage   The error message if the rule isn't passed
	 * @return The rule
	 */
	public final <E, T extends Collection<E>> Rule<T> isEmptyCollection(@NonNull Class<T> collectionType,
			@NonNull TextTemplate errorMessage) {
		ErrorMessageBuilder errorMessageBuilder = new ErrorMessageBuilder(errorMessage);
		return new AssertRule<>(collectionType, errorMessageBuilder, value -> value.size() == 0);
	}

	/**
	 * Create rule checking that the collection under validation is empty collection
	 * 
	 * @param <T>            The under validation value type
	 * @param collectionType The character sequence concrete type name
	 * @param errorMessage   The error message if the rule isn't passed
	 * @return The rule
	 */
	public final <E, T extends Collection<E>> Rule<T> isNotEmptyCollection(@NonNull Class<T> collectionType,
			@NonNull TextTemplate errorMessage) {
		return isEmptyCollection(collectionType, errorMessage).invert();
	}

	private static class ErrorMessageBuilder {
		private DefaultTextTemplate textTemplate;

		private ErrorMessageBuilder(TextTemplate textTemplate) {
			super();
			this.textTemplate = TextTemplates.createBy(textTemplate);
		}

		public static final ErrorMessageBuilder of(TextTemplate textTemplate) {
			return new ErrorMessageBuilder(textTemplate);
		}

		public <T> ErrorMessageBuilder withParameter(String name, T value) {
			this.textTemplate = textTemplate.withParameter(name, value);
			return this;
		}

		public <T> TextTemplate build(T value) {
			return textTemplate.withParameter(VALUE_PARAMETER_NAME, value);
		}
	}

	private static class AssertRule<T> extends TypeSafeRule<T> implements Rule<T> {
		private final ErrorMessageBuilder errorMessage;
		private final RulePredicate<T> ruleCheckPredicate;

		public AssertRule(@NonNull Class<T> valueType, @NonNull ErrorMessageBuilder message,
				@NonNull RulePredicate<T> ruleCheckPredicate) {
			super(valueType);
			this.errorMessage = message;
			this.ruleCheckPredicate = ruleCheckPredicate;
		}

		@Override
		public Collection<TextTemplate> check(T value) {
			return Optional.ofNullable(value).filter(ruleCheckPredicate::checkRuleFor).map(this::createResultErrorsList)
					.orElse(Collections.emptyList());
		}

		private Collection<TextTemplate> createResultErrorsList(T value) {
			return Arrays.asList(errorMessage.build(value));
		}

		@Override
		public Rule<T> invert() {
			RulePredicate<T> originalRuleCheckPredicate = this.ruleCheckPredicate;
			return new AssertRule<>(type, errorMessage, value -> !originalRuleCheckPredicate.checkRuleFor(value));
		}

	}

	public interface RulePredicate<T> {
		public boolean checkRuleFor(T value);
	}
}
