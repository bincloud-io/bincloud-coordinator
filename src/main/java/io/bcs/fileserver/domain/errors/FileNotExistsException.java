package io.bcs.fileserver.domain.errors;

import io.bce.domain.errors.ApplicationException;
import io.bcs.fileserver.Constants;

/**
 * This exception notifies about exceptional case, where the requested file doesn't exist, but
 * should be existed for requested operation.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class FileNotExistsException extends ApplicationException {
  private static final long serialVersionUID = -3843785521240074860L;

  /**
   * Create exception.
   */
  public FileNotExistsException() {
    super(Constants.CONTEXT, ErrorSeverity.BUSINESS, Constants.FILE_NOT_EXIST_ERROR,
        "File not found!");
  }
}
