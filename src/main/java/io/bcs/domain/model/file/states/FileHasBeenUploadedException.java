package io.bcs.domain.model.file.states;

import io.bce.domain.errors.ApplicationException;
import io.bcs.domain.model.Constants;

public class FileHasBeenUploadedException extends ApplicationException {
    private static final long serialVersionUID = -7708271435737322280L;

    public FileHasBeenUploadedException() {
        super(Constants.CONTEXT, ErrorSeverity.BUSINESS, Constants.FILE_HAS_BEEN_UPLOADED_ERROR,
                "File content has been already uploaded!");
    }
}
