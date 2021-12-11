package io.bcs.domain.model.file;

import io.bce.domain.errors.ApplicationException;
import io.bcs.domain.model.Constants;

public class UnsatisfiableRangeFormatException extends ApplicationException {
  private static final long serialVersionUID = -5482382949706358492L;

  public UnsatisfiableRangeFormatException() {
    super(Constants.CONTEXT, ErrorSeverity.BUSINESS, Constants.UNSATISFIABLE_RANGES_FORMAT_ERROR,
        "Range start value should be greater than range end value and range start value should be positive!");
  }
}