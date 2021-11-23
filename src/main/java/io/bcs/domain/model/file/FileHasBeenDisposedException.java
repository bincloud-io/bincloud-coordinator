package io.bcs.domain.model.file;

import io.bce.domain.errors.ApplicationException;
import io.bcs.domain.model.Constants;

public class FileHasBeenDisposedException extends ApplicationException {
    private static final long serialVersionUID = 3846153632008865728L;

    public FileHasBeenDisposedException() {
        super(Constants.CONTEXT, ErrorSeverity.BUSINESS, Constants.FILE_IS_DISPOSED_ERROR,
                "File has already been disposed!");
    }
}
