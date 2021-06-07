package io.bincloud.common.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import io.bincloud.common.ApplicationException.Severity;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
public class ValidationState {
	private static final String UNGROUPED_MESSAGE_GROUP = "$$__UNGROUPED_MESSAGES__$$";
	private final Map<String, Set<String>> errorMessages = new HashMap<String, Set<String>>();
	
	private ValidationState(ValidationState prototype) {
		this.errorMessages.putAll(prototype.errorMessages);
	}
	
	public ErrorState getErrorState() {
		return new DefaultErrorState(
			getUngroupedErrors(), 
			getGroupedErrors()
		);
	}
	
	public boolean isValid() {
		return errorMessages.isEmpty();
	}
	
	public ValidationState withUngrouped(String message) {
		return withGrouped(UNGROUPED_MESSAGE_GROUP, message);
	}
	
	public ValidationState withGrouped(String group, String message) {
		ValidationState result = new ValidationState(this);
		result.addGroupedError(group, message);
		return result;
	}
	
	public void checkValidState(Severity errorSeverity) {
		if (!isValid()) {
			throw new ValidationException(errorSeverity, this);
		}
	}
	
	@Getter
	@EqualsAndHashCode
	@AllArgsConstructor
	private static class DefaultErrorState implements ErrorState {
		private Collection<String> ungroupedErrors;
		private Map<String, Collection<String>> groupedErrors;
	}

	private Collection<String> getUngroupedErrors() {
		if (errorMessages.containsKey(UNGROUPED_MESSAGE_GROUP)) {
			return getUnmodifiableMessagesCollection(errorMessages.get(UNGROUPED_MESSAGE_GROUP));
		}
		return Collections.emptyList();
	}
	
	private Map<String, Collection<String>> getGroupedErrors() {
		return errorMessages.entrySet().stream()
			.filter(entry -> !UNGROUPED_MESSAGE_GROUP.equals(entry.getKey()))
			.collect(Collectors.toMap(Entry::getKey, entry -> getUnmodifiableMessagesCollection(entry.getValue())));
	}
	
	private Collection<String> getUnmodifiableMessagesCollection(Collection<String> messages) {
		List<String> ungroupedErrors = new ArrayList<String>(messages);
		return Collections.unmodifiableList(ungroupedErrors);
	}
	
	private void addGroupedError(String group, String message) {
		if (!errorMessages.containsKey(group)) {
			this.errorMessages.put(group, new LinkedHashSet<String>());
		}
		errorMessages.get(group).add(message);
	}
	
	public interface ErrorState {
		public Collection<String> getUngroupedErrors();
		public Map<String, Collection<String>> getGroupedErrors();
	}
}
