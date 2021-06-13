package io.bincloud.files.domain.model.errors;

import io.bincloud.common.domain.model.error.ApplicationException;

public abstract class FileManagementException extends ApplicationException {
	private static final long serialVersionUID = 7641731127838902724L;
	public static final String CONTEXT = "STORAGE__FILE_MANAGEMENT";
	
	public FileManagementException(Severity severity, Long errorNumber, String message) {
		super(severity, CONTEXT, errorNumber, message);
	}
	
	public FileManagementException(Long errorNumber, String message) {
		this(Severity.BUSINESS, errorNumber, message);
	}
}
