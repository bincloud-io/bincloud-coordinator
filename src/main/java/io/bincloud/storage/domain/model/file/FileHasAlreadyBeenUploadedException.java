package io.bincloud.storage.domain.model.file;

public class FileHasAlreadyBeenUploadedException extends FileManagementException {
	private static final long serialVersionUID = 4910914343298985112L;
	public static final Long ERROR_CODE = 3L;
	
	public FileHasAlreadyBeenUploadedException() {
		super(ERROR_CODE, "File has already been uploaded and it is distributioning");
	}
}
