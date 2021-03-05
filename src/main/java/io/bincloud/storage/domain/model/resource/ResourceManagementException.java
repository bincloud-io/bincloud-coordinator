package io.bincloud.storage.domain.model.resource;

import io.bincloud.common.ApplicationException;
import lombok.NonNull;

public class ResourceManagementException extends ApplicationException {
	private static final long serialVersionUID = 7203245717827459184L;
	public static final String CONTEXT = "STORAGE__RESOURCE_MANAGEMENT";
	
	public ResourceManagementException(
			@NonNull Severity severity, 
			@NonNull Long errorNumber,
			String message) {
		super(severity, CONTEXT, errorNumber, message);
	}
}
