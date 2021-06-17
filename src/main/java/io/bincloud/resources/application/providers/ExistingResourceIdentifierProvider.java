package io.bincloud.resources.application.providers;

import java.util.Optional;
import java.util.function.Supplier;

import io.bincloud.resources.domain.model.Resource;
import io.bincloud.resources.domain.model.ResourceRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExistingResourceIdentifierProvider implements Supplier<Long> {
	private Supplier<Resource> resourceProvider;
	
	public ExistingResourceIdentifierProvider(Optional<Long> resourceId, ResourceRepository resourceRepository) {
		super();
		this.resourceProvider = new ExistingResourceProvider(resourceId, resourceRepository);
	}
	
	@Override
	public Long get() {
		return resourceProvider.get().getId();
	}
}
