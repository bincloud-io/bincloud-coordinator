package io.bcs.storage.domain.model.states;

import io.bcs.storage.domain.model.Constants;

public class FileAlreadyExistsException extends FileManagementException {
	private static final long serialVersionUID = -1089968967833489801L;
	public static final ErrorCode ERROR_CODE = Constants.FILE_ALREADY_EXISTS_EXCEPTION;
	
	public FileAlreadyExistsException() {
		super(ERROR_CODE, "File already exists.");
	}
}
