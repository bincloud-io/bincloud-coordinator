package io.bcs.domain.model.file;

import io.bce.domain.errors.ApplicationException;
import io.bcs.domain.model.Constants;

public class FileNotExistsException extends ApplicationException {
    private static final long serialVersionUID = -3843785521240074860L;

    public FileNotExistsException() {
        super(Constants.CONTEXT, ErrorSeverity.BUSINESS, Constants.FILE_NOT_EXIST_ERROR, "File not found!");
    }
}
