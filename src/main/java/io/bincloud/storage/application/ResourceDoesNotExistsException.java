package io.bincloud.storage.application;

import io.bincloud.storage.domain.model.resource.ResourceManagementException;
import lombok.NonNull;

public class ResourceDoesNotExistsException extends ResourceManagementException {
	private static final long serialVersionUID = 7730011267743448985L;
	public static final Long ERROR_CODE = 2L;
	public static final String ERROR_MESSAGE_TEMPLATE = "Resource[%s] does not exists in the repository";
	
	public ResourceDoesNotExistsException(@NonNull Long resourceId) {
		super(Severity.BUSINESS, ERROR_CODE, String.format(ERROR_MESSAGE_TEMPLATE, resourceId));
	}
}
