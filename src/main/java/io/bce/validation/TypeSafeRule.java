package io.bce.validation;

import io.bce.validation.ValidationContext.Rule;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 * This class is the base rule, which is acceptable for specified value type only.
 *
 * @author Dmitry Mikhaylenko
 *
 * @param <T> The acceptable value type name
 */
@RequiredArgsConstructor
public abstract class TypeSafeRule<T> implements Rule<T> {
  protected final Class<T> type;

  @Override
  public final boolean isAcceptableFor(T value) {
    return Optional.ofNullable(value).map(v -> type.isInstance(v)).orElse(true);
  }
}
