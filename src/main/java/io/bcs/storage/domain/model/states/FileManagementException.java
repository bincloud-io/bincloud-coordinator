package io.bcs.storage.domain.model.states;

import io.bce.domain.BoundedContextId;
import io.bce.domain.errors.ApplicationException;
import io.bcs.storage.domain.model.Constants;

public abstract class FileManagementException extends ApplicationException {
	private static final long serialVersionUID = 7641731127838902724L;
	public static final BoundedContextId CONTEXT = Constants.CONTEXT;
	
	public FileManagementException(ErrorSeverity severity, ErrorCode errorNumber, String message) {
		super(CONTEXT, severity, errorNumber, message);
	}
	
	public FileManagementException(ErrorCode errorNumber, String message) {
		this(ErrorSeverity.BUSINESS, errorNumber, message);
	}
}
