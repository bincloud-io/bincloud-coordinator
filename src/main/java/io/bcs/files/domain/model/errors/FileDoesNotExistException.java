package io.bcs.files.domain.model.errors;

public class FileDoesNotExistException extends FileManagementException {
	private static final long serialVersionUID = -7269219671623350232L;
	public static final Long ERROR_CODE = 1L;
	
	public FileDoesNotExistException() {
		this(Severity.BUSINESS);	
	}
	
	public FileDoesNotExistException(Severity severity) {
		super(severity, ERROR_CODE, "File revision does not exist.");	
	}
}
