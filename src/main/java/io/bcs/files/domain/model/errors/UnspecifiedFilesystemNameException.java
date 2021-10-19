package io.bcs.files.domain.model.errors;

import io.bcs.files.domain.model.Constants;

public class UnspecifiedFilesystemNameException extends FileManagementException {	
	private static final long serialVersionUID = -6241501905585818399L;
	private static final Long ERROR_CODE = Constants.UNSPECIFIED_FILESYSTEM_NAME_ERROR;

	public UnspecifiedFilesystemNameException() {
		super(Severity.BUSINESS, ERROR_CODE, "File revision must be fully specified.");
	}
}
