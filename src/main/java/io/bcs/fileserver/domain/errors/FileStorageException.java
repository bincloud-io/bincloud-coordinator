package io.bcs.fileserver.domain.errors;

import io.bce.domain.errors.ApplicationException;
import io.bcs.fileserver.Constants;

/**
 * This exception notifies about exceptional case, where something went wrong during file IO
 * operation into the file storage.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class FileStorageException extends ApplicationException {
  private static final long serialVersionUID = -7436932297582987110L;

  /**
   * Create exception.
   *
   * @param cause The reason of this exception
   */
  public FileStorageException(Throwable cause) {
    super(Constants.CONTEXT, ErrorSeverity.INCIDENT, Constants.FILE_STORAGE_INCIDENT_ERROR,
        cause.getMessage());
    initCause(cause);
  }
}
