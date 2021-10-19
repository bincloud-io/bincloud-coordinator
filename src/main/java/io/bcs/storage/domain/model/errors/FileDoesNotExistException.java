package io.bcs.storage.domain.model.errors;

import io.bcs.storage.domain.model.Constants;

public class FileDoesNotExistException extends FileManagementException {
	private static final long serialVersionUID = -7269219671623350232L;
	public static final Long ERROR_CODE = Constants.FILE_DOES_NOT_EXIST_EXCEPTION;
	
	public FileDoesNotExistException() {
		this(Severity.BUSINESS);	
	}
	
	public FileDoesNotExistException(Severity severity) {
		super(severity, ERROR_CODE, "File revision does not exist.");	
	}
}
