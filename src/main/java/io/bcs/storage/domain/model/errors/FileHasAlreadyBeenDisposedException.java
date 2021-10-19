package io.bcs.storage.domain.model.errors;

import io.bcs.storage.domain.model.Constants;

public class FileHasAlreadyBeenDisposedException extends FileManagementException {
	private static final long serialVersionUID = -5270755575741209121L;
	public static final Long ERROR_CODE = Constants.FILE_HAS_ALREADY_BEEN_DISPOSED_EXCEPTION;
	
	public FileHasAlreadyBeenDisposedException() {
		super(ERROR_CODE, "File has already been disposed.");
	}
}
