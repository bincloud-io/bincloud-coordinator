package io.bcs.storage.domain.model.errors;

import io.bcs.storage.domain.model.Constants;

public class UnsatisfiableRangeFormatException extends FileManagementException {
	private static final long serialVersionUID = 7769850795657374683L;
	public static final ErrorCode ERROR_CODE = Constants.UNSATISFIABLE_RANGE_FORMAT_ERROR;
	
	public UnsatisfiableRangeFormatException() {
		super(ErrorSeverity.BUSINESS, ERROR_CODE, "File range has incorrect format");
	}
}
