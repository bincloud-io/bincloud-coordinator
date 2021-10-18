package io.bincloud.resources.port.adapter.endpoint.resource;

import io.bincloud.common.port.adapter.integration.global.ServiceResponseType;
import io.bincloud.resources.domain.model.Constants;

public class WSRemoveExistingResourceResponse extends ServiceResponseType {
	public WSRemoveExistingResourceResponse() {
		super();
		setBoundedContext(Constants.CONTEXT);
	}
}
