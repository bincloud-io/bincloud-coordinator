package io.bincloud.storage.domain.model.file.errors;

public class FileHasAlreadyBeenDisposedException extends FileManagementException {
	private static final long serialVersionUID = -5270755575741209121L;
	public static final Long ERROR_CODE = 6L;
	
	public FileHasAlreadyBeenDisposedException() {
		super(ERROR_CODE, "File has already been disposed.");
	}
}
