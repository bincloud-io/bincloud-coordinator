package io.bcs.domain.model.file;

import io.bce.domain.errors.ApplicationException;
import io.bcs.domain.model.Constants;
import lombok.NonNull;

public class FileNotSpecifiedException extends ApplicationException {
    private static final long serialVersionUID = 5455765854329053327L;

    public FileNotSpecifiedException(@NonNull ErrorCode reason) {
        super(Constants.CONTEXT, ErrorSeverity.BUSINESS, Constants.FILE_IS_NOT_SPECIFIED, "Wrong download URL");
    }
}
