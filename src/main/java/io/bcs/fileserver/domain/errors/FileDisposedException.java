package io.bcs.fileserver.domain.errors;

import io.bce.domain.errors.ApplicationException;
import io.bcs.fileserver.Constants;

/**
 * This exception notifies about exceptional case, where an operation is tried to be performed over
 * disposed file entity.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class FileDisposedException extends ApplicationException {
  private static final long serialVersionUID = 3846153632008865728L;

  /**
   * Create exception.
   */
  public FileDisposedException() {
    super(Constants.CONTEXT, ErrorSeverity.BUSINESS, Constants.FILE_IS_DISPOSED_ERROR,
        "File has already been disposed!");
  }
}
