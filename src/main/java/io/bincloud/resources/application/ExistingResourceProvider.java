package io.bincloud.resources.application;

import java.util.Optional;
import java.util.function.Supplier;

import io.bincloud.resources.domain.model.resource.Resource;
import io.bincloud.resources.domain.model.resource.ResourceDoesNotExistException;
import io.bincloud.resources.domain.model.resource.ResourceRepository;
import io.bincloud.resources.domain.model.resource.UnspecifiedResourceException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExistingResourceProvider implements Supplier<Resource> {
	private final Optional<Long> resourceId;
	private final ResourceRepository resourceRepository;
	
	@Override
	public Resource get() {
		Long extractedId = resourceId.orElseThrow(() -> new UnspecifiedResourceException());
		return resourceRepository.findById(extractedId).orElseThrow(() -> new ResourceDoesNotExistException());
	}

}
