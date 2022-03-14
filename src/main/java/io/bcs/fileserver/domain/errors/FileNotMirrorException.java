package io.bcs.fileserver.domain.errors;

import io.bce.domain.errors.ApplicationException;
import io.bcs.fileserver.domain.Constants;

/**
 * This exception notifies about exceptional case, where a file storage mode should be mirror, but
 * isn't.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class FileNotMirrorException extends ApplicationException {
  private static final long serialVersionUID = -7433805361923789180L;

  public FileNotMirrorException() {
    super(Constants.CONTEXT, ErrorSeverity.BUSINESS, Constants.FILE_IS_NOT_MIRROR_ERROR,
        "Requested file should be mirror.");
  }
}
