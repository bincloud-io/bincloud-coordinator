package io.bce.validation;

import java.util.Collection;

import io.bce.validation.ValidationContext.Validatable;

public class GroupedValidationCase extends ValidationCase {
	private final String group;

	public GroupedValidationCase(Validatable validatable, String group, ExpectedResult expectedResult,
			Collection<String> expectedMessages) {
		super(validatable, expectedResult, expectedMessages);
		this.group = group;
	}

	@Override
	public boolean containsExpectedErrorMessages() {
		return report.containsGroupedErrors(group, getExpectedErrorMessages());
	}
}
