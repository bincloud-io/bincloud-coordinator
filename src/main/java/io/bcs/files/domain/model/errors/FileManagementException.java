package io.bcs.files.domain.model.errors;

import io.bcs.common.domain.model.error.ApplicationException;
import io.bcs.files.domain.model.Constants;

public abstract class FileManagementException extends ApplicationException {
	private static final long serialVersionUID = 7641731127838902724L;
	public static final String CONTEXT = Constants.CONTEXT;
	
	public FileManagementException(Severity severity, Long errorNumber, String message) {
		super(severity, CONTEXT, errorNumber, message);
	}
	
	public FileManagementException(Long errorNumber, String message) {
		this(Severity.BUSINESS, errorNumber, message);
	}
}
