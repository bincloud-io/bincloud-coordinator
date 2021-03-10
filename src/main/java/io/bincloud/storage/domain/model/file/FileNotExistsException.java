package io.bincloud.storage.domain.model.file;

public class FileNotExistsException extends FileManagementException {
	private static final long serialVersionUID = -7269219671623350232L;
	public static final Long ERROR_CODE = 1L;
	
	public FileNotExistsException() {
		super(ERROR_CODE, "File not exists.");
	}
}
