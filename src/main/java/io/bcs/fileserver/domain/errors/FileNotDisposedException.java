package io.bcs.fileserver.domain.errors;

import io.bce.domain.errors.ApplicationException;
import io.bcs.fileserver.domain.Constants;

/**
 * This error notifies that the file wasn't disposed.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class FileNotDisposedException extends ApplicationException {
  private static final long serialVersionUID = -1070395633832596193L;

  public FileNotDisposedException() {
    super(Constants.CONTEXT, ErrorSeverity.INCIDENT, Constants.FILE_IS_NOT_DISPOSED_ERROR,
        "File was not disposied.");
  }
}
