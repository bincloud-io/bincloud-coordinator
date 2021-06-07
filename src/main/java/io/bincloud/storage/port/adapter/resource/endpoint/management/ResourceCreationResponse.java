package io.bincloud.storage.port.adapter.resource.endpoint.management;

import io.bincloud.common.port.adapters.web.URLAddress;

public class ResourceCreationResponse extends CreateNewResourceRsType {
	public ResourceCreationResponse(String rootURL, Long resourceId) {
		super();
		setResourceId(resourceId);
		setUploadLink(createUploadingLink(rootURL, resourceId).getValue());
	}
	
	private URLAddress createUploadingLink(String rootURL, Long resourceId) {
		return new URLAddress(rootURL).append("/resource/upload?resourceId=%s", resourceId).escape();
	}
}
