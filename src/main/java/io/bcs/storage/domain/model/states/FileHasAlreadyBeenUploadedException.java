package io.bcs.storage.domain.model.states;

import io.bcs.storage.domain.model.Constants;

public class FileHasAlreadyBeenUploadedException extends FileManagementException {
	private static final long serialVersionUID = 4910914343298985112L;
	public static final ErrorCode ERROR_CODE = Constants.FILE_HAS_ALREADY_BEEN_UPLOADED_EXCEPTION;
	
	public FileHasAlreadyBeenUploadedException() {
		super(ERROR_CODE, "File has already been uploaded and it is distributioning");
	}
}
