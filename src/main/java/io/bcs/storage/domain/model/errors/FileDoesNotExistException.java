package io.bcs.storage.domain.model.errors;

import io.bcs.storage.domain.model.Constants;

public class FileDoesNotExistException extends FileManagementException {
	private static final long serialVersionUID = -7269219671623350232L;
	public static final ErrorCode ERROR_CODE = Constants.FILE_DOES_NOT_EXIST_EXCEPTION;
	
	public FileDoesNotExistException() {
		this(ErrorSeverity.BUSINESS);	
	}
	
	public FileDoesNotExistException(ErrorSeverity severity) {
		super(severity, ERROR_CODE, "File revision does not exist.");	
	}
}
