package io.bincloud.storage.domain.model.contracts;

import java.util.Optional;

import io.bincloud.storage.domain.model.Resource.ResourceDetails;

public interface ResourceManager {
	public Long createNewResource(ResourceDetails resourceDetails);
	
	public void removeExistingResource(Optional<Long> resourceId);
}
