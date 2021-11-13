package io.bce.validation

import java.util.stream.Collectors

import io.bce.validation.ValidationState.ErrorState
import io.bce.validation.ValidationState.GroupedError


class GroupedErrors {
	private String groupName;
	private ErrorState errorState;
	
	public GroupedErrors(String groupName, ErrorState errorState) {
		super();
		this.groupName = groupName;
		this.errorState = errorState;
	}
	
	public Collection<ErrorMessage> getErrors() {
		return errorState.getGroupedErrors().stream()
			.filter({error -> error.getGroupName().equals(groupName)})
			.flatMap({error -> error.getMessages().stream()})
			.collect(Collectors.toSet())
	}
	
	public static Collection<ErrorMessage> errorsOf(String groupName, ErrorState errorState) {
		return new GroupedErrors(groupName, errorState).getErrors();
	}
}
