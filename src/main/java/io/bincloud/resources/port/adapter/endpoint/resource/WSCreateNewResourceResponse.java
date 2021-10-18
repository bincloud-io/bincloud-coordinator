package io.bincloud.resources.port.adapter.endpoint.resource;

import io.bincloud.common.domain.model.web.URLAddress;
import io.bincloud.resources.domain.model.Constants;
import io.bincloud.storage.port.adapter.resource.endpoint.management.CreateNewResourceRsType;

public class WSCreateNewResourceResponse extends CreateNewResourceRsType {
	public WSCreateNewResourceResponse(String rootURL, Long resourceId) {
		super();
		setResourceId(resourceId);
		setBoundedContext(Constants.CONTEXT);
		setUploadLink(createUploadingLink(rootURL, resourceId).getValue());
	}
	
	private URLAddress createUploadingLink(String rootURL, Long resourceId) {
		return new URLAddress(rootURL).append("/resource/upload?resourceId=%s", resourceId).escape();
	}
}
