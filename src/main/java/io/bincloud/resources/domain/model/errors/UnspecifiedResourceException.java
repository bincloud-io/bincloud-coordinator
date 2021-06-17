package io.bincloud.resources.domain.model.errors;

import io.bincloud.resources.domain.model.Constants;

public class UnspecifiedResourceException extends ResourceManagementException {
	private static final long serialVersionUID = -6158040825883289420L;
	public static final Long ERROR_CODE = Constants.UNSPECIFIED_RESOURCE_ERROR;
	public static final String ERROR_MESSAGE = "Resource must be specified";
	
	public UnspecifiedResourceException() {
		super(Severity.BUSINESS, ERROR_CODE, ERROR_MESSAGE);
	}

}