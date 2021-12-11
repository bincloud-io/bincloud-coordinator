package io.bcs.domain.model;

import io.bce.domain.errors.ValidationException;
import io.bce.validation.ValidationState;

public class PrimaryValidationException extends ValidationException {
  private static final long serialVersionUID = 4415785977776372011L;

  public PrimaryValidationException(ValidationState validationState) {
    super(Constants.CONTEXT, Constants.PRIMARY_VALIDATION_ERROR, validationState);
  }
}
