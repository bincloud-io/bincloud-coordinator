package io.bincloud.storage.domain.model.resource.facades;

import java.util.Optional;

import io.bincloud.storage.domain.model.resource.Resource.ResourceDetails;

public interface ResourceManager {
	public Long createNewResource(ResourceDetails resourceDetails);
	
	public void removeExistingResource(Optional<Long> resourceId);
}
