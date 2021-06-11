package io.bincloud.storage.port.adapter.resource.endpoint.management;

import io.bincloud.common.port.adapter.integration.global.ServiceResponseType;
import io.bincloud.storage.domain.model.resource.Constants;

public class ResourceRemovingResponse extends ServiceResponseType {
	public ResourceRemovingResponse() {
		super();
		setBoundedContext(Constants.CONTEXT);
	}
}
