package io.bce.validation;

import java.util.Collection;

import io.bce.text.TextTemplate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefaultValidationContext implements ValidationContext {
	private final ValidationGroup group;
	private final DerivationPolicy derivation;
	private final ValidationState validationState;

	DefaultValidationContext() {
		this(ValidationGroup.UNGROUPED, DerivationPolicy.DERIVE_GROUPES, new ValidationState());
	}

	/**
	 * Get the result validation state
	 * 
	 * @return The validation state
	 */
	public ValidationState getState() {
		return this.derivation.deriveState(group, validationState);
	}

	/**
	 * Validate an object, implementing the {@link Validatable} interface
	 * 
	 * @param validatable The validatable object
	 * @return The derived context
	 */
	@Override
	public ValidationContext validate(Validatable validatable) {
		return validate(ValidationGroup.UNGROUPED, validatable);
	}

	/**
	 * Validate an object, implementing the {@link Validatable} interface using
	 * specified derivation policy
	 * 
	 * @param validatable      The validatable object
	 * @param derivationPolicy The derivation policy
	 * @return The derived context
	 */
	@Override
	public ValidationContext validate(Validatable validatable, DerivationPolicy derivationPolicy) {
		return validate(ValidationGroup.UNGROUPED, validatable, derivationPolicy);
	}

	/**
	 * Validate an object, implementing the {@link Validatable} interface, grouped
	 * by a specified group
	 * 
	 * @param groupName   The validation group
	 * @param validatable The validatable object
	 * @return The derived context
	 */
	@Override
	public ValidationContext validate(String groupName, Validatable validatable) {
		return validate(ValidationGroup.createFor(groupName), validatable);
	}

	/**
	 * Validate an object, implementing the {@link Validatable} interface, grouped
	 * by a specified group, using specified derivation policy
	 * 
	 * @param groupName        The validation group
	 * @param validatable      The validatable object
	 * @param derivationPolicy The derivation policy
	 * @return The derived context
	 */
	@Override
	public ValidationContext validate(String groupName, Validatable validatable, DerivationPolicy derivationPolicy) {
		return validate(ValidationGroup.createFor(groupName), validatable, derivationPolicy);
	}
	
	@Override
	public <T> ValidationContext withRule(ValueProvider<T> valueProvider, Rule<T> rule) {
		return withRule(valueProvider, rule, (context, errors) -> withErrors(errors));
	}

	@Override
	public <T> ValidationContext withRule(String groupName, ValueProvider<T> valueProvider, Rule<T> rule) {
		return withRule(valueProvider, rule, (context, errors) -> withErrors(groupName, errors));
	}

	@Override
	public ValidationContext withErrors(TextTemplate... errors) {
		return withErrors(errors, (state, error) -> state.withUngrouped(error));
	}

	@Override
	public ValidationContext withErrors(String groupName, TextTemplate... errors) {
		ValidationGroup group = ValidationGroup.createFor(groupName);
		return withErrors(errors, (state, error) -> state.withGrouped(group, error));
	}

	private <T> ValidationContext withRule(ValueProvider<T> valueProvider, Rule<T> rule, ContextErrorAppender errorAppender) {
		T value = valueProvider.getValue();
		if (rule.isAcceptableFor(value)) {
			Collection<TextTemplate> errors = rule.check(value);
			if (!errors.isEmpty()) {
				return errorAppender.withError(this, errors.toArray(new TextTemplate[errors.size()]));
			}
		}
		return this;
	}

	private ValidationContext withErrors(TextTemplate[] errors, StateErrorAppender errorAppender) {
		ValidationState resultState = this.validationState;
		for (TextTemplate error : errors) {
			resultState = errorAppender.withError(resultState, error);
		}
		return new DefaultValidationContext(this.group, this.derivation, resultState);
	}

	private ValidationContext validate(ValidationGroup group, Validatable validatable) {
		return validate(group, validatable, DerivationPolicy.DERIVE_STATE);
	}
	
	private ValidationContext validate(ValidationGroup group, Validatable validatable, DerivationPolicy derivationPolicy) {
		ValidationContext subContext = validatable.validate(new DefaultValidationContext(group, derivationPolicy, new ValidationState()));
		return merge(subContext);
	}
	
	private ValidationContext merge(ValidationContext subContext) {
		return new DefaultValidationContext(this.group, this.derivation, this.validationState.merge(subContext.getState()));
	}

	private interface ContextErrorAppender {
		public ValidationContext withError(ValidationContext currentState, TextTemplate... errors);
	}

	private interface StateErrorAppender {
		public ValidationState withError(ValidationState currentState, TextTemplate error);
	}
	
	public static final ValidationService createValidationService() {
		return new ValidationService() {
			@Override
			public <V> ValidationState validate(V validatable) {
				if (validatable instanceof Validatable) {
					return validate((Validatable) validatable);
				}
				return new ValidationState();
			}
			
			private ValidationState validate(Validatable validatable) {
				return validatable.validate(new DefaultValidationContext()).getState();
			}
		};
	}
}
