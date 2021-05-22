package io.bincloud.storage.domain.model.resource;

import io.bincloud.common.domain.model.error.ApplicationException;
import lombok.NonNull;

public class ResourceManagementException extends ApplicationException {
	private static final long serialVersionUID = 7203245717827459184L;
	public ResourceManagementException(
			@NonNull Severity severity, 
			@NonNull Long errorNumber,
			String message) {
		super(severity, Constants.CONTEXT, errorNumber, message);
	}
}
