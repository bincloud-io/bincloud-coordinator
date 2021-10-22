package io.bcs.storage.domain.model.errors;

import io.bcs.storage.domain.model.Constants;
import io.bcs.storage.domain.model.states.FileManagementException;

public class UnspecifiedFilesystemNameException extends FileManagementException {	
	private static final long serialVersionUID = -6241501905585818399L;
	private static final ErrorCode ERROR_CODE = Constants.UNSPECIFIED_FILESYSTEM_NAME_ERROR;

	public UnspecifiedFilesystemNameException() {
		super(ErrorSeverity.BUSINESS, ERROR_CODE, "File revision must be fully specified.");
	}
}
