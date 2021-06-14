package io.bincloud.resources.port.adapter.endpoint.management;

import io.bincloud.common.port.adapter.integration.global.ServiceResponseType;
import io.bincloud.resources.domain.model.Constants;

public class ResourceRemovingResponse extends ServiceResponseType {
	public ResourceRemovingResponse() {
		super();
		setBoundedContext(Constants.CONTEXT);
	}
}
