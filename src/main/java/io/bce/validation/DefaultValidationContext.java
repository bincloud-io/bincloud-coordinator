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
	
	@Override
	public ValidationState getState() {
		return this.derivation.deriveState(group, validationState);
	}

	@Override
	public ValidationContext validate(Validatable validatable) {
		return validate(ValidationGroup.UNGROUPED, validatable);
	}
	@Override
	public ValidationContext validate(Validatable validatable, DerivationPolicy derivationPolicy) {
		return validate(ValidationGroup.UNGROUPED, validatable, derivationPolicy);
	}

	@Override
	public ValidationContext validate(String groupName, Validatable validatable) {
		return validate(ValidationGroup.createFor(groupName), validatable);
	}

	@Override
	public ValidationContext validate(String groupName, Validatable validatable, DerivationPolicy derivationPolicy) {
		return validate(ValidationGroup.createFor(groupName), validatable, derivationPolicy);
	}

	@Override
	public <T> ValidationContext validate(String groupName, Collection<T> collection) {
		Long index = 0L;
		DefaultValidationContext context = this;
		for (T value : collection) {
			if (value instanceof Validatable) {
				context = context.validate(createIndexedGroup(groupName, index), (Validatable) value);
			}
			index++;
		}
		return context;
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

	private <T> ValidationContext withRule(ValueProvider<T> valueProvider, Rule<T> rule,
			ContextErrorAppender errorAppender) {
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

	private DefaultValidationContext validate(ValidationGroup group, Validatable validatable) {
		return validate(group, validatable, DerivationPolicy.DERIVE_STATE);
	}

	private DefaultValidationContext validate(ValidationGroup group, Validatable validatable,
			DerivationPolicy derivationPolicy) {
		ValidationContext subContext = validatable
				.validate(new DefaultValidationContext(group, derivationPolicy, new ValidationState()));
		return merge(subContext);
	}

	private DefaultValidationContext merge(ValidationContext subContext) {
		return new DefaultValidationContext(this.group, this.derivation,
				this.validationState.merge(subContext.getState()));
	}
	
	private ValidationGroup createIndexedGroup(String groupName, Long index) {
		ValidationGroup indexGroup = ValidationGroup.createFor(String.format("[%s]", index));
		return ValidationGroup.createFor(groupName).deriveWith(indexGroup);
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
