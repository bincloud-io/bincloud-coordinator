package io.bincloud.storage.domain.model.resource;

public class UnspecifiedResourceException extends ResourceManagementException {
	private static final long serialVersionUID = -6158040825883289420L;
	public static final Long ERROR_CODE = 1L;
	public static final String ERROR_MESSAGE = "Resource must be specified";
	
	public UnspecifiedResourceException() {
		super(Severity.BUSINESS, ERROR_CODE, ERROR_MESSAGE);
	}

}