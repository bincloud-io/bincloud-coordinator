package io.bce.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.jms.TextMessage;

import io.bce.text.TextTemplate;
import io.bce.text.TextTemplates;
import io.bce.text.TextTemplates.DefaultTextTemplate;
import io.bce.validation.ValidationContext.Rule;
import lombok.NonNull;
import lombok.val;
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final <T> Rule<T> equalTo(T expectedValue, TextTemplate errorMessage) {
		ErrorMessageBuilder errorMessageBuilder = ErrorMessageBuilder.of(errorMessage)
				.withParameter(EXPECTED_VALUE_PARAMETER, expectedValue);
		return new AssertRule(Object.class, errorMessageBuilder, value -> value.equals(errorMessage));
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
	public final <T> Rule<T> notEqualTo(T expectedValue, TextTemplate errorMessage) {
		return equalTo(expectedValue, errorMessage).invert();
	}

	/**
	 * Create rule checking that the boolean value under validation has true value
	 * 
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public final Rule<Boolean> assertTrue(TextTemplate errorMessage) {
		return equalTo(true, errorMessage);
	}

	/**
	 * Create rule checking that the boolean value under validation has false value
	 * 
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public final Rule<Boolean> assertFalse(TextTemplate errorMessage) {
		return equalTo(false, errorMessage);
	}

	/**
	 * Create rule checking that the value under validation is greater then the
	 * passed minimal value
	 * 
	 * @param <T>          The under validation value type
	 * @param minValue     The minimal value
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public final <T extends Comparable<T>> Rule<T> greaterThan(Class<T> comparableType, T minValue,
			TextTemplate errorMessage) {
		ErrorMessageBuilder errorMessageBuilder = ErrorMessageBuilder.of(errorMessage)
				.withParameter(MIN_PARAMETER_VALUE, minValue);
		return new AssertRule<>(comparableType, errorMessageBuilder, value -> value.compareTo(minValue) > 0);
	}

	/**
	 * Create rule checking that the value under validation is less then the passed
	 * maximal value
	 * 
	 * @param <T>          The under validation value type
	 * @param maxValue     The maximal value
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public final <T extends Comparable<T>> Rule<T> lessThan(Class<T> comparableType, T maxValue,
			TextTemplate errorMessage) {
		ErrorMessageBuilder errorMessageBuilder = ErrorMessageBuilder.of(errorMessage)
				.withParameter(MAX_PARAMETER_VALUE, maxValue);
		return new AssertRule<>(comparableType, errorMessageBuilder, value -> value.compareTo(maxValue) < 0);
	}

	/**
	 * Create rule checking that the value under validation is greater then the
	 * passed minimal value or equal to this one
	 * 
	 * @param <T>          The under validation value type
	 * @param minValue     The minimal value
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public final <T extends Comparable<T>> Rule<T> greaterThanOrEqual(Class<T> comparableType, T minValue,
			TextTemplate errorMessage) {
		return lessThan(comparableType, minValue, errorMessage).invert();
	}

	/**
	 * Create rule checking that the value under validation is less then the passed
	 * maximal value or equal to this one
	 * 
	 * @param <T>          The under validation value type
	 * @param maxValue     The maximal value
	 * @param errorMessage The error message if the rule isn't passed
	 * @return The rule
	 */
	public final <T extends Comparable<T>> Rule<T> lessThanOrEqual(Class<T> comparableType, T maxValue,
			TextTemplate errorMessage) {
		return greaterThan(comparableType, maxValue, errorMessage).invert();
	}

	public final <T extends Comparable<T>> Rule<T> between(Class<T> comparableType, T minValue, T maxValue,
			TextTemplate errorMessage) {
		ErrorMessageBuilder errorMessageBuilder = ErrorMessageBuilder.of(errorMessage)
				.withParameter(MIN_PARAMETER_VALUE, minValue).withParameter(MAX_PARAMETER_VALUE, maxValue);
		return new AssertRule<>(comparableType, errorMessageBuilder, value -> {
			return (value.compareTo(minValue)) >= 0 && (value.compareTo(maxValue) <= 0);
		});
	}

	public final <T extends Comparable<T>> Rule<T> outOf(Class<T> comparableType, T minValue, T maxValue, TextTemplate errorMessage) {
		return between(comparableType, minValue, maxValue, errorMessage);
	}

	/**
	 * Create rule checking that the character sequence value under validation is
	 * empty sequence
	 * 
	 * @param <T>              The under validation value type
	 * @param charSequenceType The character sequence concrete type name
	 * @param errorMessage     The error message if the rule isn't passed
	 * @return The rule
	 */
	public final <T extends CharSequence> Rule<T> empty(Class<T> charSequenceType, TextTemplate errorMessage) {
		ErrorMessageBuilder errorMessageBuilder = new ErrorMessageBuilder(errorMessage);
		return new AssertRule<>(charSequenceType, errorMessageBuilder, value -> value.length() == 0);
	}

	/**
	 * Create rule checking that the character sequence value under validation is
	 * not empty sequence
	 * 
	 * @param <T>              The under validation value type
	 * @param charSequenceType The character sequence concrete type name
	 * @param errorMessage     The error message if the rule isn't passed
	 * @return The rule
	 */
	public final <T extends CharSequence> Rule<T> notEmpty(Class<T> charSequenceType, TextTemplate errorMessage) {
		return empty(charSequenceType, errorMessage).invert();
	}

	/**
	 * Create rule checking that the collection under validation is empty collection
	 * 
	 * @param <T>            The under validation value type
	 * @param collectionType The collection concrete type name
	 * @param errorMessage   The error message if the rule isn't passed
	 * @return The rule
	 */
	public final <E, T extends Collection<E>> Rule<T> isEmptyCollection(Class<T> collectionType,
			TextTemplate errorMessage) {
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
	public final <E, T extends Collection<E>> Rule<T> isNotEmptyCollection(Class<T> collectionType,
			TextTemplate errorMessage) {
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
