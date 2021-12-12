package io.bcs.fileserver.domain.errors;

import io.bce.domain.errors.ApplicationException;
import io.bcs.fileserver.Constants;

/**
 * This exception notifies about exceptional case, where the requested ranges format is
 * unsatisfiable (for example has wrong format or contains incorrect values).
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class UnsatisfiableRangeFormatException extends ApplicationException {
  private static final long serialVersionUID = -5482382949706358492L;
  
  /**
   * Create exception.
   */
  public UnsatisfiableRangeFormatException() {
    super(Constants.CONTEXT, ErrorSeverity.BUSINESS, Constants.UNSATISFIABLE_RANGES_FORMAT_ERROR,
        "Range start value should be greater than range end value and range start "
            + "value should be positive!");
  }
}