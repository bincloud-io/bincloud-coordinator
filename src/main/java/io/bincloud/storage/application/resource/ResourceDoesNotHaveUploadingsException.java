package io.bincloud.storage.application.resource;

import io.bincloud.storage.domain.model.resource.ResourceManagementException;

public class ResourceDoesNotHaveUploadingsException extends ResourceManagementException {
	private static final long serialVersionUID = -7004351298684061160L;
	public static final Long ERROR_CODE = 3L;
	
	public ResourceDoesNotHaveUploadingsException() {
		super(Severity.INCIDENT, ERROR_CODE, "Resource doesn't have uploadings");
	}
}
