package io.bincloud.storage.domain.model.file;

import io.bincloud.common.ApplicationException;
import lombok.NonNull;

public abstract class FileManagementException extends ApplicationException {
	private static final long serialVersionUID = 7641731127838902724L;
	public static final String CONTEXT = "STORAGE__FILE_MANAGEMENT";
	
	public FileManagementException(@NonNull Severity severity, @NonNull Long errorNumber, String message) {
		super(severity, CONTEXT, errorNumber, message);
	}
	
	public FileManagementException(@NonNull Long errorNumber, String message) {
		this(Severity.BUSINESS, errorNumber, message);
	}
}
