package io.bcs.fileserver.domain.validations;

import io.bce.validation.ErrorMessage;
import io.bce.validation.GlobalValidations;
import io.bce.validation.Rules;
import io.bce.validation.ValidationContext;
import io.bce.validation.ValidationContext.Rule;
import io.bce.validation.ValidationContext.Validatable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This validation checks that the value is exists inside a dictionary.
 *
 * @author Dmitry Mikhaylenko
 *
 * @param <T> The value type name
 */
@RequiredArgsConstructor
public abstract class DictionaryValidation<T> implements Validatable {
  private final String ruleAlias;
  @Getter(value = AccessLevel.PRIVATE)
  private final T validatableValue;

  protected static <V> Rule<V> createGlobalRule(Class<V> valueType, ErrorMessage errorMessage,
      DictionaryPredicate<V> predicate) {
    return Rules.match(valueType, errorMessage, predicate::isSatisfiedBy);
  }

  @Override
  public final ValidationContext validate(ValidationContext context) {
    return context.withRule(this::getValidatableValue, GlobalValidations.getRule(ruleAlias));
  }

  /**
   * This interface describes a component, checking that the value is satisfied by condition of a
   * value search inside a dictionary.
   *
   * @author Dmitry Mikhaylenko
   *
   * @param <V> The value type name
   */
  public interface DictionaryPredicate<V> {
    boolean isSatisfiedBy(V value);
  }
}
