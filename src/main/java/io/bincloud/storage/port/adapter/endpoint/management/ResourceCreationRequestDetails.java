package io.bincloud.storage.port.adapter.endpoint.management;

import io.bincloud.storage.port.adapter.acl.types.ResourceDetailsAcl;
import io.bincloud.storage.port.adapter.resource.endpoint.management.CreateNewResourceRqType;

public class ResourceCreationRequestDetails extends ResourceDetailsAcl {
	public ResourceCreationRequestDetails(CreateNewResourceRqType request) {
		super(request.getFileName());
	}
}
