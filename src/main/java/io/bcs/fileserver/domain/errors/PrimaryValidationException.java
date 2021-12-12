package io.bcs.fileserver.domain.errors;

import io.bce.domain.errors.ValidationException;
import io.bce.validation.ValidationState;
import io.bcs.fileserver.Constants;

/**
 * This exception notifies about exceptional case, where an input data primary validation isn't
 * passed.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class PrimaryValidationException extends ValidationException {
  private static final long serialVersionUID = 4415785977776372011L;

  /**
   * Create exception.
   *
   * @param validationState The validation state
   */
  public PrimaryValidationException(ValidationState validationState) {
    super(Constants.CONTEXT, Constants.PRIMARY_VALIDATION_ERROR, validationState);
  }
}
