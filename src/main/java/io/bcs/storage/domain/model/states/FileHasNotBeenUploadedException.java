package io.bcs.storage.domain.model.states;

import io.bcs.storage.domain.model.Constants;

public class FileHasNotBeenUploadedException extends FileManagementException {
	private static final long serialVersionUID = -1077148033501341697L;
	public static final ErrorCode ERROR_CODE = Constants.FILE_HAS_NOT_BEEN_UPLOADED_EXCEPTION;
	
	public FileHasNotBeenUploadedException() {
		super(ERROR_CODE, "File hasn't been uploaded exception");
	}
}
