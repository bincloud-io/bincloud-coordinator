package io.bincloud.storage.domain.model.file.errors;

public class FileAlreadyExistsException extends FileManagementException {
	private static final long serialVersionUID = -1089968967833489801L;
	public static final Long ERROR_CODE = 2L;
	
	public FileAlreadyExistsException() {
		super(ERROR_CODE, "File already exists.");
	}
}
