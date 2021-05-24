package io.bincloud.common.domain.model.io.transfer;

import io.bincloud.common.domain.model.io.InputOutputException;

public class DataTransferingException extends InputOutputException {
	private static final long serialVersionUID = 8769350037770641135L;
	public static final Long ERROR_CODE = 1L;
	
	public DataTransferingException(String message) {
		super(ERROR_CODE, message);
	}
}
