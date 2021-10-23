package io.bcs.storage.domain.model.errors;

import io.bcs.storage.domain.model.Constants;
import io.bcs.storage.domain.model.states.FileManagementException;

public class UnspecifiedRevisionNameException extends FileManagementException {	
	private static final long serialVersionUID = -6241501905585818399L;
	private static final ErrorCode ERROR_CODE = Constants.UNSPECIFIED_REVISION_NAME_ERROR;

	public UnspecifiedRevisionNameException() {
		super(ErrorSeverity.BUSINESS, ERROR_CODE, "File revision must be fully specified.");
	}
}
