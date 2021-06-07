package io.bincloud.storage.domain.model.resource;

public class ResourceDoesNotHaveUploadingsException extends ResourceManagementException {
	private static final long serialVersionUID = -7004351298684061160L;
	public static final Long ERROR_CODE = Constants.RESOURCE_DOES_NOT_HAVE_UPLOADINGS_ERROR;
	
	public ResourceDoesNotHaveUploadingsException() {
		super(Severity.INCIDENT, ERROR_CODE, "Resource doesn't have uploadings");
	}
}
