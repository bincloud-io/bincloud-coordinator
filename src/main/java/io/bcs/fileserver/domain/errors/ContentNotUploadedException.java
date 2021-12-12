package io.bcs.fileserver.domain.errors;

import io.bce.domain.errors.ApplicationException;
import io.bcs.fileserver.Constants;

/**
 * This exception notifies about exceptional case, where the requested file content hasn't been
 * uploaded yet.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class ContentNotUploadedException extends ApplicationException {
  private static final long serialVersionUID = -1808721239833705507L;

  /**
   * Create exception.
   */
  public ContentNotUploadedException() {
    super(Constants.CONTEXT, ErrorSeverity.BUSINESS, Constants.CONTENT_IS_NOT_UPLOADED_ERROR,
        "File content has not been uploaded yet!");
  }
}
