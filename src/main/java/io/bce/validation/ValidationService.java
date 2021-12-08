package io.bce.validation;

/**
 * This interface declares the base contract for object validation issue.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface ValidationService {
  public <V> ValidationState validate(V validatable);
}
