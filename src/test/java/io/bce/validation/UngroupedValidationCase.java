package io.bce.validation;

import java.util.Collection;

import io.bce.validation.ValidationContext.Validatable;

public class UngroupedValidationCase extends ValidationCase {
	public UngroupedValidationCase(Validatable validatable, ExpectedResult expectedResult,
			Collection<String> expectedMessages) {
		super(validatable, expectedResult, expectedMessages);
	}

	@Override
	public boolean containsExpectedErrorMessages() {
		return report.containsUngroupedErrors(getExpectedErrorMessages());
	}

}
