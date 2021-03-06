package io.bincloud.storage.application;

import io.bincloud.common.validation.ValidationService;
import io.bincloud.storage.domain.model.resource.Resource.IdGenerator;
import io.bincloud.storage.domain.model.resource.Resource.ResourceDetails;
import io.bincloud.storage.domain.model.resource.ResourceRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateResourceService {
	private final IdGenerator idGenerator;
	private final ValidationService validationService;
	private final ResourceRepository resourceRepository;
	
	public Long createNewResource(ResourceDetails resourceDetails) {
		throw new UnsupportedOperationException();
	}
}
