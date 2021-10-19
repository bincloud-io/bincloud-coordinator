package io.bcs.files.domain.model.errors;

public class FileHasNotBeenUploadedException extends FileManagementException {
	private static final long serialVersionUID = -1077148033501341697L;
	public static final Long ERROR_CODE = 4L;
	
	public FileHasNotBeenUploadedException() {
		super(ERROR_CODE, "File hasn't been uploaded exception");
	}
}
