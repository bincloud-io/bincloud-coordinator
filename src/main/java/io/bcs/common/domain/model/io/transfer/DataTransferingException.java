package io.bcs.common.domain.model.io.transfer;

import io.bcs.common.domain.model.io.InputOutputException;

public class DataTransferingException extends InputOutputException {
	private static final long serialVersionUID = 8769350037770641135L;
	public static final ErrorCode ERROR_CODE = ErrorCode.createFor(1L);
	
	public DataTransferingException(String message) {
		super(ERROR_CODE, message);
	}
}
