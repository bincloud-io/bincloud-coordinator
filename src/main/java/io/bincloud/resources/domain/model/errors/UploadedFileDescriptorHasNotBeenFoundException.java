package io.bincloud.resources.domain.model.errors;

import io.bincloud.resources.domain.model.Constants;

public class UploadedFileDescriptorHasNotBeenFoundException extends ResourceManagementException {
	private static final long serialVersionUID = -1396238065454078128L;
	public static final Long ERROR_CODE = Constants.UPLOADED_FILE_DESCRIPTOR_HAS_NOT_BEEN_FOUND_ERROR;

	public UploadedFileDescriptorHasNotBeenFoundException() {
		super(Severity.INCIDENT, ERROR_CODE, "Successfully uploaded file descriptor hasn't been found.");
	}
}
