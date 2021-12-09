package io.bce.validation;

import io.bce.validation.ValidationContext.Rule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class executes rule and returns report about rule execution completing.
 *
 * @author Dmitry Mikhaylenko
 *
 * @param <V> The validatable value type
 */
@RequiredArgsConstructor
public class RuleExecutor<V> {
  private final V validatable;
  private final Supplier<Rule<V>> validationRule;

  /**
   * Execute rule.
   *
   * @return The rule execution report
   */
  public RuleExecutionReport execute() {
    try {
      if (validationRule.get().isAcceptableFor(validatable)) {
        return new RuleExecutionReport(true, validationRule.get().check(validatable));
      } else {
        return new RuleExecutionReport(false, Collections.emptyList());
      }

    } catch (Throwable error) {
      return new RuleExecutionReport(error);
    }
  }

  /**
   * This class keeps report information about the rule execution.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public static final class RuleExecutionReport {
    @Getter
    private boolean acceptable;
    @Getter
    private Optional<Throwable> thrownError;
    @Getter
    private Collection<ErrorMessage> ruleResult = new ArrayList<>();

    private RuleExecutionReport(boolean acceptable, Collection<ErrorMessage> errorMessages) {
      super();
      this.acceptable = acceptable;
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
      return completedWithoutError() && ruleResult.isEmpty();
    }

    public boolean ruleIsFailed() {
      return completedWithoutError() && !ruleResult.isEmpty();
    }

    public Collection<String> getErrorTexts() {
      return this.ruleResult.stream().map(ErrorMessage::toString).collect(Collectors.toList());
    }

    public boolean contains(ErrorMessage textMessage) {
      return ruleResult.contains(textMessage);
    }

    public boolean contains(Collection<ErrorMessage> textMessage) {
      return this.ruleResult.containsAll(textMessage);
    }
  }
}
