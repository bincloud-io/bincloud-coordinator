package io.bincloud.storage.port.adapter.resource.endpoint.management;

import io.bincloud.storage.port.adapter.resource.acl.types.ResourceDetailsAcl;

public class ResourceCreationRequestDetails extends ResourceDetailsAcl {
	public ResourceCreationRequestDetails(CreateNewResourceRqType request) {
		super(request.getFileName());
	}
}
