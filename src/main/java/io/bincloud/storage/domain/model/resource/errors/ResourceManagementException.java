package io.bincloud.storage.domain.model.resource.errors;

import io.bincloud.common.domain.model.error.ApplicationException;
import io.bincloud.storage.domain.model.resource.Constants;

public class ResourceManagementException extends ApplicationException {
	private static final long serialVersionUID = 7203245717827459184L;
	public ResourceManagementException(
			Severity severity, 
			Long errorNumber,
			String message) {
		super(severity, Constants.CONTEXT, errorNumber, message);
	}
}
