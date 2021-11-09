package io.bce.validation;

import java.util.Collection;

import io.bce.text.TextTemplate;

public interface ValidationContext {

	/**
	 * Get the result validation state
	 * 
	 * @return The validation state
	 */
	public ValidationState getState();

	/**
	 * Validate an object, implementing the {@link Validatable} interface
	 * 
	 * @param validatable The validatable object
	 * @return The derived context
	 */
	public ValidationContext validate(Validatable validatable);

	/**
	 * Validate an object, implementing the {@link Validatable} interface using
	 * specified derivation policy
	 * 
	 * @param validatable      The validatable object
	 * @param derivationPolicy The derivation policy
	 * @return The derived context
	 */
	public ValidationContext validate(Validatable validatable, DerivationPolicy derivationPolicy);

	/**
	 * Validate an object, implementing the {@link Validatable} interface, grouped
	 * by a specified group
	 * 
	 * @param groupName   The validation group
	 * @param validatable The validatable object
	 * @return The derived context
	 */
	public ValidationContext validate(String groupName, Validatable validatable);

	/**
	 * Validate an object, implementing the {@link Validatable} interface, grouped
	 * by a specified group, using specified derivation policy
	 * 
	 * @param groupName        The validation group
	 * @param validatable      The validatable object
	 * @param derivationPolicy The derivation policy
	 * @return The derived context
	 */
	public ValidationContext validate(String groupName, Validatable validatable, DerivationPolicy derivationPolicy);

	/**
	 * Validate all objects inside a collection, implementing the
	 * {@link Validatable} interface, grouped by a specified group
	 * 
	 * @param groupName        The validation group
	 * @param collection       The validatable collection
	 * @return The derived context
	 */
	public <T> ValidationContext validate(String groupName, Collection<T> collection);

	/**
	 * Append ungrouped rule checking for the value, obtained by the provider
	 * 
	 * @param <T>           The value type name
	 * @param valueProvider The under validation value provider
	 * @param rule          The rule under validation
	 * @return The validation context
	 */
	<T> ValidationContext withRule(ValueProvider<T> valueProvider, Rule<T> rule);

	/**
	 * Append grouped rule checking for the value, obtained by the provider
	 * 
	 * @param <T>           The value type name
	 * @param groupName     The validation group name
	 * @param valueProvider The under validation value provider
	 * @param rule          The rule under validation
	 * @return The validation context
	 */
	<T> ValidationContext withRule(String groupName, ValueProvider<T> valueProvider, Rule<T> rule);

	/**
	 * Append the ungrouped errors
	 * 
	 * @param <T>
	 * @param errors
	 * @return
	 */
	ValidationContext withErrors(TextTemplate... errors);

	ValidationContext withErrors(String groupName, TextTemplate... errors);

	/**
	 * This interface declares the contract for object validation.
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 */
	public interface Validatable {
		/**
		 * Validate object state inside the context
		 * 
		 * @param context The validation context
		 * @return The derived context with applied validations
		 */
		public ValidationContext validate(ValidationContext context);
	}

	/**
	 * This class enumerates the variants of derivation policies.
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 */
	public static enum DerivationPolicy {
		/**
		 * Derive only groups of the grouped messages excluding ungrouped
		 */
		DERIVE_GROUPES {
			@Override
			protected ValidationState deriveState(ValidationGroup group, ValidationState state) {
				return state.asDerivedFrom(group);
			}
		},
		/**
		 * Derive all state including ungrouped(it will be represented as a grouped
		 * messages of the base group)
		 */
		DERIVE_STATE {
			@Override
			protected ValidationState deriveState(ValidationGroup group, ValidationState state) {
				return state.asSubgroup(group);
			}
		};

		protected abstract ValidationState deriveState(ValidationGroup group, ValidationState state);
	}

	/**
	 * This interface declares the rule of the value providing.
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 * @param <T> The type of value
	 */
	public interface ValueProvider<T> {
		/**
		 * Get the value
		 * 
		 * @return The value
		 */
		public T getValue();
	}

	public interface Rule<T> {
		public boolean isAcceptableFor(T value);

		public Collection<TextTemplate> check(T value);

		public Rule<T> invert();
	}
}