package io.bincloud.storage.application;

import io.bincloud.storage.domain.model.resource.ResourceRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RemoveResourceService {
	private final ResourceRepository resourceRepository;
	
	public void removeExistingResource(Long resourceId) {
		throw new UnsupportedOperationException();
	}
}
