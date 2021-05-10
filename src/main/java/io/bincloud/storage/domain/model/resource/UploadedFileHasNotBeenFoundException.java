package io.bincloud.storage.domain.model.resource;

public class UploadedFileHasNotBeenFoundException extends ResourceManagementException {
	private static final long serialVersionUID = -1396238065454078128L;
	public static final Long ERROR_CODE = 4L;

	public UploadedFileHasNotBeenFoundException() {
		super(Severity.INCIDENT, ERROR_CODE, "Successfully uploaded file descriptor hasn't been found.");
	}
}
