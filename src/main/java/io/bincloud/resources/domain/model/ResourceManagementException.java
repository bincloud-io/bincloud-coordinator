package io.bincloud.resources.domain.model;

import io.bincloud.common.domain.model.error.ApplicationException;

public class ResourceManagementException extends ApplicationException {
	private static final long serialVersionUID = 7203245717827459184L;
	
	public ResourceManagementException(
			Severity severity, 
			Long errorNumber,
			String message) {
		super(severity, Constants.CONTEXT, errorNumber, message);
	}
}
