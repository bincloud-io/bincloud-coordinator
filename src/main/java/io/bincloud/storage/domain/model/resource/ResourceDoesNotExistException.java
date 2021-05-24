package io.bincloud.storage.domain.model.resource;

import lombok.Getter;

@Getter
public class ResourceDoesNotExistException extends ResourceManagementException {
	private static final long serialVersionUID = -7373747956887477836L;
	public static final Long ERROR_CODE = 2L;
	
	public ResourceDoesNotExistException() {
		super(Severity.BUSINESS, ERROR_CODE, "Resource does not exist.");
	}
}
