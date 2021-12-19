package io.bcs.fileserver.domain.errors;

import io.bce.domain.errors.ApplicationException;
import io.bcs.fileserver.domain.Constants;

/**
 * This exception notifies about exceptional case, where the file content has been already uploaded
 * and couldn't be uploaded again.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class ContentUploadedException extends ApplicationException {
  private static final long serialVersionUID = -7708271435737322280L;

  /**
   * Create exception.
   */
  public ContentUploadedException() {
    super(Constants.CONTEXT, ErrorSeverity.BUSINESS, Constants.CONTENT_IS_UPLOADED_ERROR,
        "File content has been already uploaded!");
  }
}
