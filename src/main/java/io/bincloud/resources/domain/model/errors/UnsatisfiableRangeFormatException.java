package io.bincloud.resources.domain.model.errors;

import io.bincloud.resources.domain.model.Constants;

public class UnsatisfiableRangeFormatException extends ResourceManagementException {
	private static final long serialVersionUID = 7769850795657374683L;
	public static final Long ERROR_CODE = Constants.UNSATISFIABLE_RANGE_FORMAT_ERROR;
	
	public UnsatisfiableRangeFormatException() {
		super(Severity.BUSINESS, ERROR_CODE, "File range has incorrect format");
	}
}
