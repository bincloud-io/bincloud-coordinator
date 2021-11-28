package io.bcs.domain.model.file;

import io.bce.domain.errors.ApplicationException;
import io.bcs.domain.model.Constants;

public class FileStorageException extends ApplicationException {
    private static final long serialVersionUID = -7436932297582987110L;

    public FileStorageException(Throwable cause) {
        super(Constants.CONTEXT, ErrorSeverity.INCIDENT, Constants.FILE_STORAGE_INCIDENT_ERROR, cause.getMessage());
        initCause(cause);
    }
}
