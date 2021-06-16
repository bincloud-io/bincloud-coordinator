package io.bincloud.resources.domain.model.errors;

import io.bincloud.resources.domain.model.Constants;

public class ResourceDoesNotHaveUploadsException extends ResourceManagementException {
	private static final long serialVersionUID = -7004351298684061160L;
	public static final Long ERROR_CODE = Constants.RESOURCE_DOES_NOT_HAVE_UPLOADINGS_ERROR;
	
	public ResourceDoesNotHaveUploadsException() {
		super(Severity.INCIDENT, ERROR_CODE, "Resource doesn't have uploadings");
	}
}
