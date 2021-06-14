package io.bincloud.resources.domain.model.contracts;

import java.util.Optional;

import io.bincloud.resources.domain.model.Resource.ResourceDetails;

public interface ResourceManager {
	public Long createNewResource(ResourceDetails resourceDetails);
	
	public void removeExistingResource(Optional<Long> resourceId);
}
