package io.bincloud.storage.port.adapter.endpoint.management;

import io.bincloud.common.port.adapters.web.URLAddress;
import io.bincloud.storage.domain.model.Constants;
import io.bincloud.storage.port.adapter.resource.endpoint.management.CreateNewResourceRsType;

public class ResourceCreationResponse extends CreateNewResourceRsType {
	public ResourceCreationResponse(String rootURL, Long resourceId) {
		super();
		setResourceId(resourceId);
		setBoundedContext(Constants.CONTEXT);
		setUploadLink(createUploadingLink(rootURL, resourceId).getValue());
	}
	
	private URLAddress createUploadingLink(String rootURL, Long resourceId) {
		return new URLAddress(rootURL).append("/resource/upload?resourceId=%s", resourceId).escape();
	}
}
