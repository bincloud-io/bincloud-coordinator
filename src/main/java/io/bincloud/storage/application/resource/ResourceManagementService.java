package io.bincloud.storage.application.resource;

import java.util.Optional;

import io.bincloud.common.domain.model.event.EventPublisher;
import io.bincloud.common.domain.model.generator.SequentialGenerator;
import io.bincloud.common.domain.model.validation.ValidationService;
import io.bincloud.storage.domain.model.resource.Constants;
import io.bincloud.storage.domain.model.resource.Resource;
import io.bincloud.storage.domain.model.resource.ResourceDoesNotExistException;
import io.bincloud.storage.domain.model.resource.ResourceHasBeenRemoved;
import io.bincloud.storage.domain.model.resource.Resource.ResourceDetails;
import io.bincloud.storage.domain.model.resource.ResourceManager;
import io.bincloud.storage.domain.model.resource.ResourceRepository;
import io.bincloud.storage.domain.model.resource.UnspecifiedResourceException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ResourceManagementService implements ResourceManager {
	private final SequentialGenerator<Long> idGenerator;
	private final ValidationService validationService;
	private final ResourceRepository resourceRepository;
	private final SequentialGenerator<String> defaultFileNameGenerator;
	private final EventPublisher<ResourceHasBeenRemoved> eventPublsiher;

	@Override
	public Long createNewResource(ResourceDetails resourceDetails) {
		validationService.validate(resourceDetails).checkValidState(Constants.CONTEXT,
				Constants.INVALID_RESOURCE_DETAILS_ERROR);
		Resource resource = new Resource(idGenerator, resourceDetails, defaultFileNameGenerator);
		resourceRepository.save(resource);
		return resource.getId();
	}

	@Override
	public void removeExistingResource(Optional<Long> resourceId) {
		Long extractedId = extractRemovableResourceId(resourceId);
		resourceRepository.remove(extractedId);
		eventPublsiher.publish(new ResourceHasBeenRemoved(extractedId));
	}

	private Long extractRemovableResourceId(Optional<Long> resourceId) {
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
