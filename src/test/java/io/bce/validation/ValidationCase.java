package io.bce.validation;

import java.util.Collection;

import io.bce.validation.ValidationContext.Validatable;
import io.bce.validation.ValidationExecutor.ValidationReport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.ToString.Include;

@ToString(onlyExplicitlyIncluded = true)
public abstract class ValidationCase {
	@Getter
	@Include
	private final Validatable validatableObject;
	protected final ValidationReport report;
	@Getter
	@Include
	private final ExpectedResult expectedRuleResult;
	@Getter
	@Include
	private final Collection<String> expectedErrorMessages;

	public ValidationCase(Validatable validatable, ExpectedResult expectedResult, Collection<String> expectedMessages) {
		super();
		ValidationService validationService = DefaultValidationContext.createValidationService();
		this.validatableObject = validatable;
		this.report = new ValidationExecutor(validatable, validationService).execute();
		this.expectedErrorMessages = expectedMessages;
		this.expectedRuleResult = expectedResult;
	}
	
	public boolean isPassed() {
		return report.isPassed() == shouldBePassed();
	}
	
	private boolean shouldBePassed() {
		return expectedRuleResult.shouldBeValid;
	}
	
	public abstract boolean containsExpectedErrorMessages();

	@AllArgsConstructor
	public enum ExpectedResult {
		PASSED(true), FAILED(false);

		private boolean shouldBeValid;
	}
}
