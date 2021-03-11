package io.bincloud.storage.domain.model.file;

public class FileNotExistException extends FileManagementException {
	private static final long serialVersionUID = -7269219671623350232L;
	public static final Long ERROR_CODE = 1L;
	
	public FileNotExistException() {
		super(Severity.INCIDENT, ERROR_CODE, "File not exists.");
	}
}
