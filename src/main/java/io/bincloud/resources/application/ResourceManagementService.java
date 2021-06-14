package io.bincloud.resources.application;

import java.util.Optional;

import io.bincloud.common.domain.model.generator.SequentialGenerator;
import io.bincloud.common.domain.model.validation.ValidationService;
import io.bincloud.resources.domain.model.Constants;
import io.bincloud.resources.domain.model.Resource;
import io.bincloud.resources.domain.model.ResourceRepository;
import io.bincloud.resources.domain.model.Resource.ResourceDetails;
import io.bincloud.resources.domain.model.contracts.ResourceManager;
import io.bincloud.resources.domain.model.errors.ResourceDoesNotExistException;
import io.bincloud.resources.domain.model.errors.UnspecifiedResourceException;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ResourceManagementService implements ResourceManager {
	private final SequentialGenerator<Long> idGenerator;
	private final ValidationService validationService;
	private final ResourceRepository resourceRepository;
	private final SequentialGenerator<String> defaultFileNameGenerator;
	private final FileUploadsHistory fileUploadsHistory;

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
		fileUploadsHistory.clearUploadsHistory(extractedId);
		resourceRepository.remove(extractedId);
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
