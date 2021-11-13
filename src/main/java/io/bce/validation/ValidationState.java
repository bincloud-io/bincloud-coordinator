package io.bce.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@EqualsAndHashCode
public final class ValidationState {
	private final Map<ValidationGroup, Collection<ErrorMessage>> errorMessages = new HashMap<>();

	private ValidationState(ValidationState prototype) {
		this.errorMessages.putAll(prototype.errorMessages);
	}

	public ErrorState getErrorState() {
		return new DefaultErrorState(getUnmodifiableMessagesCollection(getUngroupedErrors()),
				getUnmodifiableGroupedMessages(getGroupedErrors()));
	}

	/**
	 * Check that the validation state has not errors
	 * 
	 * @return True if valid or false otherwise
	 */
	public boolean isValid() {
		return errorMessages.values().stream().allMatch(Collection::isEmpty);
	}

	/**
	 * Create derived validation state with ungrouped error
	 * 
	 * @param errorMessage The violation descriptor
	 * @return The derived validation state
	 */
	public ValidationState withUngrouped(ErrorMessage errorMessage) {
		return withGrouped(ValidationGroup.UNGROUPED, errorMessage);
	}

	/**
	 * Create derived validation state with grouped error
	 * 
	 * @param group        The error group
	 * @param errorMessage The violation descriptor
	 * @return The derived validation state
	 */
	public ValidationState withGrouped(ValidationGroup group, ErrorMessage errorMessage) {
		ValidationState result = new ValidationState(this);
		result.addGroupedError(group, errorMessage);
		return result;
	}

	/**
	 * Merge the current validation state with another state.
	 * 
	 * @param stateToMerge The state to merge
	 * @return The merged validation state
	 */
	public ValidationState merge(ValidationState stateToMerge) {
		ValidationState result = new ValidationState(this);
		result.addErrors(stateToMerge.errorMessages);
		return result;
	}

	/**
	 * Derive all messages of the validation state from the specified group name.
	 * All ungrouped messages will have group the same as the specified group name.
	 * 
	 * @param group The group name
	 * @return The derived validation state
	 */
	public ValidationState asSubgroup(ValidationGroup group) {
		ValidationState derived = new ValidationState();
		derived.addErrors(createDerivedGroups(group, this.errorMessages));
		return derived;
	}

	/**
	 * Derive grouped messages of the validation state from the specified group
	 * name. Ungrouped messages won't converted to the grouped messages.
	 * 
	 * @param group The group name
	 * @return The derived validation state
	 */
	public ValidationState asDerivedFrom(ValidationGroup group) {
		ValidationState derived = new ValidationState();
		derived.addUngroupedErrors(getUngroupedErrors());
		derived.addErrors(createDerivedGroups(group, getGroupedErrors()));
		return derived;
	}

	private Map<ValidationGroup, Collection<ErrorMessage>> createDerivedGroups(ValidationGroup baseGroup,
			Map<ValidationGroup, Collection<ErrorMessage>> errorMessages) {

		return errorMessages.entrySet().stream()
				.collect(Collectors.toMap(entry -> baseGroup.deriveWith(entry.getKey()), Entry::getValue));
	}

	private Collection<ErrorMessage> getUngroupedErrors() {
		if (errorMessages.containsKey(ValidationGroup.UNGROUPED)) {
			return errorMessages.get(ValidationGroup.UNGROUPED);
		}
		return Collections.emptyList();
	}

	private Map<ValidationGroup, Collection<ErrorMessage>> getGroupedErrors() {
		return errorMessages.entrySet().stream().filter(entry -> !ValidationGroup.UNGROUPED.equals(entry.getKey()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	private Map<String, Collection<ErrorMessage>> getUnmodifiableGroupedMessages(
			Map<ValidationGroup, Collection<ErrorMessage>> messages) {
		return messages.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toString(),
				entry -> getUnmodifiableMessagesCollection(entry.getValue())));
	}

	private Collection<ErrorMessage> getUnmodifiableMessagesCollection(Collection<ErrorMessage> messages) {
		return Collections.unmodifiableList(new ArrayList<>(messages));
	}

	private void addErrors(Map<ValidationGroup, Collection<ErrorMessage>> errors) {
		errors.entrySet().stream().forEach(entry -> {
			ValidationGroup group = entry.getKey();
			entry.getValue().stream().forEach(message -> addGroupedError(group, message));
		});
	}

	private void addUngroupedErrors(Collection<ErrorMessage> ungroupedMessages) {
		this.errorMessages.put(ValidationGroup.UNGROUPED, ungroupedMessages);
	}

	private void addGroupedError(ValidationGroup group, ErrorMessage message) {
		if (!errorMessages.containsKey(group)) {
			this.errorMessages.put(group, new LinkedHashSet<>());
		}
		errorMessages.get(group).add(message);
	}

	@ToString
	@EqualsAndHashCode
	@AllArgsConstructor
	private static class DefaultErrorState implements ErrorState {
		private Collection<ErrorMessage> ungroupedErrors;
		private Map<String, Collection<ErrorMessage>> groupedErrors;

		@Override
		public Collection<ErrorMessage> getUngroupedErrors() {
			return Collections.unmodifiableCollection(ungroupedErrors);
		}

		@Override
		public Collection<GroupedError> getGroupedErrors() {
			return Collections.unmodifiableCollection(
					groupedErrors.entrySet().stream().map(DefaultGroupedError::new).collect(Collectors.toSet()));
		}
	}

	@ToString
	@AllArgsConstructor
	@EqualsAndHashCode(onlyExplicitlyIncluded = true)
	private static final class DefaultGroupedError implements GroupedError {
		private Entry<String, Collection<ErrorMessage>> entry;

		@Override
		@EqualsAndHashCode.Include
		public String getGroupName() {
			return entry.getKey();
		}

		@Override
		@EqualsAndHashCode.Include
		public Collection<ErrorMessage> getMessages() {
			return Collections.unmodifiableList(new ArrayList<>(entry.getValue()));
		}
	}

	public interface ErrorState {
		/**
		 * Get the ungrouped errors
		 * 
		 * @return The ungrouped errors
		 */
		public Collection<ErrorMessage> getUngroupedErrors();

		/**
		 * Get the grouped errors
		 * 
		 * @return The grouped errors
		 */
		public Collection<GroupedError> getGroupedErrors();
	}

	public interface GroupedError {
		/**
		 * Get grouped error group name
		 * 
		 * @return The group name
		 */
		public String getGroupName();

		/**
		 * Get grouped error messages
		 * 
		 * @return The grouped errors messages collection
		 */
		public Collection<ErrorMessage> getMessages();
	}
}
