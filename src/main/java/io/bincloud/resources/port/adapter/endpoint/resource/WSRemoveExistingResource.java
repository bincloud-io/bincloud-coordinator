package io.bincloud.resources.port.adapter.endpoint.resource;

import java.util.Optional;

import io.bincloud.resources.domain.model.resource.RemoveExistingResource;
import io.bincloud.storage.port.adapter.resource.endpoint.management.RemoveExistingResourceRqType;

public class WSRemoveExistingResource implements RemoveExistingResource {
	private final Long resourceId;
	
	public WSRemoveExistingResource(RemoveExistingResourceRqType request) {
		super();
		this.resourceId = request.getResourceId();
	}
	
	@Override
	public Optional<Long> getResourceId() {
		return Optional.ofNullable(resourceId);
	}

}
