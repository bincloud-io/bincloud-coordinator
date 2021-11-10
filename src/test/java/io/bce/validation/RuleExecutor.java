package io.bce.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import io.bce.text.TextTemplate;
import io.bce.validation.ValidationContext.Rule;
import io.bce.validation.ValidationContext.Validatable;
import lombok.Getter;

public class RuleExecutor<V extends Validatable> {
	private V validatable;
	private Rule<V> validationRule;

	public RuleExecutionReport probe() {
		try {
			return new RuleExecutionReport(validationRule.check(validatable));
		} catch (Throwable error) {
			return new RuleExecutionReport(error);
		}
	}

	public static final class RuleExecutionReport {
		@Getter
		private Optional<Throwable> thrownError;
		@Getter
		private Collection<TextTemplate> ruleResult = new ArrayList<>();

		private RuleExecutionReport(Collection<TextTemplate> errorMessages) {
			super();
			this.ruleResult.addAll(errorMessages);
			this.thrownError = Optional.empty();
		}

		private RuleExecutionReport(Throwable thrownError) {
			super();
			this.thrownError = Optional.of(thrownError);
		}

		public boolean completedWithError() {
			return thrownError.isPresent();
		}

		public boolean completedWithoutError() {
			return !completedWithError();
		}

		public <E extends Throwable> boolean completedWith(Class<E> errorType) {
			return thrownError.map(error -> errorType.isInstance(error)).orElse(false);
		}

		public boolean ruleIsPassed() {
			return ruleResult.isEmpty();
		}

		public boolean ruleIsFailed() {
			return !ruleIsPassed();
		}

		public Collection<String> getErrorTexts() {
			return this.ruleResult.stream().map(TextTemplate::toString).collect(Collectors.toList());
		}

		public boolean contains(TextTemplate textMessage) {
			return ruleResult.contains(textMessage);
		}

		public boolean contains(Collection<TextTemplate> textMessage) {
			return this.ruleResult.containsAll(textMessage);
		}
	}
}
