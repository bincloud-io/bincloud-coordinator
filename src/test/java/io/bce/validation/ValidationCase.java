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
	private final ValidationExecutor validationExecutor;
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
		this.validationExecutor = new ValidationExecutor(validatable, validationService);
		this.expectedErrorMessages = expectedMessages;
		this.expectedRuleResult = expectedResult;
	}
	
	public ValidationCaseReport execute() {
		ValidationReport report = validationExecutor.execute();
		return new ValidationCaseReport() {
			@Override
			public boolean isPassed() {
				return report.isPassed() == shouldBePassed();
			}

			@Override
			public boolean containsExpectedErrorMessages() {
				return ValidationCase.this.containsExpectedErrorMessages(report);
			}
		};
	}
	
	
	private boolean shouldBePassed() {
		return expectedRuleResult.shouldBeValid;
	}
	
	protected abstract boolean containsExpectedErrorMessages(ValidationReport validationReport);

	public interface ValidationCaseReport {
		public boolean isPassed();
		public boolean containsExpectedErrorMessages();
	}
	
	@AllArgsConstructor
	public enum ExpectedResult {
		PASSED(true), FAILED(false);

		private boolean shouldBeValid;
	}
}
