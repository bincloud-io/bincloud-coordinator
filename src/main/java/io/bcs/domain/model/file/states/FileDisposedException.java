package io.bcs.domain.model.file.states;

import io.bce.domain.errors.ApplicationException;
import io.bcs.domain.model.Constants;

public class FileDisposedException extends ApplicationException {
    private static final long serialVersionUID = 3846153632008865728L;

    public FileDisposedException() {
        super(Constants.CONTEXT, ErrorSeverity.BUSINESS, Constants.FILE_IS_DISPOSED_ERROR,
                "File has already been disposed!");
    }
}
