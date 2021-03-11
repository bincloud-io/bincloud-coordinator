package io.bincloud.storage.application.resource;

import io.bincloud.common.event.EventPublisher;
import io.bincloud.storage.domain.model.resource.ResourceHasBeenRemoved;
import io.bincloud.storage.domain.model.resource.ResourceRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RemoveResourceService {
	private final ResourceRepository resourceRepository;
	private final EventPublisher<ResourceHasBeenRemoved> eventPublsiher;
	
	public void removeExistingResource(Long resourceId) {
		throw new UnsupportedOperationException();
	}
}
