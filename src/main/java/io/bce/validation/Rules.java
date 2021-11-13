package io.bce.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

import io.bce.Range;
import io.bce.validation.ValidationContext.Rule;
import lombok.NonNull;

public class Rules {
	public static final String VALIDATED_ELEMENT_PARAMETER_NAME = "$$element";
	public static final String EXPECTED_VALUE_PARAMETER = "$$value";

	public static final String MIN_PARAMETER_VALUE = "$$minValue";
	public static final String MAX_PARAMETER_VALUE = "$$maxValue";

	public static final String EXPECTED_LENGTH_PARAMETER_VALUE = "$$length";
	public static final String MIN_LENGTH_PARAMETER_VALUE = "$$minLength";
	public static final String MAX_LENGTH_PARAMETER_VALUE = "$$maxLength";

	public static final String EXPECTED_SIZE_PARAMETER_VALUE = "$$size";
	public static final String MIN_SIZE_PARAMETER_VALUE = "$$minSize";
	public static final String MAX_SIZE_PARAMETER_VALUE = "$$maxSize";

	public static final String REGEXP_PARAMETER_VALUE = "$$pattern";

	/**
	 * Create rule checking that the value under validation is not null
	 * 
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final Rule<Object> notNull(ErrorMessage errorMessage) {
		return wrapIsPresentMatcherRule(match(Optional.class, errorMessage, Optional::isPresent));

	}

	/**
	 * Create rule checking that the value under validation is null
	 * 
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final Rule<Object> isNull(ErrorMessage errorMessage) {
		return notNull(errorMessage).invert();
	}

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
	public static final <T> Rule<T> equalTo(@NonNull T expectedValue, @NonNull ErrorMessage errorMessage) {
		return (Rule<T>) new AssertRule<>(Object.class,
				errorMessage.withParameter(EXPECTED_VALUE_PARAMETER, expectedValue),
				value -> value.equals(expectedValue));
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
	public static final <T> Rule<T> notEqualTo(@NonNull T expectedValue, @NonNull ErrorMessage errorMessage) {
		return equalTo(expectedValue, errorMessage).invert();
	}

	/**
	 * Create rule checking that the boolean value under validation has true value
	 * 
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final Rule<Boolean> assertTrue(@NonNull ErrorMessage errorMessage) {
		return equalTo(true, errorMessage);
	}

	/**
	 * Create rule checking that the boolean value under validation has false value
	 * 
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final Rule<Boolean> assertFalse(@NonNull ErrorMessage errorMessage) {
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
	public static final <T extends Comparable<T>> Rule<T> greaterThan(@NonNull Class<T> valueType, @NonNull T minValue,
			@NonNull ErrorMessage errorMessage) {
		return new AssertRule<>(valueType, errorMessage.withParameter(MIN_PARAMETER_VALUE, minValue),
				value -> value.compareTo(minValue) > 0);
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
	public static final <T extends Comparable<T>> Rule<T> greaterThanOrEqual(@NonNull Class<T> valueType,
			@NonNull T minValue, @NonNull ErrorMessage errorMessage) {
		return new AssertRule<>(valueType, errorMessage.withParameter(MIN_PARAMETER_VALUE, minValue),
				value -> value.compareTo(minValue) >= 0);
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
	public static final <T extends Comparable<T>> Rule<T> lessThan(@NonNull Class<T> valueType, @NonNull T maxValue,
			@NonNull ErrorMessage errorMessage) {
		return new AssertRule<>(valueType, errorMessage.withParameter(MAX_PARAMETER_VALUE, maxValue),
				value -> value.compareTo(maxValue) < 0);
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
	public static final <T extends Comparable<T>> Rule<T> lessThanOrEqual(@NonNull Class<T> valueType,
			@NonNull T maxValue, @NonNull ErrorMessage errorMessage) {
		return new AssertRule<>(valueType, errorMessage.withParameter(MAX_PARAMETER_VALUE, maxValue),
				value -> value.compareTo(maxValue) <= 0);
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
	public static final <T extends Comparable<T>> Rule<T> between(@NonNull Class<T> valueType, @NonNull T min,
			@NonNull T max, @NonNull ErrorMessage errorMessage) {
		Range<T> range = Range.createFor(min, max);
		return new AssertRule<>(valueType, errorMessage.withParameter(MIN_PARAMETER_VALUE, range.getMin())
				.withParameter(MAX_PARAMETER_VALUE, range.getMax()), value -> range.contains(value));
	}

	/**
	 * Create rule checking that the value under validation is outside the specified
	 * range
	 * 
	 * @param <T>          The under validation value type name
	 * @param valueType    The under validation value class
	 * @param min          The minimal range value (lower threshold)
	 * @param max          The maximal range value (upper threshold)
	 * @param errorMessage The error message
	 * @return The rule
	 */
	public static final <T extends Comparable<T>> Rule<T> outside(@NonNull Class<T> comparableType, @NonNull T min,
			@NonNull T max, @NonNull ErrorMessage errorMessage) {
		return between(comparableType, min, max, errorMessage).invert();
	}

	/**
	 * Create rule checking that the under validation character sequence value
	 * length is equal to the specified value
	 * 
	 * @param <T>          The under validation value type name
	 * @param valueType    The under validation value class
	 * @param length       The expected collection length
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final <T extends CharSequence> Rule<T> hasLength(@NonNull Class<T> valueType, @NonNull Long length,
			@NonNull ErrorMessage errorMessage) {
		checkThatTheSizeValueIsNotNegative(length);
		return new AssertRule<>(valueType, errorMessage.withParameter(EXPECTED_LENGTH_PARAMETER_VALUE, length),
				value -> value.length() == length);
	}

	/**
	 * Create rule checking that the under validation character sequence value
	 * length is equal to the specified value
	 * 
	 * @param <T>          The under validation value type name
	 * @param valueType    The under validation value class
	 * @param length       The expected collection length
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final <T extends CharSequence> Rule<T> doesNotHaveLength(@NonNull Class<T> valueType,
			@NonNull Long length, @NonNull ErrorMessage errorMessage) {
		return hasLength(valueType, length, errorMessage).invert();
	}

	/**
	 * Create rule checking that the character sequence value under validation is
	 * empty sequence
	 * 
	 * @param valueType    The under validation value class
	 * @param valueType    The character sequence concrete type
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final <T extends CharSequence> Rule<T> empty(@NonNull Class<T> valueType,
			@NonNull ErrorMessage errorMessage) {
		return new AssertRule<>(valueType, errorMessage, value -> value.length() == 0);
	}

	/**
	 * Create rule checking that the character sequence value under validation is
	 * not empty sequence er
	 * 
	 * @param <T>          The under validation value type name
	 * @param valueType    The under validation value class
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final <T extends CharSequence> Rule<T> notEmpty(@NonNull Class<T> valueType,
			@NonNull ErrorMessage errorMessage) {
		return empty(valueType, errorMessage).invert();
	}

	/**
	 * Create rule checking that the character under validation sequence length is
	 * not less then minimal value
	 * 
	 * @param <T>          The under validation value type name
	 * @param valueType    The under validation value class
	 * @param minLength    The character sequence minimal length
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final <T extends CharSequence> Rule<T> minLength(@NonNull Class<T> valueType, @NonNull Long minLength,
			@NonNull ErrorMessage errorMessage) {
		checkThatTheSizeValueIsNotNegative(minLength);
		return new AssertRule<>(valueType, errorMessage.withParameter(MIN_LENGTH_PARAMETER_VALUE, minLength),
				value -> value.length() >= minLength.longValue());
	}

	/**
	 * Create rule checking that the character under validation sequence length is
	 * not greater then maximal value
	 * 
	 * @param <T>          The under validation value type name
	 * @param valueType    The under validation value class
	 * @param maxLength    The character sequence maximal length
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final <T extends CharSequence> Rule<T> maxLength(@NonNull Class<T> valueType, @NonNull Long maxLength,
			@NonNull ErrorMessage errorMessage) {
		checkThatTheSizeValueIsNotNegative(maxLength);
		return new AssertRule<>(valueType, errorMessage.withParameter(MAX_LENGTH_PARAMETER_VALUE, maxLength),
				value -> value.length() <= maxLength.longValue());
	}

	/**
	 * Create rule checking that the character under validation sequence length is
	 * limited by the minimal and maximal values and doesn't violate them
	 * 
	 * @param <T>          The under validation value type name
	 * @param valueType    The under validation value class
	 * @param minLength    The character sequence minimal length
	 * @param maxLength    The character sequence maximal length
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final <T extends CharSequence> Rule<T> limitedLength(@NonNull Class<T> valueType,
			@NonNull Long minLength, @NonNull Long maxLength, @NonNull ErrorMessage errorMessage) {
		checkThatTheSizeValueIsNotNegative(minLength);
		checkThatTheSizeValueIsNotNegative(maxLength);
		Range<Long> lengthRange = Range.createFor(minLength, maxLength);
		return new AssertRule<>(valueType, errorMessage.withParameter(MIN_LENGTH_PARAMETER_VALUE, minLength)
				.withParameter(MAX_LENGTH_PARAMETER_VALUE, maxLength),
				value -> lengthRange.contains((long) value.length()));
	}

	/**
	 * Create rule checking that the under validation character sequence length is
	 * matched to the specified regular expression
	 * 
	 * @param <T>          The under validation value type name
	 * @param valueType    The under validation value class
	 * @param regexp       The regular expression
	 * @param maxLength    The character sequence maximal length
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final <T extends CharSequence> Rule<T> pattern(@NonNull Class<T> valueType, @NonNull Pattern regexp,
			@NonNull ErrorMessage errorMessage) {
		return new AssertRule<>(valueType, errorMessage.withParameter(REGEXP_PARAMETER_VALUE, regexp),
				value -> regexp.matcher(value).matches());
	}

	/**
	 * Create rule checking that the under validation collection size is equal to
	 * the specified value
	 * 
	 * @param <T>            The under validation value type name
	 * @param collectionType The under validation value class
	 * @param size           The expected collection size
	 * @param errorMessage   The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final <E, T extends Collection<E>> Rule<T> collectionHasSize(@NonNull Class<T> collectionType,
			@NonNull Long size, @NonNull ErrorMessage errorMessage) {
		checkThatTheSizeValueIsNotNegative(size);
		return new AssertRule<>(collectionType, errorMessage.withParameter(EXPECTED_SIZE_PARAMETER_VALUE, size),
				value -> value.size() == size.longValue());
	}

	/**
	 * Create rule checking that the under validation collection size is not equal
	 * to the specified value
	 * 
	 * @param <T>            The under validation value type name
	 * @param collectionType The under validation value class
	 * @param size           The wrong collection size
	 * @param errorMessage   The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final <E, T extends Collection<E>> Rule<T> collectionDoesNotHaveSize(@NonNull Class<T> collectionType,
			@NonNull Long size, @NonNull ErrorMessage errorMessage) {
		return collectionHasSize(collectionType, size, errorMessage).invert();
	}

	/**
	 * Create rule checking that the collection under validation is empty collection
	 * 
	 * @param <T>            The under validation value type name
	 * @param collectionType The under validation value class
	 * @param errorMessage   The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final <E, T extends Collection<E>> Rule<T> emptyCollection(@NonNull Class<T> collectionType,
			@NonNull ErrorMessage errorMessage) {
		return new AssertRule<>(collectionType, errorMessage, value -> value.size() == 0);
	}

	/**
	 * Create rule checking that the collection under validation is empty collection
	 * 
	 * @param <T>            The under validation value type name
	 * @param collectionType The under validation value class
	 * @param errorMessage   The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final <E, T extends Collection<E>> Rule<T> notEmptyCollection(@NonNull Class<T> collectionType,
			@NonNull ErrorMessage errorMessage) {
		return emptyCollection(collectionType, errorMessage).invert();
	}

	/**
	 * Create rule checking that the character under validation collection size is
	 * not less then minimal value
	 * 
	 * @param <T>            The under validation value type name
	 * @param collectionType The under validation value class
	 * @param minSize        The character sequence minimal length
	 * @param errorMessage   The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final <E, T extends Collection<E>> Rule<T> minCollectionSize(@NonNull Class<T> collectionType,
			@NonNull Long minSize, @NonNull ErrorMessage errorMessage) {
		checkThatTheSizeValueIsNotNegative(minSize);
		return new AssertRule<>(collectionType, errorMessage.withParameter(MIN_SIZE_PARAMETER_VALUE, minSize),
				value -> value.size() >= minSize.longValue());
	}

	/**
	 * Create rule checking that the character under validation collection size is
	 * not greater then maximal value
	 * 
	 * @param <T>            The under validation value type name
	 * @param collectionType The under validation value class
	 * @param maxSize        The collection maximal size
	 * @param errorMessage   The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final <E, T extends Collection<E>> Rule<T> maxCollectionSize(@NonNull Class<T> collectionType,
			@NonNull Long maxSize, @NonNull ErrorMessage errorMessage) {
		checkThatTheSizeValueIsNotNegative(maxSize);
		return new AssertRule<>(collectionType, errorMessage.withParameter(MAX_SIZE_PARAMETER_VALUE, maxSize),
				value -> value.size() <= maxSize.longValue());
	}

	/**
	 * Create rule checking that the character under validation collection size is
	 * not greater then maximal value
	 * 
	 * @param <T>            The under validation value type name
	 * @param collectionType The under validation value class
	 * @param maxSize        The collection maximal size
	 * @param errorMessage   The error message if the rule isn't passed
	 * @return The rule
	 */
	public static final <E, T extends Collection<E>> Rule<T> limitedCollectionSize(@NonNull Class<T> collectionType,
			@NonNull Long minSize, @NonNull Long maxSize, @NonNull ErrorMessage errorMessage) {
		checkThatTheSizeValueIsNotNegative(minSize);
		checkThatTheSizeValueIsNotNegative(maxSize);
		Range<Long> range = Range.createFor(minSize, maxSize);
		return new AssertRule<>(collectionType, errorMessage.withParameter(MIN_SIZE_PARAMETER_VALUE, minSize)
				.withParameter(MAX_SIZE_PARAMETER_VALUE, maxSize), value -> range.contains((long) value.size()));
	}

	/**
	 * Create the rule, which matches the random value using predicate.
	 * 
	 * @param <T>          The under validation value type name
	 * @param valueType    The under validation value class
	 * @param errorMessage The error message if the rule isn't passed
	 * @param predicate    The rule checking predicate, which will make decision if
	 *                     the value is valid or not
	 * @return The rule
	 */
	public static final <T> Rule<T> match(@NonNull Class<T> valueType, @NonNull ErrorMessage errorMessage,
			RulePredicate<T> predicate) {
		return new AssertRule<>(valueType, errorMessage, predicate);
	}

	@SuppressWarnings("rawtypes")
	private static final Rule<Object> wrapIsPresentMatcherRule(Rule<Optional> rule) {
		return new Rule<Object>() {
			@Override
			public boolean isAcceptableFor(Object value) {
				return true;
			}

			@Override
			public Collection<ErrorMessage> check(Object value) {
				return rule.check(Optional.ofNullable(value));
			}

			@Override
			public Rule<Object> invert() {
				return wrapIsPresentMatcherRule(rule.invert());
			}
		};
	}

	private static final void checkThatTheSizeValueIsNotNegative(Long value) {
		if (value.compareTo(0L) < 0) {
			throw new SizeMustNotBeNegativeValue();
		}
	}

	public interface RulePredicate<T> {
		public boolean checkRuleFor(T value);
	}

	private static class AssertRule<T> extends TypeSafeRule<T> implements Rule<T> {
		private final ErrorMessage errorMessage;
		private final RulePredicate<T> ruleCheckPredicate;

		public AssertRule(@NonNull Class<T> valueType, @NonNull ErrorMessage message,
				@NonNull RulePredicate<T> ruleCheckPredicate) {
			super(valueType);
			this.errorMessage = message;
			this.ruleCheckPredicate = ruleCheckPredicate;
		}

		@Override
		public Collection<ErrorMessage> check(T value) {
			return Optional.ofNullable(value).filter(v -> !ruleCheckPredicate.checkRuleFor(v))
					.map(this::createResultErrorsList).orElse(Collections.emptyList());
		}

		private Collection<ErrorMessage> createResultErrorsList(T value) {
			return Arrays.asList(errorMessage.withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, value));
		}

		@Override
		public Rule<T> invert() {
			RulePredicate<T> originalRuleCheckPredicate = this.ruleCheckPredicate;
			return new AssertRule<>(type, errorMessage, value -> !originalRuleCheckPredicate.checkRuleFor(value));
		}

	}

	/**
	 * This exception notifies that the size or length amount has negative value,
	 * but must not be
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 */
	public static final class SizeMustNotBeNegativeValue extends RuntimeException {
		private static final long serialVersionUID = -7584943704277538153L;

		public SizeMustNotBeNegativeValue() {
			super("Values, representing size or length values must not be negative.");
		}
	}
}
