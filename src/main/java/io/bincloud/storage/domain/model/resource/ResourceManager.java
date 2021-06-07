package io.bincloud.storage.domain.model.resource;

import io.bincloud.storage.domain.model.resource.Resource.ResourceDetails;

public interface ResourceManager {
	public Long createNewResource(ResourceDetails resourceDetails);
}
