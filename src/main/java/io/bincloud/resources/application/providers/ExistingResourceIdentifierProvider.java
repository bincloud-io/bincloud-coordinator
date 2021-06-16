package io.bincloud.resources.application.providers;

import java.util.Optional;
import java.util.function.Supplier;

import io.bincloud.resources.domain.model.ResourceRepository;
import io.bincloud.resources.domain.model.errors.ResourceDoesNotExistException;
import io.bincloud.resources.domain.model.errors.UnspecifiedResourceException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExistingResourceIdentifierProvider implements Supplier<Long> {
	private final Optional<Long> resourceId;
	private final ResourceRepository resourceRepository;
	
	@Override
	public Long get() {
		Long extractedId = resourceId.orElseThrow(() -> new UnspecifiedResourceException());
		checkResourceExistence(extractedId);
		return extractedId;
	}
	
	private void checkResourceExistence(Long extractedId) {
		if (!resourceRepository.isExists(extractedId)) {
			throw new ResourceDoesNotExistException();
		}
	}
}
