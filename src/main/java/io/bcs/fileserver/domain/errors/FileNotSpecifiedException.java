package io.bcs.fileserver.domain.errors;

import io.bce.domain.errors.ApplicationException;
import io.bcs.fileserver.domain.Constants;

/**
 * This exception notifies about exceptional case, where the file, over which an operation is going
 * to be performed, wasn't specified.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class FileNotSpecifiedException extends ApplicationException {
  private static final long serialVersionUID = 5455765854329053327L;

  /**
   * Create exception.
   */
  public FileNotSpecifiedException() {
    super(Constants.CONTEXT, ErrorSeverity.BUSINESS, Constants.FILE_IS_NOT_SPECIFIED,
        "Wrong download URL");
  }
}
