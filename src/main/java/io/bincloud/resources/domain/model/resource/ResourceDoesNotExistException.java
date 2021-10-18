package io.bincloud.resources.domain.model.resource;

import io.bincloud.resources.domain.model.Constants;
import io.bincloud.resources.domain.model.ResourceManagementException;
import lombok.Getter;

@Getter
public class ResourceDoesNotExistException extends ResourceManagementException {
	private static final long serialVersionUID = -7373747956887477836L;
	public static final Long ERROR_CODE = Constants.RESOURCE_DOES_NOT_EXIST_ERROR;
	
	public ResourceDoesNotExistException() {
		super(Severity.BUSINESS, ERROR_CODE, "Resource does not exist.");
	}
}
