package io.bincloud.storage.application.resource;

import io.bincloud.common.domain.model.generator.SequentialGenerator;
import io.bincloud.common.domain.model.validation.ValidationService;
import io.bincloud.storage.domain.model.resource.Constants;
import io.bincloud.storage.domain.model.resource.Resource;
import io.bincloud.storage.domain.model.resource.Resource.ResourceDetails;
import io.bincloud.storage.domain.model.resource.ResourceManager;
import io.bincloud.storage.domain.model.resource.ResourceRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ResourceManagementService implements ResourceManager {
	private final SequentialGenerator<Long> idGenerator;
	private final ValidationService validationService;
	private final ResourceRepository resourceRepository;
	private final SequentialGenerator<String> defaultFileNameGenerator;

	@Override
	public Long createNewResource(ResourceDetails resourceDetails) {
		validationService.validate(resourceDetails).checkValidState(Constants.CONTEXT,
				Constants.INVALID_RESOURCE_DETAILS_ERROR);
		Resource resource = new Resource(idGenerator, resourceDetails, defaultFileNameGenerator);
		resourceRepository.save(resource);
		return resource.getId();
	}
}
