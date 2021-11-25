package io.bcs.domain.model.file.states;

import io.bce.domain.errors.ApplicationException;
import io.bcs.domain.model.Constants;

public class ContentNotUploadedException extends ApplicationException {
    private static final long serialVersionUID = -1808721239833705507L;

    public ContentNotUploadedException() {
        super(Constants.CONTEXT, ErrorSeverity.BUSINESS, Constants.CONTENT_IS_NOT_UPLOADED_ERROR,
                "File content has not been uploaded yet!");
    }
}
